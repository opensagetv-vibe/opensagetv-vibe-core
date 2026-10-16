package sage;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/** Focused transport regression coverage for directory-backed Blu-ray media. */
public class MiniPlayerBluRayTransportTest
{
  @Test
  public void ordinaryPullCapableMediaRetainsPullEligibility()
  {
    assertTrue(MiniPlayer.shouldUsePullPlayback(true, false));
  }

  @Test
  public void nonPullCapableMediaRemainsPushOnly()
  {
    assertFalse(MiniPlayer.shouldUsePullPlayback(false, false));
  }

  @Test
  public void blurayDirectoryNeverUsesOpenUrlPull()
  {
    assertFalse(MiniPlayer.shouldUsePullPlayback(true, true));
    assertFalse(MiniPlayer.shouldUsePullPlayback(false, true));
  }
}
