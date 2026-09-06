package sage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MiniPlayerMediaStateTest
{
  @Test
  public void appendsVersionedEncodedMediaStateFragment()
  {
    assertEquals(
        "stv://host//tv/a.ts#sagetv-media-v1;active=1;buffer=4096;major=1;minor=2;channel=5.1;encoding=MPEG2-TS%3Bvideo%3D1",
        MiniPlayer.appendMediaStateUrl("stv://host//tv/a.ts", (byte) 1, (byte) 2,
            "MPEG2-TS;video=1", "5.1", true, 4096));
  }
}
