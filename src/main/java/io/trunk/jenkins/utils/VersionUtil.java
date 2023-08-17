package io.trunk.jenkins.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class VersionUtil {

    private static final String VERSION = loadVersionFromPom();

    private static String loadVersionFromPom() {
        try {
            final Properties properties = new Properties();
            properties.load(VersionUtil.class.getClassLoader().getResourceAsStream("version.properties"));
            final var version = properties.getProperty("io.trunk.jenkins.version");
            Logger.getLogger(VersionUtil.class.getName()).info(String.format("Trunk plugin version: %s", version));
            return version;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}
