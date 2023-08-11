package io.trunk.jenkins.utils;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;

public class VersionUtil {

    private static final String VERSION = loadVersionFromPom();

    private static String loadVersionFromPom() {
        try {
            final var reader = new MavenXpp3Reader();
            final var model = reader.read(new FileReader("pom.xml"));
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
