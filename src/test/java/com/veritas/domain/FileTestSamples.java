package com.veritas.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static File getFileSample1() {
        return new File()
            .id(1L)
            .filename("filename1")
            .bucketName("bucketName1")
            .objectName("objectName1")
            .contentType("contentType1")
            .fileSize(1L)
            .uploadedBy("uploadedBy1");
    }

    public static File getFileSample2() {
        return new File()
            .id(2L)
            .filename("filename2")
            .bucketName("bucketName2")
            .objectName("objectName2")
            .contentType("contentType2")
            .fileSize(2L)
            .uploadedBy("uploadedBy2");
    }

    public static File getFileRandomSampleGenerator() {
        return new File()
            .id(longCount.incrementAndGet())
            .filename(UUID.randomUUID().toString())
            .bucketName(UUID.randomUUID().toString())
            .objectName(UUID.randomUUID().toString())
            .contentType(UUID.randomUUID().toString())
            .fileSize(longCount.incrementAndGet())
            .uploadedBy(UUID.randomUUID().toString());
    }
}
