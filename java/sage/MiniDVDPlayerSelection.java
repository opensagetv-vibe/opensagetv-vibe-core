package sage;

/** Selection policy kept independent of VideoFrame's heavyweight static state. */
final class MiniDVDPlayerSelection
{
  private static final int DVD_CONTROL_ACTIVATE_CURRENT = 208;
  private static final int VTS_DOMAIN = 4;

  private MiniDVDPlayerSelection()
  {
  }

  static boolean shouldUseServerNavigation(boolean remoteNavigationSupport, String inputDevices)
  {
    return remoteNavigationSupport || inputDevices == null || inputDevices.indexOf("MOUSE") == -1;
  }

  static boolean shouldUseServerNavigation(boolean remoteNavigationSupport, String inputDevices,
      String discPolicy, boolean nativeFallback, boolean transformAvailable)
  {
    boolean unavailableExplicitMode = ("hybrid".equals(discPolicy) ||
        "transformed_main_feature".equals(discPolicy)) && !transformAvailable && !nativeFallback;
    return !unavailableExplicitMode &&
        shouldUseServerNavigation(remoteNavigationSupport, inputDevices);
  }

  static boolean shouldUseTransform(String discPolicy, boolean transformAvailable)
  {
    return ("hybrid".equals(discPolicy) || "transformed_main_feature".equals(discPolicy)) &&
        transformAvailable;
  }

  /** Select the VM-owned button after a DVD NAV/program-chain transition. */
  static int currentNavButton(int vmButton, int previousButton)
  {
    return vmButton > 0 ? vmButton : previousButton;
  }

  /** Keeps remote DVD VM PTS/sector seeks inside the authored title domain. */
  static long clampDiscSeekTime(long requestedTime)
  {
    return Math.max(0, requestedTime);
  }

  /**
   * Once a user activates an authored menu button, the DVD VM owns the audio
   * and subtitle selections made by that navigation path. Applying SageTV's
   * default-language preference on the following menu-to-title transition
   * would overwrite the disc's explicit SetSTN command.
   */
  static boolean menuActivationOwnsLanguageSelections(int controlCode, int domain)
  {
    return controlCode == DVD_CONTROL_ACTIVATE_CURRENT && domain != VTS_DOMAIN;
  }

  static boolean clientRequestsMenuSkip(SageRenderer renderer)
  {
    if (!(renderer instanceof MiniClientSageRenderer))
      return false;
    MiniClientSageRenderer mini = (MiniClientSageRenderer) renderer;
    return mini.supportsRemoteDVDNavigation() &&
        (mini.isDvdDiscSkipMenus() || "transformed_main_feature".equals(mini.getDvdDiscPolicy()));
  }

  static boolean clientRequestsPreviewSkip(SageRenderer renderer)
  {
    return renderer instanceof MiniClientSageRenderer &&
        ((MiniClientSageRenderer) renderer).supportsRemoteDVDNavigation() &&
        !((MiniClientSageRenderer) renderer).isDvdDiscSkipMenus() &&
        ((MiniClientSageRenderer) renderer).isDvdDiscSkipPreviews();
  }
}
