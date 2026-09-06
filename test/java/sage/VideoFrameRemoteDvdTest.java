package sage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class VideoFrameRemoteDvdTest
{
  @Test
  public void negotiatedClientUsesServerDvdVmEvenWithMouseInput()
  {
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(true, "IR,KEYBOARD,MOUSE"));
  }

  @Test
  public void legacyExtenderBehaviorIsPreserved()
  {
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(false, "IR,TV"));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(false, null));
    assertFalse(MiniDVDPlayerSelection.shouldUseServerNavigation(false, "IR,KEYBOARD,MOUSE"));
  }

  @Test
  public void unavailableExplicitDiscModeFailsClosed()
  {
    assertFalse(MiniDVDPlayerSelection.shouldUseServerNavigation(
        false, "IR,TV", "hybrid", false, false));
    assertFalse(MiniDVDPlayerSelection.shouldUseServerNavigation(
        false, "IR,TV", "mim_main_feature", false, false));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(
        true, "IR,TV", "hybrid", true, false));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(
        false, "IR,TV", "auto", true, false));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(
        true, "IR,TV", "hybrid", false, true));
  }

  @Test
  public void mimTransportRequiresBothNegotiatedEnds()
  {
    assertTrue(MiniDVDPlayerSelection.shouldUseMimTransport("hybrid", true, true));
    assertTrue(MiniDVDPlayerSelection.shouldUseMimTransport("mim_main_feature", true, true));
    assertFalse(MiniDVDPlayerSelection.shouldUseMimTransport("auto", true, true));
    assertFalse(MiniDVDPlayerSelection.shouldUseMimTransport("hybrid", false, true));
    assertFalse(MiniDVDPlayerSelection.shouldUseMimTransport("hybrid", true, false));
  }

  @Test
  public void navTransitionUsesVmButtonInsteadOfStaleRenderedButton()
  {
    assertEquals(MiniDVDPlayerSelection.currentNavButton(1, 2), 1);
    assertEquals(MiniDVDPlayerSelection.currentNavButton(0, 2), 2);
  }

  @Test
  public void discSeekNeverPassesNegativeTimeToDvdVm()
  {
    assertEquals(MiniDVDPlayerSelection.clampDiscSeekTime(-30000), 0);
    assertEquals(MiniDVDPlayerSelection.clampDiscSeekTime(0), 0);
    assertEquals(MiniDVDPlayerSelection.clampDiscSeekTime(12500), 12500);
  }

  @Test
  public void authoredMenuActivationPreservesTheDvdVmLanguageSelection()
  {
    assertTrue(MiniDVDPlayerSelection.menuActivationOwnsLanguageSelections(208, 2));
    assertTrue(MiniDVDPlayerSelection.menuActivationOwnsLanguageSelections(208, 3));
    assertFalse(MiniDVDPlayerSelection.menuActivationOwnsLanguageSelections(208, 4));
    assertFalse(MiniDVDPlayerSelection.menuActivationOwnsLanguageSelections(210, 3));
  }

  @Test
  public void discPolicyNegotiationRejectsUnknownValues()
  {
    assertEquals(MiniClientSageRenderer.normalizeVibeDiscPolicy("native"), "native");
    assertEquals(MiniClientSageRenderer.normalizeVibeDiscPolicy("HYBRID"), "hybrid");
    assertEquals(MiniClientSageRenderer.normalizeVibeDiscPolicy("mim_main_feature"), "mim_main_feature");
    assertEquals(MiniClientSageRenderer.normalizeVibeDiscPolicy("future-mode"), "auto");
    assertEquals(MiniClientSageRenderer.normalizeVibeDiscPolicy(null), "auto");
  }

  @Test
  public void mimCapabilityProbeRequiresTheExplicitBooleanToken()
  {
    assertTrue(MiniDVDStreamTranscoder.capabilitiesAdvertiseTransform(
        "{\"mimVersion\":\"0.4.8\",\"dvdStreamTransform\":true}"));
    assertFalse(MiniDVDStreamTranscoder.capabilitiesAdvertiseTransform(
        "{\"dvdStreamTransform\":false}"));
    assertFalse(MiniDVDStreamTranscoder.capabilitiesAdvertiseTransform(null));
  }
}
