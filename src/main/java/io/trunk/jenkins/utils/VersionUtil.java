package io.trunk.jenkins.utils;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class VersionUtil {

    private static final String VERSION = loadVersionFromPom();

    private static String loadVersionFromPom() {
        try (final var file = new FileReader("pom.xml", StandardCharsets.UTF_8)) {
            final var reader = new MavenXpp3Reader();
            final var model = reader.read(file);
            return String.format("%s%s",
                    model.getProperties().getProperty("revision"),
                    model.getProperties().getProperty("changelist"));
        } catch (IOException | XmlPullParserException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}
