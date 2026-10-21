package com.example.taxi.util;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public final class RideNumberGenerator {

    private RideNumberGenerator() {
    }

    public static String next(long seq) {
        return "COURSE-" + LocalDate.now().getYear() + "-" + String.format("%04d", seq);
    }

    public static String confirmationCode() {
        return "C-" + LocalDate.now().getYear() + "-"
                + String.format("%06d", ThreadLocalRandom.current().nextInt(0, 999999));
    }
}