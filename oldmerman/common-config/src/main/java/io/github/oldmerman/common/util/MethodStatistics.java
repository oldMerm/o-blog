package io.github.oldmerman.common.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MethodStatistics {

    private final ConcurrentHashMap<String, MethodStats> statsMap = new ConcurrentHashMap<>();

    public void record(String methodName, long duration) {
        statsMap.computeIfAbsent(methodName, k -> new MethodStats())
                .update(duration);
    }

    public MethodStats getStats(String methodName) {
        return statsMap.get(methodName);
    }

    public static class MethodStats {
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);

        public void update(long duration) {
            totalTime.addAndGet(duration);
            count.incrementAndGet();

            // 更新最大值
            maxTime.updateAndGet(current -> Math.max(current, duration));
            // 更新最小值
            minTime.updateAndGet(current -> Math.min(current, duration));
        }

        public double getAverageTime() {
            long c = count.get();
            return c > 0 ? (double) totalTime.get() / c : 0;
        }

        // getter methods...
        public long getCount() {
            return count.get();
        }
    }
}