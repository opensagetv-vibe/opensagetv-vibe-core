package sage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MiniClientVibeChannelTest
{
  @Test
  public void acceptsLogicalAtscAndWholeChannels()
  {
    assertTrue(MiniClientSageRenderer.isValidVibeChannel("2.1"));
    assertTrue(MiniClientSageRenderer.isValidVibeChannel("5.1"));
    assertTrue(MiniClientSageRenderer.isValidVibeChannel("11"));
  }

  @Test
  public void rejectsControlCharactersAndNonChannelPayloads()
  {
    assertFalse(MiniClientSageRenderer.isValidVibeChannel(null));
    assertFalse(MiniClientSageRenderer.isValidVibeChannel(""));
    assertFalse(MiniClientSageRenderer.isValidVibeChannel("2-1"));
    assertFalse(MiniClientSageRenderer.isValidVibeChannel("2.1\nstop"));
    assertFalse(MiniClientSageRenderer.isValidVibeChannel("live_tv"));
  }
}
