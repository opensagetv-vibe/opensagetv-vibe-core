package sage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Bounded bridge between the server DVD VM's MPEG-PS bytes and FFmpeg/MIM.
 *
 * <p>The DVD VM remains authoritative for navigation, title/chapter state,
 * audio/subpicture selection, and SageTV control. MIM changes only the pushed
 * media representation to H.264/AC-3/DVB-subtitle MPEG-TS.</p>
 */
final class MiniDVDStreamTranscoder
{
  static final String CAPABILITY_TOKEN = "\"dvdStreamTransform\":true";
  private static final int OUTPUT_CHUNK_SIZE = 32768;
  private static final int OUTPUT_QUEUE_CHUNKS = 128;

  private static String cachedToolIdentity;
  private static boolean cachedAvailable;

  private final Process process;
  private final OutputStream input;
  private final InputStream output;
  private final ArrayBlockingQueue<byte[]> outputQueue =
      new ArrayBlockingQueue<byte[]>(OUTPUT_QUEUE_CHUNKS);
  private volatile IOException outputFailure;
  private volatile boolean outputEnded;
  private volatile boolean inputClosed;

  private MiniDVDStreamTranscoder(Process process)
  {
    this.process = process;
    input = process.getOutputStream();
    output = process.getInputStream();
    startOutputReader();
    startErrorReader(process.getErrorStream());
  }

  static synchronized boolean isAvailable()
  {
    File tool;
    try
    {
      // Use the same stock SageTV transcoder resolver as every ordinary
      // FFMPEGTranscoder job. This preserves stock ffmpeg fallback while
      // allowing an installed SageTVTranscoder plugin bridge to advertise
      // and provide the DVD stream transform.
      tool = resolveTranscoderTool();
    }
    catch (RuntimeException e)
    {
      if (Sage.DBG) System.out.println("DVD MIM capability probe has no transcoder: " + e);
      return false;
    }
    String identity = tool.getAbsolutePath() + ':' + tool.length() + ':' + tool.lastModified();
    if (identity.equals(cachedToolIdentity))
      return cachedAvailable;

    cachedToolIdentity = identity;
    cachedAvailable = false;
    if (!tool.isFile())
      return false;

    Process probe = null;
    try
    {
      probe = new ProcessBuilder(tool.getAbsolutePath(), "--mim-capabilities")
          .redirectErrorStream(true).start();
      if (!probe.waitFor(3, TimeUnit.SECONDS))
      {
        probe.destroyForcibly();
        return false;
      }
      String reply = readBounded(probe.getInputStream(), 16384);
      cachedAvailable = probe.exitValue() == 0 && capabilitiesAdvertiseTransform(reply);
    }
    catch (Throwable t)
    {
      if (Sage.DBG) System.out.println("DVD MIM capability probe failed: " + t);
    }
    finally
    {
      if (probe != null)
        probe.destroy();
    }
    return cachedAvailable;
  }

  static boolean capabilitiesAdvertiseTransform(String reply)
  {
    return reply != null && reply.indexOf(CAPABILITY_TOKEN) >= 0;
  }

  static MiniDVDStreamTranscoder start() throws IOException
  {
    File tool;
    try
    {
      tool = resolveTranscoderTool();
    }
    catch (RuntimeException e)
    {
      throw new IOException("DVD FFmpeg/MIM transcoder is unavailable", e);
    }
    if (!tool.isFile())
      throw new IOException("FFmpeg/MIM executable is missing: " + tool);

    String bitrate = Sage.get("miniclient/dvd_mim_video_bitrate", "6M");
    if (!bitrate.matches("[1-9][0-9]*[kKmM]?"))
      bitrate = "6M";

    List<String> command = new ArrayList<String>();
    command.add(tool.getAbsolutePath());
    command.add("-sagetvdiscstream");
    command.add("-f"); command.add("mpeg");
    command.add("-i"); command.add("-");
    command.add("-map"); command.add("0:v:0");
    command.add("-map"); command.add("0:a?");
    command.add("-map"); command.add("0:s?");
    command.add("-vcodec"); command.add("mpeg4");
    command.add("-b:v"); command.add(bitrate);
    command.add("-acodec"); command.add("copy");
    command.add("-c:s"); command.add("dvbsub");
    command.add("-f"); command.add("mpegts");
    command.add("-");

    if (Sage.DBG) System.out.println("Starting DVD FFmpeg/MIM stream transform: " + command);
    return new MiniDVDStreamTranscoder(new ProcessBuilder(command).start());
  }

  static File resolveTranscoderTool()
  {
    return new File(FFMPEGTranscoder.getTranscoderPath());
  }

  void write(byte[] data, int offset, int length) throws IOException
  {
    if (outputFailure != null)
      throw outputFailure;
    if (inputClosed)
      throw new IOException("DVD MIM input is already closed");
    input.write(data, offset, length);
  }

  byte[] pollOutput(long timeoutMillis) throws IOException
  {
    if (outputFailure != null)
      throw outputFailure;
    try
    {
      return timeoutMillis <= 0 ? outputQueue.poll() :
          outputQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted waiting for DVD MIM output", e);
    }
  }

  boolean isOutputEnded()
  {
    return outputEnded && outputQueue.isEmpty();
  }

  void closeInput()
  {
    if (inputClosed)
      return;
    inputClosed = true;
    try { input.close(); } catch (IOException e) {}
  }

  void close()
  {
    closeInput();
    try { output.close(); } catch (IOException e) {}
    process.destroy();
    try
    {
      if (!process.waitFor(2, TimeUnit.SECONDS))
        process.destroyForcibly();
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
    outputQueue.clear();
  }

  private void startOutputReader()
  {
    Thread reader = new Thread(new Runnable()
    {
      public void run()
      {
        byte[] buffer = new byte[OUTPUT_CHUNK_SIZE];
        try
        {
          int read;
          while ((read = output.read(buffer)) >= 0)
          {
            if (read == 0)
              continue;
            byte[] chunk = new byte[read];
            System.arraycopy(buffer, 0, chunk, 0, read);
            outputQueue.put(chunk);
          }
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
        }
        catch (IOException e)
        {
          if (process.isAlive())
            outputFailure = e;
        }
        finally
        {
          outputEnded = true;
        }
      }
    }, "DVD-MIM-Output");
    reader.setDaemon(true);
    reader.start();
  }

  private void startErrorReader(final InputStream errors)
  {
    Thread reader = new Thread(new Runnable()
    {
      public void run()
      {
        byte[] buffer = new byte[4096];
        try
        {
          int read;
          while ((read = errors.read(buffer)) >= 0)
          {
            if (read > 0 && Sage.DBG)
              System.out.print("DVD MIM: " + new String(buffer, 0, read, Sage.BYTE_CHARSET));
          }
        }
        catch (IOException e) {}
        finally
        {
          try { errors.close(); } catch (IOException e) {}
        }
      }
    }, "DVD-MIM-Errors");
    reader.setDaemon(true);
    reader.start();
  }

  private static String readBounded(InputStream in, int maximum) throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int read;
    while (out.size() < maximum && (read = in.read(buffer, 0,
        Math.min(buffer.length, maximum - out.size()))) >= 0)
    {
      if (read > 0)
        out.write(buffer, 0, read);
    }
    return new String(out.toByteArray(), Sage.BYTE_CHARSET);
  }
}
