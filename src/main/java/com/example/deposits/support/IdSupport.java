package com.example.deposits.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Time-sortable numeric ID generator.
 * Shifts current time millis left by 20 bits and ORs with a rolling counter
 * to give collision-safe IDs under fast loops.
 */
public final class IdSupport {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final int COUNTER_MASK = 0xFFFFF; // 20 bits

    private IdSupport() {}

    public static long nextId() {
        int seq = COUNTER.getAndIncrement() & COUNTER_MASK;
        return (System.currentTimeMillis() << 20) | seq;
    }
}
