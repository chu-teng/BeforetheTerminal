package dev.hae.btt.rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;









public final class RoleRotation {

    private static final double MIN_WEIGHT = 1.0e-6;

    private RoleRotation() {}


    public static double imbalance(Iterable<GameEntry> window, RoleCategory category) {
        double expected = 0.0;
        int landed = 0;
        for (GameEntry entry : window) {
            expected += entry.shareOf(category);
            if (entry.bucket() == category) landed++;
        }
        return expected - landed;
    }


    public static double weightOf(double imbalance, RotationStrength strength) {
        if (!strength.enabled()) return 1.0;
        double raw = Math.exp(strength.gain() * imbalance);
        return Math.max(strength.floor(), Math.min(strength.ceiling(), raw));
    }





    public static <P> List<P> drawWeighted(List<P> pool, ToDoubleFunction<P> weightOf,
                                           int count, DoubleSupplier uniform01) {
        List<P> remaining = new ArrayList<>(pool);
        int take = Math.max(0, Math.min(count, remaining.size()));
        List<P> drawn = new ArrayList<>(take);
        for (int n = 0; n < take; n++) {
            double total = 0.0;
            for (P candidate : remaining) {
                total += Math.max(weightOf.applyAsDouble(candidate), MIN_WEIGHT);
            }
            double target = uniform01.getAsDouble() * total;
            int chosen = remaining.size() - 1;
            double walked = 0.0;
            for (int i = 0; i < remaining.size(); i++) {
                walked += Math.max(weightOf.applyAsDouble(remaining.get(i)), MIN_WEIGHT);
                if (target < walked) {
                    chosen = i;
                    break;
                }
            }
            drawn.add(remaining.remove(chosen));
        }
        return drawn;
    }





    public static <R> R drawAvoidingRecent(List<R> options, Predicate<R> recent,
                                           DoubleSupplier uniform01) {
        if (options.isEmpty()) return null;
        List<R> fresh = new ArrayList<>(options.size());
        for (R option : options) {
            if (!recent.test(option)) fresh.add(option);
        }
        List<R> pool = fresh.isEmpty() ? options : fresh;
        int index = (int) Math.floor(uniform01.getAsDouble() * pool.size());
        if (index < 0) index = 0;
        if (index >= pool.size()) index = pool.size() - 1;
        return pool.get(index);
    }
}
