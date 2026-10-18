package dev.hae.btt.record.replay;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;





public final class ReplayRegistry {

    private static final Map<String, ReplayEventFormatter> BY_EVENT_KEY = new HashMap<>();
    private static final Map<Identifier, ReplayEventFormatter> BY_SKILL = new HashMap<>();
    private static final Map<Identifier, ReplayEventFormatter> BY_GLOBAL_EVENT = new HashMap<>();

    private ReplayRegistry() {}

    public static void onEvent(String eventKey, ReplayEventFormatter formatter) {
        BY_EVENT_KEY.put(eventKey, formatter);
    }

    public static void onSkill(Identifier skillId, ReplayEventFormatter formatter) {
        BY_SKILL.put(skillId, formatter);
    }

    public static void onGlobalEvent(Identifier eventId, ReplayEventFormatter formatter) {
        BY_GLOBAL_EVENT.put(eventId, formatter);
    }

    @Nullable
    public static ReplayEventFormatter skill(Identifier skillId) {
        return BY_SKILL.get(skillId);
    }

    @Nullable
    public static ReplayEventFormatter globalEvent(Identifier eventId) {
        return BY_GLOBAL_EVENT.get(eventId);
    }

    @Nullable
    public static ReplayEventFormatter eventKey(String eventKey) {
        return BY_EVENT_KEY.get(eventKey);
    }
}
