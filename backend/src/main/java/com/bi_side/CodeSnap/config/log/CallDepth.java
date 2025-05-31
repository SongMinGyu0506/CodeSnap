package com.bi_side.CodeSnap.config.log;

public class CallDepth {
    private static final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);

    public static void increase() {
        depth.set(depth.get() + 1);
    }

    public static void decrease() {
        depth.set(depth.get() - 1);
    }

    public static int get() {
        return depth.get();
    }

    public static void clear() {
        depth.remove();
    }

    public static String enterPrefix() {
        return "|" + "----".repeat(depth.get()) + ">";
    }

    public static String returnPrefix() {
        return "|<" + "----".repeat(depth.get());
    }

    public static String exceptionPrefix() {
        return "|<X" + "---".repeat(depth.get()); // 예외는 `<X---` 형태
    }
}
