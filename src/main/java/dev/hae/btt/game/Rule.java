package dev.hae.btt.game;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;











public enum Rule {

    SCRIPT(false),

    DRAFT(false),

    MEMORY(false),

    DEBATE(true),

    DUEL(true),

    JUDGMENT(true);

    private final boolean optional;

    Rule(boolean optional) {
        this.optional = optional;
    }

    public boolean optional() {
        return optional;
    }

    public boolean base() {
        return !optional;
    }


    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }


    public static List<Rule> displayOrder() {
        return List.of(SCRIPT, DRAFT, MEMORY, DEBATE, DUEL, JUDGMENT);
    }

    public static Rule byKey(String key) {
        if (key == null) return null;
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (Rule rule : values()) {
            if (rule.key().equals(normalized)) return rule;
        }
        return null;
    }

    public static Set<Rule> parse(String csv) {
        Set<Rule> out = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return out;
        for (String part : csv.split(",")) {
            Rule rule = byKey(part);
            if (rule != null) out.add(rule);
        }
        return out;
    }

    public static String toCsv(java.util.Collection<Rule> rules) {
        List<String> parts = new ArrayList<>();
        for (Rule rule : displayOrder()) {
            if (rules != null && rules.contains(rule)) parts.add(rule.key());
        }
        return String.join(",", parts);
    }


    public static Rule baseOf(java.util.Collection<Rule> rules) {
        if (rules == null) return null;
        for (Rule rule : displayOrder()) {
            if (rule.base() && rules.contains(rule)) return rule;
        }
        return null;
    }


    public static Set<Rule> normalize(java.util.Collection<Rule> rules) {
        Set<Rule> out = new LinkedHashSet<>();
        Rule base = baseOf(rules);
        out.add(base == null ? SCRIPT : base);
        for (Rule rule : displayOrder()) {
            if (rule.optional() && rules != null && rules.contains(rule)) out.add(rule);
        }
        return out;
    }
}
