package sage;

import org.testng.annotations.Test;

import java.io.File;

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
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("native"), "native");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("HYBRID"), "hybrid");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("mim_main_feature"), "mim_main_feature");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy("future-mode"), "auto");
    assertEquals(MiniClientSageRenderer.normalizeDvdDiscPolicy(null), "auto");
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

  @Test
  public void dvdMimUsesTheStockSageTvTranscoderPreference() throws Exception
  {
    String originalToolsPath = System.getProperty("sage.paths.tools");
    File tools = File.createTempFile("sagetv-dvd-mim-tools", "");
    assertTrue(tools.delete());
    assertTrue(tools.mkdirs());
    try
    {
      System.setProperty("sage.paths.tools", tools.getAbsolutePath());
      File stock = new File(Sage.getToolPath("ffmpeg"));
      File bridge = new File(Sage.getToolPath("SageTVTranscoder"));
      assertTrue(stock.createNewFile());
      assertTrue(bridge.createNewFile());

      assertEquals(MiniDVDStreamTranscoder.resolveTranscoderTool().getCanonicalFile(),
          bridge.getCanonicalFile());
    }
    finally
    {
      if (originalToolsPath == null)
        System.clearProperty("sage.paths.tools");
      else
        System.setProperty("sage.paths.tools", originalToolsPath);
      new File(tools, Sage.WINDOWS_OS ? "ffmpeg.exe" : "ffmpeg").delete();
      new File(tools, Sage.WINDOWS_OS ? "SageTVTranscoder.exe" : "SageTVTranscoder").delete();
      tools.delete();
    }
  }
}
