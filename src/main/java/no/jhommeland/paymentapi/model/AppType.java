package no.jhommeland.paymentapi.model;

public enum AppType {
    IOS("iOS", "com.paymentapp-ios://"),
    ANDROID("Android", "com.paymentapp-android://"),
    FLUTTER("Flutter", "com.paymentapp-flutter://");

    private final String name;

    private final String deepLinkPrefix;

    public String getDeepLinkPrefix() {
        return deepLinkPrefix;
    }

    public String getName() {
        return name;
    }

    AppType(String name, String deepLinkPrefix) {
        this.name = name;
        this.deepLinkPrefix = deepLinkPrefix;
    }

    public static AppType getAppTypeFromName(String name) {
        for (AppType appType : AppType.values()) {
            if (name.contains(appType.getName())) {
                return appType;
            }
        }
        return null;
    }
}
