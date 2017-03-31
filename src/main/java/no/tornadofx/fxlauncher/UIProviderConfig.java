package no.tornadofx.fxlauncher;

/**
 * Object to contain the settings needed for the uiProverClass.
 *
 */
public class UIProviderConfig {
    private String uiProviderClass;
    private String uiProviderPackage;
    private String uiProviderLocation;
    /**
     * Empty constructor dont know if it is needed.
     */
    public UIProviderConfig() {
    }

    public UIProviderConfig(String uiProviderClass, String UIProviderPackage) {
        this.uiProviderClass = uiProviderClass;
        this.uiProviderPackage = UIProviderPackage;
    }

    public String getUiProviderClass() {
        return uiProviderClass;
    }

    public void setUiProviderClass(String uiProviderClass) {
        this.uiProviderClass = uiProviderClass;
    }

    public String getUiProviderPackage() {
        return uiProviderPackage;
    }

    public void setUiProviderPackage(String uiProviderPackage) {
        this.uiProviderPackage = uiProviderPackage;
    }

    @Override
    public String toString() {
        return "UIProviderConfig{" +
                "uiProviderClass='" + uiProviderClass + '\'' +
                ", uiProviderPackage='" + uiProviderPackage + '\'' +
                '}';
    }

    public String getUiProviderLocation() {
        return uiProviderLocation;
    }

    public void setUiProviderLocation(String uiProviderLocation) {
        this.uiProviderLocation = uiProviderLocation;
    }
}
