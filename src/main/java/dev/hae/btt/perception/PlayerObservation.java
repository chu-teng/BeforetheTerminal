package dev.hae.btt.perception;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.encounter.Memory;
import dev.hae.btt.role.Roles;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.Voodoo;
import dev.hae.btt.trait.Traits;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;


public final class PlayerObservation {
    private PlayerObservation() {}
    private static final int GREEN = 0x4EDD35;

    public static void append(NbtCompound view, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        var game = GameWorldComponent.KEY.get(target.getWorld());
        view.putInt("observedInstinctColor", instinctColor(game, target, viewer));
        view.putInt("observedPassiveColor", passiveColor(game, target, viewer));
        boolean chair = viewer.getVehicle() instanceof dev.doctor4t.wathe.block.entity.SeatEntity seat
                && viewer.getWorld().getBlockState(seat.getBlockPos()).getBlock() == dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR;
        var role = game.getRole(target);
        boolean immune = (role == Roles.SHOUJO || role == Roles.AVIATEUR) && !Drunk.isDrunk(target);
        view.putBoolean("observedLookout", chair && game.isRunning() && !Voodoo.isRemnant(viewer)
                && GameFunctions.isPlayerAliveAndSurvival(target) && viewer.canSee(target) && !immune);
        view.putString("observedVoters", dev.hae.btt.game.Debate.isVoting(viewer.getWorld())
                && isLookingAt(target, viewer) ? dev.hae.btt.game.Debate.votersFor(viewer.getServerWorld(), target.getUuid()) : "");
        boolean looking = canInspect(target, viewer);
        view.putString("observedGroup", looking ? group(game, target, viewer) : "");
        view.putString("observedCohort", looking ? cohort(game, target, viewer) : "");
        boolean profiling = Traits.has(viewer, Traits.PROFILING) && Roles.kindOf(Roles.realRole(game, target)) == Roles.Kind.LONE;
        Role inspected = looking && (game.isRole(viewer, Roles.AGENT) || profiling)
                ? inspectRole(game, target, viewer) : null;
        view.putString("observedInspectRole", inspected == null ? "" : inspected.identifier().toString());
    }

    public static int passiveColor(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (!game.isRunning() || Voodoo.isRemnant(viewer) || Memory.isBlind(viewer) || target.isSpectator()) return -1;
        var poison = dev.doctor4t.wathe.cca.PlayerPoisonComponent.KEY.get(target);
        if (game.isRole(viewer, Roles.PHARMACIST) && poison.poisonTicks > 0) return 0xFFFF0000;
        if (game.isRole(viewer, Roles.SUCCUBUS) && poison.poisonTicks > 0 && viewer.getUuid().equals(poison.poisoner))
            return Roles.SUCCUBUS.color();
        if (game.isRole(viewer, Roles.NIGHT_WATCHMAN)
                && dev.doctor4t.wathe.cca.PlayerPsychoComponent.KEY.get(target).getPsychoTicks() > 0)
            return Roles.NIGHT_WATCHMAN.color();
        if (game.isRole(viewer, Roles.EXECUTIONER)
                && target.getUuid().equals(dev.hae.btt.component.ExecutionerState.KEY.get(viewer).target)) return 0xFFFFFF00;
        return -1;
    }

    public static int instinctColor(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (!game.isRunning() || Voodoo.isRemnant(viewer) || Memory.isBlind(viewer)) return -1;
        if (NativeRoleView.fullObserver(viewer)) {
            Role role = Roles.realRole(game, target);
            return GameFunctions.isPlayerAliveAndSurvival(target) && role != null ? role.color() : -1;
        }
        if (game.isRole(viewer, Roles.WEREWOLF)) return -1;
        if (dev.hae.btt.role.Werewolf.member(viewer)) {
            return GameFunctions.isPlayerAliveAndSurvival(target) && !game.isRole(target, Roles.WEREWOLF)
                    && dev.hae.btt.role.Werewolf.samePack(viewer, target) ? Roles.WEREWOLF.color() : -1;
        }
        if (game.isRole(viewer, Roles.VOODOO) && PlayerState.KEY.get(target).voodooPhase == 1)
            return Roles.VOODOO.color();
        if (!GameFunctions.isPlayerAliveAndSurvival(target)
                || Roles.hasCurrentOrPriorRole(game, viewer, Roles.TRAITOR)
                || Roles.hasCurrentOrPriorRole(game, viewer, Roles.MARIONETTE)) return -1;
        var own = PlayerState.KEY.get(viewer);
        var subject = PlayerState.KEY.get(target);
        if (game.isRole(viewer, Roles.COMEDIAN) && subject.voicePitchTicks > 0) return Roles.COMEDIAN.color();
        boolean messiahFollower = game.isRole(viewer, Roles.MESSIAH) || own.isMessiahFollower();
        boolean majo = game.isRole(viewer, Roles.MAJO) || own.isMajoRecruit();
        if (messiahFollower && (game.isRole(target, Roles.MESSIAH) || subject.isMessiahFollower())) return 0xFFFF00FF;
        if (majo && (game.isRole(target, Roles.MAJO) || subject.isMajoRecruit())) return Roles.MAJO.color();
        if ((messiahFollower || majo) && !Roles.isKillerCampFor(game, viewer)) return -1;
        if (game.isRole(viewer, Roles.ARSONIST) && GameFunctions.isPlayerAliveAndSurvival(viewer)
                && subject.isDoused()) return Roles.ARSONIST.color();
        if (!GameFunctions.isPlayerAliveAndSurvival(viewer)
                || !Roles.isKillerCampFor(game, viewer) && !Roles.hasCurrentOrPriorRole(game, viewer, Roles.LUNATIC)) return -1;
        Role role = game.getRole(target);
        if (role == null) return -1;
        if ((role == Roles.SHOUJO || role == Roles.AVIATEUR) && !Drunk.isDrunk(target)) return -1;
        if (Roles.hasCurrentOrPriorRole(game, viewer, Roles.LUNATIC)
                && Roles.kindOfReal(game, target) == Roles.Kind.PRINCIPAL) return GREEN;
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.MARIONETTE) && Roles.isKillerCampFor(game, viewer))
            return Roles.MARIONETTE.color();
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.LUNATIC) && Roles.isKillerCampFor(game, viewer))
            return Roles.LUNATIC.color();
        for (var other : viewer.getServerWorld().getPlayers())
            if (other != viewer && other.isAlive() && game.isRole(other, Roles.POPPY_GROWER) && !Drunk.isDrunk(other))
                return GREEN;
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.TRAITOR)) return GREEN;
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.BLACKDEATH)) return Roles.BLACKDEATH.color();
        if (Roles.isKillerCampFor(game, target))
            return Traits.has(viewer, Traits.PREMEDITATION)
                    ? role == Roles.UNDERCOVER ? undercoverColor(game, target) : Roles.realRole(game, target).color() : 0xDD0000;
        if (Roles.campOf(game, target) == Roles.Faction.OUTSIDER && Roles.factionOf(role) != Roles.Faction.OUTSIDER)
            return 0xFF00FF;
        return role == Roles.UNDERCOVER
                ? Traits.has(viewer, Traits.PREMEDITATION) ? undercoverColor(game, target) : 0xDD0000 : GREEN;
    }

    private static int undercoverColor(GameWorldComponent game, ServerPlayerEntity target) {
        var present = new java.util.HashSet<String>(dev.hae.btt.component.GameState.KEY.get(target.getWorld()).openingRoles.values());
        var absent = Roles.allRoles().stream().filter(r -> Roles.kindOf(r) == Roles.Kind.ACCOMPLICE)
                .filter(r -> !present.contains(r.identifier().toString()))
                .filter(r -> !game.getRoles().containsValue(r)).toList();
        if (absent.isEmpty()) return 0xDD0000;
        var random = new java.util.Random(target.getUuid().getMostSignificantBits() ^ target.getUuid().getLeastSignificantBits());
        return absent.get(random.nextInt(absent.size())).color();
    }

    private static boolean canInspect(ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (viewer == target || Voodoo.isRemnant(viewer) || Memory.isBlind(viewer)) return false;
        var game = GameWorldComponent.KEY.get(viewer.getWorld());
        if (!game.isRunning() || target.getWorld() != viewer.getWorld() || PlayerState.KEY.get(target).isFakeDead()) return false;
        return isLookingAt(target, viewer);
    }

    private static boolean isLookingAt(ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (target == viewer || target.getWorld() != viewer.getWorld()) return false;
        double range = GameFunctions.isPlayerSpectatingOrCreative(viewer) ? 8 : 2;
        if (viewer.squaredDistanceTo(target) > (range + 1) * (range + 1)) return false;
        var hit = ProjectileUtil.getCollision(viewer, entity -> entity instanceof PlayerEntity, range);
        return hit instanceof EntityHitResult entityHit && entityHit.getEntity() == target;
    }

    private static String cohort(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (!Roles.isKillerCampFor(game, viewer) || Roles.hasCurrentOrPriorRole(game, viewer, Roles.MARIONETTE) || Roles.hasCurrentOrPriorRole(game, viewer, Roles.TRAITOR)
                || Roles.hasCurrentOrPriorRole(game, viewer, Roles.LUNATIC)
                || Roles.hasCurrentOrPriorRole(game, target, Roles.TRAITOR)) return "";
        Role role = game.getRole(target);
        if (role == null) return "";
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.MARIONETTE)) return "btt.hud.cohort.marionette";
        boolean lunatic = Roles.hasCurrentOrPriorRole(game, target, Roles.LUNATIC);
        if (role == Roles.UNDERCOVER) return "btt.hud.cohort.accomplice";
        if (!Roles.isKillerCampFor(game, target) && !lunatic) return "";
        if (lunatic) return "btt.hud.cohort.lunatic";
        if (Roles.isKillerCampFor(game, target) && role != Roles.TRAITOR
                && !Roles.isKillerSeatFor(game, target) && role != Roles.BLACKDEATH) return "btt.hud.cohort.fellow";
        if (role == Roles.BLACKDEATH) return "btt.hud.cohort.black_death";
        if (Roles.kindOf(role) == Roles.Kind.PRINCIPAL) return "btt.hud.cohort.principal";
        if (Roles.kindOf(role) == Roles.Kind.ACCOMPLICE) return "btt.hud.cohort.accomplice";
        return "";
    }

    private static String group(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        var own = PlayerState.KEY.get(viewer);
        if (!game.isRole(viewer, Roles.NOBLE)) return "";
        boolean drunk = Drunk.isDrunk(viewer);
        if (dev.hae.btt.role.RoleGroups.inGroup(drunk ? own.drunkKins : own.kins, target.getUuid())) return "btt.hud.kins";
        if (dev.hae.btt.role.RoleGroups.inGroup(drunk ? own.drunkBranchKins : own.branchKins, target.getUuid())) return "btt.hud.branch_kins";
        return "";
    }

    private static Role inspectRole(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        Role role = game.getRole(target);
        if (role == null) return null;
        if (role == Roles.HERETIC) {
            var passengers = WatheRoles.ROLES.stream().filter(r -> Roles.kindOf(r) != null && Roles.kindOf(r).isCivilian()).toList();
            role = passengers.isEmpty() ? Roles.DETECTIVE : passengers.get(Math.floorMod(target.getUuid().hashCode(), passengers.size()));
        }
        if (Drunk.isDrunk(viewer) && !WatheRoles.ROLES.isEmpty()) {
            long seed = target.getUuid().getMostSignificantBits() ^ target.getUuid().getLeastSignificantBits();
            role = WatheRoles.ROLES.get(Math.floorMod(new java.util.Random(seed).nextInt(), WatheRoles.ROLES.size()));
        }
        return role;
    }
}
