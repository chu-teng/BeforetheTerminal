package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.config.BttConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;













public final class RoleWeights {
    private RoleWeights() {}

    private static final Random RANDOM = new Random();


    public static int weightOf(Role role) {
        if (role == null) return 0;
        Map<String, Integer> cfg = BttConfig.HANDLER.instance().roleWeights;
        if (cfg == null || cfg.isEmpty()) return 1;
        Integer w = cfg.get(role.identifier().toString());
        if (w == null) {

            w = cfg.get(role.identifier().getPath());
        }
        return w == null ? 1 : Math.max(0, w);
    }


    public static boolean hasCustomWeights() {
        Map<String, Integer> cfg = BttConfig.HANDLER.instance().roleWeights;
        if (cfg == null || cfg.isEmpty()) return false;
        for (Integer w : cfg.values()) if (w != null && w != 1) return true;
        return false;
    }


    public static String describeCustomWeights() {
        Map<String, Integer> cfg = BttConfig.HANDLER.instance().roleWeights;
        if (cfg == null || cfg.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : cfg.entrySet()) {
            if (e.getValue() == null || e.getValue() == 1) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }





    public static List<Role> pick(List<Role> pool, int count) {
        List<Role> result = new ArrayList<>();
        if (count <= 0 || pool == null || pool.isEmpty()) return result;

        List<Role> positive = new ArrayList<>();
        for (Role r : pool) {
            if (weightOf(r) > 0) positive.add(r);
        }
        List<Role> source = positive.size() >= Math.min(count, pool.size()) ? positive : pool;
        if (source.isEmpty()) source = pool;

        int take = Math.min(count, source.size());
        if (take <= 0) return result;


        int n = source.size();
        double[] keys = new double[n];
        for (int i = 0; i < n; i++) {
            double w = Math.max(weightOf(source.get(i)), 1e-9);
            double u = Math.max(RANDOM.nextDouble(), Double.MIN_VALUE);
            keys[i] = Math.pow(u, 1.0 / w);
        }
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> Double.compare(keys[b], keys[a]));
        for (int i = 0; i < take; i++) result.add(source.get(idx[i]));
        return result;
    }


    public static List<Role> shuffle(List<Role> pool) {
        List<Role> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, RANDOM);
        return copy;
    }
}
