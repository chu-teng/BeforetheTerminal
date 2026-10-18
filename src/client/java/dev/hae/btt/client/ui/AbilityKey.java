package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.client.mixin.game.SelectScreenMixin;
import dev.hae.btt.network.ActionReceiver;
import dev.hae.btt.network.AbilityC2SPacket;
import dev.hae.btt.network.DisguiseC2SPacket;









public final class AbilityKey {
    private AbilityKey() {}


    private static final double NEARBY_RANGE = 6.0;


    public static void handlePress(MinecraftClient client) {
        openSelection(client);
    }





    public static boolean tryDebateVote(MinecraftClient client) {
        var player = client.player;
        if (player == null || client.world == null) return false;
        if (dev.hae.btt.component.DebateState.KEY.get(client.world).phase
                != dev.hae.btt.component.DebateState.PHASE_VOTING) return false;
        var target = aimedPlayer(client);
        if (target != null) {
            player.playSound(dev.hae.btt.registry.Sounds.UI_KEY_G, 1.0F, 1.0F);
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.DebateVoteC2SPacket(target.getUuid()));
        }
        return true;
    }

    private static void openSelection(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(client.world)) return;
        var gwc = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(client.world);
        if (!gwc.isRunning()) return;

        RoleDef def = RoleDefs.get(Roles.effectiveRole(gwc, client.player));
        if (def == null) {



            if (Roles.effectiveRole(gwc, client.player) == Roles.SHOUJO) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new dev.hae.btt.network.SelectC2SPacket(client.player.getUuid(), ""));
            }
            return;
        }
        if (def.hasSelf()) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.SelectC2SPacket(client.player.getUuid(), ""));
            return;
        }
        if (def.hasRoleChoice()) {
            RolePickScreen.openForAbility(client, def.role, client.player.getUuid(), null);
            return;
        }
        if (def.role == Roles.AMNESIAC || def.role == Roles.CANNIBAL || def.role == Roles.THIEF) {
            var body = dev.hae.btt.ability.CorpseTargets.aimed(client.player);
            if (body == null) return;
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.CorpseActionC2SPacket(body.getUuid(), 1));
            return;
        }


        if (def.role == Roles.GARDENER || def.role == Roles.ENGINEER
                || def.role == Roles.ARCHITECT || def.role == Roles.LOCKSMITH
                || def.role == Roles.ARSONIST || def.role == Roles.ACTOR || def.role == Roles.SHOUJO
                || def.role == Roles.PROFESSOR
                || def.role == Roles.COLLECTOR
                || def.role == Roles.CLEANER) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.SelectC2SPacket(client.player.getUuid(), ""));
            return;
        }

        if (def.role == Roles.STOWAWAY) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.AbilityC2SPacket());
            return;
        }

        if (def.role == Roles.MUYUUBYOU || def.role == Roles.ASTRAL) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.SelectC2SPacket(client.player.getUuid(), ""));
            return;
        }


        if (def.role == Roles.PHILOSOPHER) {
            dev.hae.btt.client.ui.RolePickScreen.openForAbility(client, def.role, client.player.getUuid(), null);
            return;
        }


        if (def.role == Roles.BARTENDER) {
            dev.hae.btt.client.ui.RolePickScreen.openForAbility(client, def.role, client.player.getUuid(), null);
            return;
        }

        if (isNearbyRole(def.role)) {
            sendAimed(client);
            return;
        }

    }


    public static boolean isNearbyRole(dev.doctor4t.wathe.api.Role role) {
        return RoleDefs.isNearbySkill(role);
    }


    public static boolean hasTip(dev.doctor4t.wathe.api.Role role) {

        return (RoleDefs.get(role) != null && (RoleDefs.get(role).hasRoleChoice() || RoleDefs.get(role).hasSelf())) || isNearbyRole(role) || role == Roles.MUYUUBYOU
                || role == Roles.GARDENER || role == Roles.AMNESIAC
                || role == Roles.ARCHITECT || role == Roles.LOCKSMITH || role == Roles.ENGINEER

                || role == Roles.BARTENDER
                || role == Roles.ARSONIST || role == Roles.ACTOR || role == Roles.SHOUJO || role == Roles.CANNIBAL
                || role == Roles.PROFESSOR


                || role == Roles.PHILOSOPHER
                || role == Roles.COLLECTOR
                || role == Roles.ASTRAL
                || role == Roles.CLEANER;

    }


    public static boolean isAnyRole(dev.doctor4t.wathe.api.Role role) {
        return RoleDefs.isAnyTarget(role);
    }






    public static boolean isPickRole(dev.doctor4t.wathe.api.Role role) {
        return dev.hae.btt.role.GuessPool.isPickRole(role);
    }


    public static boolean isMorphRole(dev.doctor4t.wathe.api.Role role) {
        return role == Roles.ACTOR;
    }





    public static boolean isMultiPick(dev.doctor4t.wathe.api.Role role) {
        return role == Roles.ACTOR || role == Roles.JUDGE || role == Roles.RIGGER || role == Roles.MAGICIAN
                || role == Roles.PUPPETEER
                || role == Roles.PROPHET
                || role == Roles.TAILOR;
    }






    private static void sendAimed(MinecraftClient client) {
        var player = client.player;
        if (player == null) return;
        var hit = net.minecraft.entity.projectile.ProjectileUtil.getCollision(player,
                e -> e instanceof net.minecraft.entity.player.PlayerEntity && e != player, NEARBY_RANGE);
        if (hit instanceof net.minecraft.util.hit.EntityHitResult ehr
                && ehr.getEntity() instanceof net.minecraft.entity.player.PlayerEntity target) {

            player.playSound(dev.hae.btt.registry.Sounds.UI_KEY_G, 1.0F, 1.0F);
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new dev.hae.btt.network.SelectC2SPacket(target.getUuid(), ""));
        }
    }


    public static net.minecraft.entity.player.PlayerEntity aimedPlayer(MinecraftClient client) {
        var player = client.player;
        if (player == null) return null;
        var hit = net.minecraft.entity.projectile.ProjectileUtil.getCollision(player,
                e -> e instanceof net.minecraft.entity.player.PlayerEntity && e != player, NEARBY_RANGE);
        return hit instanceof net.minecraft.util.hit.EntityHitResult ehr
                && ehr.getEntity() instanceof net.minecraft.entity.player.PlayerEntity target ? target : null;
    }

    public static net.minecraft.text.Text aimedName(MinecraftClient client) {
        var target = aimedPlayer(client);
        return target == null ? net.minecraft.text.Text.translatable("btt.hud.target_them") : target.getName();
    }


    public static boolean hasSelectionUi() {
        var p = MinecraftClient.getInstance().player;
        if (p == null || MinecraftClient.getInstance().world == null) return false;
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(MinecraftClient.getInstance().world)) return false;
        var gwc = GameWorldComponent.KEY.get(MinecraftClient.getInstance().world);
        if (!gwc.isRunning()) return false;
        RoleDef def = RoleDefs.get(Roles.effectiveRole(gwc, p));
        return def != null && isUiRole(def.role);
    }


    public static boolean isUiRole(dev.doctor4t.wathe.api.Role role) {
        return isAnyRole(role);
    }


    public static boolean isInstant(dev.doctor4t.wathe.api.Role role) {
        return RoleDefs.isImmediateTarget(role);
    }
}
