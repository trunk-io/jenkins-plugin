package io.jenkins.plugins.trunk.utils;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionUtilTest {

    @Test
    public void testGetVersion() {
        assertNotNull(VersionUtil.getVersion());
    }

}