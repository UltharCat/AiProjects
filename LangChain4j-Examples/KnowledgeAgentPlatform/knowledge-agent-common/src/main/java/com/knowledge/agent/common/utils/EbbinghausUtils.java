package com.knowledge.agent.common.utils;

import java.time.LocalDate;

public class EbbinghausUtils {

    public record ReviewResult(double newEf, int newInterval, int newRepetitions, LocalDate nextReviewDate) {}

    /**
     * 计算复习后新参数（按 SM-2）
     * @param quality 回忆质量 0..5
     * @param currentEf 当前 easiness factor
     * @param currentInterval 当前间隔天数
     * @param currentRepetitions 当前连续成功次数
     * @param today 复习日（通常 LocalDate.now()）
     * @return ReviewResult 包含 newEf, newInterval, newRepetitions, nextReviewDate
     */
    public static ReviewResult compute(int quality,
                                       double currentEf,
                                       int currentInterval,
                                       int currentRepetitions,
                                       LocalDate today) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("quality must be between 0 and 5");
        }

        double ef = currentEf;
        int interval;
        int reps = currentRepetitions;

        if (quality < 3) {
            reps = 0;
            interval = 1;
        } else {
            reps = reps + 1;
            if (reps == 1) {
                interval = 1;
            } else if (reps == 2) {
                interval = 6;
            } else {
                interval = Math.max(1, (int) Math.round(currentInterval * ef));
            }
        }

        // update EF
        int qDiff = 5 - quality;
        double newEf = ef + (0.1 - qDiff * (0.08 + qDiff * 0.02));
        if (newEf < 1.3) newEf = 1.3;

        LocalDate next = today.plusDays(interval);
        return new ReviewResult(newEf, interval, reps, next);
    }

}
