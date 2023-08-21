package org.trunk.jenkins.utils;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionUtilTest {

    @Test
    public void testGetVersion() {
        assertNotNull(VersionUtil.getVersion());
    }

}