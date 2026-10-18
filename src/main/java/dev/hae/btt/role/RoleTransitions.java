package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.network.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.network.ActionReceiver;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;


public final class RoleTransitions {
    private RoleTransitions() {}
    private static final java.util.Set<java.util.UUID> CONVERTING = new java.util.HashSet<>();
    public static boolean isConverting(net.minecraft.entity.player.PlayerEntity player) { return CONVERTING.contains(player.getUuid()); }





    public static void takeOver(ServerPlayerEntity player, Role role) {
        if (role == null) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());

        Role before = gwc.getRole(player);
        if (before != role) dev.hae.btt.advancement.BttAdvancements.changedRole(player);
        gwc.addRole(player, role);
        gwc.sync();

        boolean ownsScope = CONVERTING.add(player.getUuid());
        try { org.agmas.harpymodloader.events.ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, role); }
        finally { if (ownsScope) CONVERTING.remove(player.getUuid()); }
        if (role == Roles.EX_UNDERCOVER) {
            java.util.List<Role> free = Roles.allRoles().stream().filter(r -> Roles.kindOf(r) == Roles.Kind.ACCOMPLICE
                    && Roles.isImplemented(r) && player.getServerWorld().getPlayers().stream().noneMatch(p -> Roles.realRole(gwc, p) == r)).toList();
            if (!free.isEmpty()) {
                replace(player, free.get(player.getRandom().nextInt(free.size())), 0, Roles.CAMP_PASSENGER, false);
                PlayerState.KEY.get(player).moodOverride = "REAL";
                PlayerState.KEY.get(player).sync();
                return;
            }
        }
        reannounce(player, role);
        PlayerState pc = PlayerState.KEY.get(player);
        Role from = pc.hasPriorRole() ? Roles.byId(pc.priorRole) : before;
        boolean decoy = pc.decoyRole != null && !pc.decoyRole.isEmpty()
                && pc.decoyRole.equals(role.identifier().toString());
        dev.hae.btt.record.GameRecordManager.recordRoleChange(player,
                from == null ? "" : from.identifier().toString(), role.identifier().toString(), decoy,
                pc.hasPriorRole());

        dev.hae.btt.trait.relationship.Relationships.onCampChanged(player);
    }










    public static void replace(ServerPlayerEntity player, Role role, int ticks,
                               String campOverride, boolean stripKnife) {
        if (role == null) return;
        rememberPrior(player);
        PlayerState pc = PlayerState.KEY.get(player);
        takeOver(player, role);
        if (stripKnife) removeAll(player, dev.doctor4t.wathe.index.WatheItems.KNIFE);
        if (campOverride != null) pc.setCampOverride(campOverride);
        if (pc.hasPriorRole() && ticks > 0) pc.priorRoleTicks = ticks;
        pc.sync();
    }


    private static void removeAll(ServerPlayerEntity player, net.minecraft.item.Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(item)) {
                player.getInventory().setStack(i, net.minecraft.item.ItemStack.EMPTY);
            }
        }
    }


    private static void rememberPrior(ServerPlayerEntity player) {
        PlayerState pc = PlayerState.KEY.get(player);
        if (pc.hasPriorRole()) return;
        Role current = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (current == null) return;
        pc.setPriorRole(current.identifier().toString(), 0);
    }


    public static void tick(ServerPlayerEntity player, PlayerState pc) {
        if (pc.priorRoleTicks > 0 && --pc.priorRoleTicks <= 0) restorePrior(player, pc);
    }


    private static void restorePrior(ServerPlayerEntity player, PlayerState pc) {
        Role prior = Roles.byId(pc.priorRole);
        pc.moodOverride = "";
        pc.clearPriorRole();
        if (prior == null) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        gwc.addRole(player, prior);
        gwc.sync();
        reannounce(player, prior);

        dev.hae.btt.trait.relationship.Relationships.onCampChanged(player);
    }







    public static java.util.Map<java.util.UUID, Role> swapToOriginalRoles(net.minecraft.server.world.ServerWorld world) {
        java.util.Map<java.util.UUID, Role> snapshot = new java.util.HashMap<>();
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        for (ServerPlayerEntity player : world.getPlayers()) {
            PlayerState pc = PlayerState.KEY.get(player);
            if (!pc.hasPriorRole()) continue;
            Role prior = Roles.byId(pc.priorRole);
            Role replaced = gwc.getRole(player);
            if (prior == null || replaced == null || replaced == prior) continue;
            snapshot.put(player.getUuid(), replaced);
            gwc.addRole(player, prior);
        }
        return snapshot;
    }


    public static void swapBackToReplacedRoles(net.minecraft.server.world.ServerWorld world,
                                              java.util.Map<java.util.UUID, Role> snapshot) {
        if (snapshot.isEmpty()) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        for (java.util.Map.Entry<java.util.UUID, Role> e : snapshot.entrySet()) {
            ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(e.getKey());
            if (player == null) continue;
            gwc.addRole(player, e.getValue());
        }
    }


    public static void reannounce(ServerPlayerEntity player, Role role) {
        if (dev.hae.btt.encounter.Memory.isBlind(player)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        int killers = 0;
        int passengers = 0;
        for (Role r : gwc.getRoles().values()) {
            if (r.canUseKiller()) killers++;
            else if (r.isInnocent()) passengers++;
        }
        ServerPlayNetworking.send(player, new AnnounceWelcomePayload(
                BeforeTheTerminalGameMode.announcementIndex(role), killers, passengers));
    }


    public static Text tookOverText(Role role) {
        return Text.translatable("btt.action.role.took_over",
                SeatAssignment.displayName(role).getString()).withColor(role.color());
    }
}
