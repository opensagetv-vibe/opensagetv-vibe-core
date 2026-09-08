package sage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MiniClientVibeWatchCompatibilityTest
{
  @Test
  public void stoppedOrPausedRedundantWatchResumes()
  {
    assertTrue(MiniClientSageRenderer.shouldResumeVibeRedundantWatch(
        true, MediaPlayer.STOPPED_STATE));
    assertTrue(MiniClientSageRenderer.shouldResumeVibeRedundantWatch(
        true, MediaPlayer.PAUSE_STATE));
  }

  @Test
  public void activeOrDifferentFileDoesNotIssueAnExtraPlay()
  {
    assertFalse(MiniClientSageRenderer.shouldResumeVibeRedundantWatch(
        true, MediaPlayer.PLAY_STATE));
    assertFalse(MiniClientSageRenderer.shouldResumeVibeRedundantWatch(
        false, MediaPlayer.STOPPED_STATE));
  }
}
