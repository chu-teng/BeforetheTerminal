package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(WatheClient.class)
public abstract class ShoujoInstinctMixin {


    private static final int INSTINCT_ITEM_GOLD = 0xDB9D00;
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void btt$instinctOverrides(Entity target, CallbackInfoReturnable<Integer> cir) {
        var local = MinecraftClient.getInstance().player;
        if (local != null && dev.hae.btt.role.Voodoo.isRemnant(local)) { btt$set(cir, -1); return; }
        if (local != null && target instanceof PlayerEntity spirit
                && dev.hae.btt.component.PlayerState.KEY.get(spirit).voodooPhase == 1
                && GameWorldComponent.KEY.get(local.getWorld()).getRole(local) == Roles.VOODOO
                && btt$instinctKeyPressed()) { btt$set(cir, Roles.VOODOO.color()); return; }

        if (!(target instanceof PlayerEntity p) || p.isSpectator()) {
            if (btt$thiefCorpses(target, cir)) return;
            if (btt$corpseSight(target, cir)) return;
            btt$gardenerFlowers(target, cir);
            return;
        }
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null || viewer == p) return;
        if (!SeatAssignment.isBttMode(viewer.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(viewer.getWorld());


        if (Roles.isPlayingAs(gwc, viewer, Roles.TRAITOR) && !dev.hae.btt.component.PlayerState.KEY.get(viewer).werewolfAlly) {
            cir.setReturnValue(-1);
            cir.cancel();
            return;
        }

        if (btt$markerGlow(viewer, p, gwc, cir)) return;


        if (btt$letterGlowSpark(viewer, p, cir)) return;


        if (GameFunctions.isPlayerSpectatingOrCreative(viewer)) return;
        btt$set(cir, btt$instinctKeyPressed()
                ? dev.hae.btt.component.PlayerState.KEY.get(p).observedInstinctColor : -1);
    }


    private static boolean btt$thiefCorpses(Entity target, CallbackInfoReturnable<Integer> cir) {
        if (!(target instanceof dev.doctor4t.wathe.entity.PlayerBodyEntity)) return false;
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null) return false;
        if (!SeatAssignment.isBttMode(viewer.getWorld())) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(viewer.getWorld());
        if (gwc.getRole(viewer) != Roles.THIEF) return false;
        if (!GameFunctions.isPlayerAliveAndSurvival(viewer)) return false;
        if (!btt$instinctKeyPressed()) return false;
        cir.setReturnValue(Roles.THIEF.color());
        cir.cancel();
        return true;
    }






    @Inject(method = "isInstinctEnabled", at = @At("RETURN"), cancellable = true)
    private static void btt$noInstinctForKeyRoles(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null || viewer.getWorld() == null) return;
        if (!SeatAssignment.isBttMode(viewer.getWorld())) return;

        if (GameFunctions.isPlayerSpectatingOrCreative(viewer)) {
            return;
        }
        if (Roles.isPlayingAs(GameWorldComponent.KEY.get(viewer.getWorld()), viewer, Roles.WEREWOLF)
                || Roles.isPlayingAs(GameWorldComponent.KEY.get(viewer.getWorld()), viewer, Roles.TRAITOR)
                    && !dev.hae.btt.component.PlayerState.KEY.get(viewer).werewolfAlly) {
            cir.setReturnValue(false);
        }
    }

    private static boolean btt$instinctKeyPressed() {




        var kb = WatheClient.instinctKeybind;
        boolean raw = kb != null && ((dev.hae.btt.client.mixin.game.KeyBindingAccessor) (Object) kb).btt$isPressedRaw();
        return dev.hae.btt.client.InstinctMode.active(raw);
    }






    private static boolean btt$markerGlow(PlayerEntity viewer, PlayerEntity target, GameWorldComponent gwc,
                                          CallbackInfoReturnable<Integer> cir) {
        if (!GameFunctions.isPlayerAliveAndSurvival(viewer)) return false;
        dev.hae.btt.component.PlayerState own =
                dev.hae.btt.component.PlayerState.KEY.get(viewer);
        String uuid = target.getUuidAsString();

        if (gwc.isRole(viewer, Roles.JOURNALIST) && !own.markedTarget.isEmpty()
                && own.markedTarget.equals(uuid)) {
            return btt$set(cir, Roles.JOURNALIST.color());
        }


        if (own.folkTicks > 0 && !own.folkTarget.isEmpty() && own.folkTarget.equals(uuid)
                && dev.hae.btt.client.SightGate.canSee(target)) {
            return btt$set(cir, Roles.FOLKLORIST.color());
        }

        if (Roles.isPassengerCampFor(gwc, viewer)
                && dev.hae.btt.component.PlayerState.KEY.get(target).starGlowTicks > 0) {
            return btt$set(cir, Roles.STAR.color());
        }

        if (own.engineerScanTicks > 0) {
            return btt$set(cir, Roles.ENGINEER.color());
        }
        if (own.thiefRevealTicks > 0) {
            return btt$set(cir, Roles.THIEF.color());
        }




        if (gwc.isRole(viewer, Roles.MAID)
                && btt$needCount(target) >= 3
                && dev.hae.btt.client.SightGate.canSee(target)) {
            return btt$set(cir, Roles.MAID.color());
        }
        return false;
    }


    private static boolean btt$letterGlowSpark(PlayerEntity viewer, PlayerEntity target,
                                               CallbackInfoReturnable<Integer> cir) {
        if (!dev.hae.btt.client.SightGate.canSee(target)) return false;
        if (!btt$holdsLetter(viewer)) return false;
        dev.hae.btt.component.LetterState state = dev.hae.btt.component.LetterState.KEY.get(viewer);
        if (state == null) return false;
        String mark = state.marks.get(target.getUuidAsString());
        if (mark == null || mark.isEmpty()) return false;
        int camp = Roles.campColor(mark);
        if (camp != -1) return btt$set(cir, camp);
        Role role = Roles.resolve(mark);
        return role != null && btt$set(cir, role.color());
    }


    private static boolean btt$holdsLetter(PlayerEntity viewer) {
        return viewer.getMainHandStack().isOf(dev.doctor4t.wathe.index.WatheItems.LETTER)
                || viewer.getOffHandStack().isOf(dev.doctor4t.wathe.index.WatheItems.LETTER);
    }


    private static int btt$needCount(net.minecraft.entity.player.PlayerEntity target) {
        var st = dev.hae.btt.component.NeedsState.KEY.get(target);
        if (st == null || st.kinds == null || st.kinds.isEmpty()) return 0;
        int count = 1;
        for (int i = 0; i < st.kinds.length(); i++) {
            if (st.kinds.charAt(i) == ',') count++;
        }
        return count;
    }


    private static boolean btt$set(CallbackInfoReturnable<Integer> cir, int color) {
        cir.setReturnValue(color);
        cir.cancel();
        return true;
    }












    private static boolean btt$corpseSight(Entity target, CallbackInfoReturnable<Integer> cir) {
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null) return false;
        if (!SeatAssignment.isBttMode(viewer.getWorld())) return false;
        if (!btt$instinctKeyPressed()) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(viewer.getWorld());
        if (!GameFunctions.isPlayerAliveAndSurvival(viewer)) return false;

        if (gwc.getRole(viewer) == Roles.COLLECTOR) {
            boolean items = target instanceof ItemEntity
                    || target instanceof dev.doctor4t.wathe.entity.NoteEntity;
            cir.setReturnValue(items ? Roles.COLLECTOR.color() : -1);
            cir.cancel();
            return true;
        }

        if (gwc.getRole(viewer) == Roles.MORTICIAN) {
            boolean body = target instanceof dev.doctor4t.wathe.entity.PlayerBodyEntity;
            cir.setReturnValue(body ? Roles.MORTICIAN.color() : -1);
            cir.cancel();
            return true;
        }
        if (gwc.getRole(viewer) != Roles.FOLKLORIST) return false;

        cir.setReturnValue(-1);
        cir.cancel();
        return true;
    }

    private static boolean btt$gardenerFlowers(Entity target, CallbackInfoReturnable<Integer> cir) {
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null) return false;
        if (!SeatAssignment.isBttMode(viewer.getWorld())) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(viewer.getWorld());
        if (gwc.getRole(viewer) != Roles.GARDENER) return false;
        if (!GameFunctions.isPlayerAliveAndSurvival(viewer)) return false;
        cir.setReturnValue(target instanceof dev.hae.btt.entity.BloomEntity || target instanceof ItemEntity item
                && dev.hae.btt.role.Flowers.isFlower(item.getStack()) ? INSTINCT_ITEM_GOLD : -1);
        cir.cancel();
        return true;
    }
}
