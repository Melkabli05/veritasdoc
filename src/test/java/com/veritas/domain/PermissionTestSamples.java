package com.veritas.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PermissionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Permission getPermissionSample1() {
        return new Permission().id(1L).fileId("fileId1").userId("userId1").permission("permission1");
    }

    public static Permission getPermissionSample2() {
        return new Permission().id(2L).fileId("fileId2").userId("userId2").permission("permission2");
    }

    public static Permission getPermissionRandomSampleGenerator() {
        return new Permission()
            .id(longCount.incrementAndGet())
            .fileId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .permission(UUID.randomUUID().toString());
    }
}
