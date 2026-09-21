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
        false, "IR,TV", "transformed_main_feature", false, false));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(
        true, "IR,TV", "hybrid", true, false));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(
        false, "IR,TV", "auto", true, false));
    assertTrue(MiniDVDPlayerSelection.shouldUseServerNavigation(
        true, "IR,TV", "hybrid", false, true));
  }

  @Test
  public void transformRequiresExplicitPolicyAndAvailableProvider()
  {
    assertTrue(MiniDVDPlayerSelection.shouldUseTransform("hybrid", true));
    assertTrue(MiniDVDPlayerSelection.shouldUseTransform("transformed_main_feature", true));
    assertFalse(MiniDVDPlayerSelection.shouldUseTransform("auto", true));
    assertFalse(MiniDVDPlayerSelection.shouldUseTransform("hybrid", false));
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
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("native"), "native");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("HYBRID"), "hybrid");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("transformed_main_feature"),
        "transformed_main_feature");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("future-mode"), "auto");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy(null), "auto");
  }

  @Test
  public void transportIntersectionIsExactAndCaseInsensitive()
  {
    assertTrue(DVDStreamTransformRegistry.supportsTransport(
        "native,dvd_mpegts_v1", "dvd_mpegts_v1"));
    assertTrue(DVDStreamTransformRegistry.supportsTransport(
        "NATIVE, DVD_MPEGTS_V1", "dvd_mpegts_v1"));
    assertFalse(DVDStreamTransformRegistry.supportsTransport(
        "native,dvd_mpegts_v10", "dvd_mpegts_v1"));
    assertFalse(DVDStreamTransformRegistry.supportsTransport(null,
        "dvd_mpegts_v1"));
  }

  @Test
  public void transformRequestBoundsInvalidBitrate()
  {
    assertEquals(new DVDStreamTransformRequest("dvd_mpegts_v1", "8M")
        .getVideoBitrate(), "8M");
    assertEquals(new DVDStreamTransformRequest("dvd_mpegts_v1", "bad value")
        .getVideoBitrate(), "6M");
  }

  @Test
  public void optionalProviderIsDiscoveredOnlyForNegotiatedTransport()
  {
    assertEquals(DVDStreamTransformRegistry.findAvailable(
        "native,test_transform_v1").getTransportId(), "test_transform_v1");
    assertEquals(DVDStreamTransformRegistry.findAvailable("native"), null);
  }
}
