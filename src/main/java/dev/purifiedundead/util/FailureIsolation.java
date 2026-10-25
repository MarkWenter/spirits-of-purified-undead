package dev.purifiedundead.util;

/** Only for optional presentation/integration work after core state is safe. */
public final class FailureIsolation {
    private static final java.util.Map<String, Long> LOGGED =
            new java.util.concurrent.ConcurrentHashMap<>();

    private FailureIsolation() {}

    public static void report(String feature, Throwable failure) {
        long now = System.nanoTime();
        Long previous = LOGGED.get(feature);
        if (previous == null
                || now - previous >= java.util.concurrent.TimeUnit.MINUTES.toNanos(1)) {
            LOGGED.put(feature, now);
            com.mojang.logging.LogUtils.getLogger()
                    .warn("Optional feature failed: {}", feature, failure);
        }
    }

    public static boolean run(String feature, Runnable action) {
        try {
            action.run();
            return true;
        } catch (RuntimeException | LinkageError failure) {
            report(feature, failure);
            return false;
        }
    }
}
