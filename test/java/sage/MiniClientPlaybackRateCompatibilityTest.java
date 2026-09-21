package sage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MiniClientPlaybackRateCompatibilityTest
{
  @Test
  public void capabilityRequiresAnExplicitNonEmptyClientProperty()
  {
    assertFalse(MiniClientSageRenderer.isVideoPlaybackRateSupported(null));
    assertFalse(MiniClientSageRenderer.isVideoPlaybackRateSupported(""));
    assertFalse(MiniClientSageRenderer.isVideoPlaybackRateSupported("  "));
    assertTrue(MiniClientSageRenderer.isVideoPlaybackRateSupported(
        "NATIVE_FORWARD_0.5_TO_2;SEEK_SCAN_4_TO_256"));
  }
}
