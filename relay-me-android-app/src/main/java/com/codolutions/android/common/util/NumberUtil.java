package com.codolutions.android.common.util;

import java.util.Date;
import java.util.Random;

public class NumberUtil {
    public static final long INVALID_RANDOM_NUMBER = Long.MIN_VALUE;

    public static long getRandomNumber() {
        long randomNumber = doGetRandomNumber();
        while (randomNumber == INVALID_RANDOM_NUMBER)
            randomNumber = doGetRandomNumber();
        return randomNumber;
    }

    private static long doGetRandomNumber() {
        long timestamp = new Date().getTime();
        int random = new Random(timestamp).nextInt(0xffff);
        return (timestamp << 16) | (random & 0xffff);
    }
}
