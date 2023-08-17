package io.trunk.jenkins;


import java.util.logging.Logger;

public class TrunkLog {

    private static final Logger LOG = Logger.getLogger(TrunkLog.class.getName());

    public static void info(String message) {
        if (Configuration.get().enableDebugLogging) {
            LOG.info(message);
        }
    }

    public static void warning(String message) {
        if (Configuration.get().enableDebugLogging) {
            LOG.warning(message);
        }
    }

}
