package org.daisy.stevin.netty.spring;

import lombok.NonNull;

public class NettyStringUtil {
    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final char DOT = '.';
    public static final char TAB = '\t';
    public static final char DOLLAR = '$';

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean equals(final String a, final String b) {
        return equals(a, b, (_a, _b) -> (_a.equals(_b)));
    }

    public static boolean equalsIgnoreCase(final String a, final String b) {
        return equals(a, b, (_a, _b) -> (_a.equalsIgnoreCase(_b)));
    }

    private static boolean equals(final String a, final String b, EqualsFunction func) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return func.equals(b, a);
            }
        } else {
            return func.equals(a, b);
        }
    }

    @FunctionalInterface
    private static interface EqualsFunction {
        boolean equals(String a, String b);
    }

    public static String toString(@NonNull Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            sb.append(object);
        }
        return sb.toString();
    }
}
