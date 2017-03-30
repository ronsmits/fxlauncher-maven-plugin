package no.tornadofx.fxlauncher;

/**
 * Object to contain the settings needed for the uiProverClass.
 *
 */
public class UIProviderConfig {
    private String UiProviderClass;
    private String UiProviderPackage;
    private String UiProviderLocation;
    /**
     * Empty constructor dont know if it is needed.
     */
    public UIProviderConfig() {
    }

    public UIProviderConfig(String uiProviderClass, String UIProviderPackage) {
        UiProviderClass = uiProviderClass;
        this.UiProviderPackage = UIProviderPackage;
    }

    public String getUiProviderClass() {
        return UiProviderClass;
    }

    public void setUiProviderClass(String uiProviderClass) {
        UiProviderClass = uiProviderClass;
    }

    public String getUiProviderPackage() {
        return UiProviderPackage;
    }

    public void setUiProviderPackage(String uiProviderPackage) {
        this.UiProviderPackage = uiProviderPackage;
    }

    @Override
    public String toString() {
        return "UIProviderConfig{" +
                "UiProviderClass='" + UiProviderClass + '\'' +
                ", UiProviderPackage='" + UiProviderPackage + '\'' +
                '}';
    }

    public String getUiProviderLocation() {
        return UiProviderLocation;
    }

    public void setUiProviderLocation(String uiProviderLocation) {
        UiProviderLocation = uiProviderLocation;
    }
}
