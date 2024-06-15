package com.veritas.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileVersionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static FileVersion getFileVersionSample1() {
        return new FileVersion().id(1L).fileId("fileId1").versionNumber(1).objectName("objectName1");
    }

    public static FileVersion getFileVersionSample2() {
        return new FileVersion().id(2L).fileId("fileId2").versionNumber(2).objectName("objectName2");
    }

    public static FileVersion getFileVersionRandomSampleGenerator() {
        return new FileVersion()
            .id(longCount.incrementAndGet())
            .fileId(UUID.randomUUID().toString())
            .versionNumber(intCount.incrementAndGet())
            .objectName(UUID.randomUUID().toString());
    }
}
