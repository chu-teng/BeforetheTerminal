package dev.hae.btt.record;

import java.util.Set;








public final class GameRecordTypes {
    private GameRecordTypes() {}

    public static final String MATCH_START = "match_start";
    public static final String ROLE_ASSIGNED = "role_assigned";
    public static final String SHOP_PURCHASE = "shop_purchase";
    public static final String DEATH = "death";
    public static final String TASK_COMPLETE = "task_complete";
    public static final String ITEM_USE = "item_use";
    public static final String ITEM_PICKUP = "item_pickup";

    public static final String ITEM_DROP = "item_drop";

    public static final String DRUNK = "drunk";

    public static final String EPILOGUE_START = "epilogue_start";
    public static final String PLATTER_TAKE = "platter_take";
    public static final String SKILL_USE = "skill_use";

    public static final String TALENT_USE = "talent_use";
    public static final String GLOBAL_EVENT = "global_event";
    public static final String MATCH_END = "match_end";
    public static final String PLAYER_RESULT = "player_result";
    public static final String PLAYER_JOIN = "player_join";
    public static final String PLAYER_LEAVE = "player_leave";
    public static final String PLAYER_POISONED = "player_poisoned";
    public static final String DOOR_INTERACTION = "door_interaction";
    public static final String SHIELD_BLOCKED = "shield_blocked";

    public static final String DRAFT_PICK = "draft_pick";

    public static final String ROLE_CHANGE = "role_change";

    public static final String CAMP_CHANGE = "camp_change";


    public static final Set<String> ALL = Set.of(
            MATCH_START, ROLE_ASSIGNED, SHOP_PURCHASE, DEATH, TASK_COMPLETE, ITEM_USE, ITEM_PICKUP,
            PLATTER_TAKE, SKILL_USE, TALENT_USE, GLOBAL_EVENT, MATCH_END, PLAYER_RESULT, PLAYER_JOIN, PLAYER_LEAVE,
            PLAYER_POISONED, DOOR_INTERACTION, SHIELD_BLOCKED, DRAFT_PICK,
            ITEM_DROP, DRUNK, EPILOGUE_START, ROLE_CHANGE, CAMP_CHANGE);

    public static boolean isKnown(String key) {
        return ALL.contains(key);
    }
}
