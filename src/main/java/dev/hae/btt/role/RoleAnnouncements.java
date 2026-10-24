package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;

import java.util.HashMap;
import java.util.Map;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.mixin.game.RoundEndRoleMixin;













public final class RoleAnnouncements {
    private RoleAnnouncements() {}


    private static final Map<String, RoleAnnouncementTexts.RoleAnnouncementText> ENTRIES = new HashMap<>();
    public static RoleAnnouncementTexts.RoleAnnouncementText MEMORY;

    public static void init() {
        for (Role role : Roles.registeredRoles()) {
            String path = role.identifier().getPath();
            RoleAnnouncementTexts.RoleAnnouncementText entry = RoleAnnouncementTexts.registerRoleAnnouncementText(
                    new RoleAnnouncementTexts.RoleAnnouncementText("btt." + path, role.color()));
            ENTRIES.put(path, entry);
        }
        MEMORY = RoleAnnouncementTexts.registerRoleAnnouncementText(new RoleAnnouncementTexts.RoleAnnouncementText("btt.memory", 0xFFFFFF));
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] per-role announcement entries registered: {}", ENTRIES.size());
    }

    public static RoleAnnouncementTexts.RoleAnnouncementText get(String path) {
        return path == null ? null : ENTRIES.get(path);
    }


    public static RoleAnnouncementTexts.RoleAnnouncementText of(Role role) {
        return role == null ? null : ENTRIES.get(role.identifier().getPath());
    }


    private static Map<RoleAnnouncementTexts.RoleAnnouncementText, Integer> ORDER;






    public static int orderOf(RoleAnnouncementTexts.RoleAnnouncementText entry) {
        if (entry == null) return Integer.MAX_VALUE;
        if (ORDER == null) {
            Map<RoleAnnouncementTexts.RoleAnnouncementText, Integer> map = new java.util.IdentityHashMap<>();
            int index = 0;
            for (Role role : Roles.displayRoles()) map.put(ENTRIES.get(role.identifier().getPath()), index++);
            ORDER = map;
        }
        Integer index = ORDER.get(entry);
        return index == null ? Integer.MAX_VALUE : index;
    }
}
