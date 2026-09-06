package sage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class MiniPlayerCaptionStateTest
{
  @Test
  public void retainsSageTvCaptionStateWithoutConnectedRenderer()
  {
    MiniPlayer player = new MiniPlayer();

    assertEquals(MediaPlayer.CC_DISABLED, player.getClosedCaptioningState());
    assertFalse(player.setClosedCaptioningState(MediaPlayer.CC_ENABLED_CAPTION1));
    assertEquals(MediaPlayer.CC_ENABLED_CAPTION1, player.getClosedCaptioningState());
    assertFalse(player.setClosedCaptioningState(MediaPlayer.CC_DISABLED));
    assertEquals(MediaPlayer.CC_DISABLED, player.getClosedCaptioningState());
  }
}
