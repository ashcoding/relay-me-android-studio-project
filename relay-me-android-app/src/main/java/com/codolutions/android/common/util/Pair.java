package com.codolutions.android.common.util;


public class Pair<F, S> extends android.util.Pair<F, S> {
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);
    }

    public Pair(F first, S second) {
        super(first, second);
    }

    @Override
    public String toString() {
        return "[" + first + " , " + second + "]";
    }
}
