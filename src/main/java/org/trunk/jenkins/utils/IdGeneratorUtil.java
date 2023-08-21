package org.trunk.jenkins.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Blake3;

import java.nio.charset.StandardCharsets;

public class IdGeneratorUtil {

    public static String hashString(String name) {
        final var h = Blake3.initHash();
        final byte[] buf = new byte[16]; // 128 bit
        h.update(name.getBytes(StandardCharsets.UTF_8));
        h.doFinalize(buf);
        return Hex.encodeHexString(buf);
    }

}
