package dev.hae.btt.combat;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.encounter.EncounterRules;
import dev.hae.btt.role.ColdWave;
import dev.hae.btt.role.Roles;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.trait.Traits;
import net.minecraft.sound.SoundCategory;
import dev.hae.btt.role.Guard;
import dev.hae.btt.game.SeatAssignment;
import java.util.ArrayList;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.BlackDeath;

import dev.hae.btt.role.Imp;


public final class DeathRules {
    private DeathRules() {}


    public static void onConfirmedDeath(ServerPlayerEntity dead, Identifier reason, boolean spawnBody) {
        var world = dead.getServerWorld();
        var gwc = GameWorldComponent.KEY.get(world);
        if (!SeatAssignment.isBttMode(world) || !gwc.isRunning()) return;
        if (dead.getWorld() instanceof net.minecraft.server.world.ServerWorld philosopherWorld) {
            for (ServerPlayerEntity bearer : philosopherWorld.getPlayers()) {
                PlayerState state = PlayerState.KEY.get(bearer);
                if (state.philosopherSources.isEmpty()) continue;
                var sources = new java.util.ArrayList<>(java.util.Arrays.asList(state.philosopherSources.split(",")));
                if (sources.remove(dead.getUuidAsString())) {
                    state.philosopherSources = String.join(",", sources);
                    state.sync();
                }
            }
        }
        ColdWave.onConfirmedDeath(dead, spawnBody);
        EncounterRules.confirmedDeath(dead, reason);
        playConfirmedDeathSounds(dead, reason);


        if (gwc.isRole(dead, dev.hae.btt.role.Roles.STAR)
                && !PlayerState.KEY.get(dead).isDrunk()) {



            String starMark = PlayerState.KEY.get(dead).starMark;
            if (!starMark.isEmpty()) {
                try {
                    if (world.getPlayerByUuid(java.util.UUID.fromString(starMark)) instanceof ServerPlayerEntity markedStar
                            && markedStar.isAlive() && GameFunctions.isPlayerAliveAndSurvival(markedStar)) {
                        PlayerState.KEY.get(markedStar).starGlowTicks = 20 * 30;
                        PlayerState.KEY.get(markedStar).sync();
                    }
                } catch (IllegalArgumentException ignored) {

                }
            }
            for (ServerPlayerEntity p : world.getPlayers()) {

                if (dev.hae.btt.role.Roles.isPassengerCampFor(gwc, p)) {

                    p.sendMessage(net.minecraft.text.Text.translatable("btt.action.star.died")
                            .withColor(dev.hae.btt.role.Roles.STAR.color()), true);
                }
            }
        }


        boolean obstinateVictim = Traits.has(dead, Traits.OBSTINATE);
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p != dead && Traits.has(p, Traits.SENSITIVE)) {
                p.sendMessage(Text.translatable("btt.trait.sensitive.death")
                        .withColor(Traits.SENSITIVE.color()), true);
            }
            if (obstinateVictim) {
                p.sendMessage(Text.translatable("btt.trait.obstinate.death", dead.getName().getString())
                        .withColor(Traits.OBSTINATE.color()), true);
            }
        }

    }


    private static void playConfirmedDeathSounds(ServerPlayerEntity dead, Identifier reason) {
        var world = dead.getServerWorld();
        if (DeathReasons.MAGIC.equals(reason) || DeathReasons.FAILURE.equals(reason)) {
            world.playSound(null, dead.getBlockPos(), Sounds.MAGIC_DEATH,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        boolean obstinate = Traits.has(dead, Traits.OBSTINATE);
        boolean star = GameWorldComponent.KEY.get(world).isRole(dead, Roles.STAR)
                && !PlayerState.KEY.get(dead).isDrunk();
        if (!obstinate && !star) return;
        for (ServerPlayerEntity listener : world.getPlayers()) {
            if (obstinate) {

                listener.playSoundToPlayer(Sounds.JUDGMENT,
                        SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else if (Roles.isPassengerCampFor(GameWorldComponent.KEY.get(world), listener)) {

                listener.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket(
                        net.minecraft.registry.Registries.SOUND_EVENT.getEntry(Sounds.STAR_DEATH),
                        SoundCategory.PLAYERS,
                        dead.getX(), dead.getY(), dead.getZ(), 1.0F, 1.0F, world.getRandom().nextLong()));
            }
        }
    }

    public static void resolve(PlayerEntity victim, PlayerEntity killer, Identifier identifier) {
        if (!SeatAssignment.isBttMode(victim.getWorld())) return;



        if (victim instanceof ServerPlayerEntity patient
                && PlayerState.KEY.get(patient).blackdeathPatient) {
            dev.hae.btt.role.BlackDeath.onPatientDeath(patient, killer);
        }

        if (victim.getWorld() instanceof net.minecraft.server.world.ServerWorld sw0) {
            GameWorldComponent gwc0 = GameWorldComponent.KEY.get(sw0);
            if (gwc0.isRole(victim, dev.hae.btt.role.Roles.MESSIAH)) {
                for (ServerPlayerEntity p : sw0.getPlayers()) {
                    if (p == victim) continue;
                    if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
                    if (PlayerState.KEY.get(p).isMessiahFollower()) {
                        GameFunctions.killPlayer(p, true, p, DeathReasons.MARTYRDOM);
                    }
                }
            }
        }
        if (!(victim.getWorld() instanceof net.minecraft.server.world.ServerWorld world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (gwc.isRole(victim, dev.hae.btt.role.Roles.IMP) && victim instanceof ServerPlayerEntity deadImp) Imp.succession(deadImp);

        if (victim instanceof ServerPlayerEntity serverVictim) {
            Guard.onDeath(serverVictim, world, gwc);
        }


        java.util.UUID lover = dev.hae.btt.trait.relationship.Relationships.partnerOf(victim);
        if (lover != null && dev.hae.btt.trait.relationship.Relationships.isLover(victim)) {
            ServerPlayerEntity partner = (ServerPlayerEntity) world.getPlayerByUuid(lover);
            if (partner != null && GameFunctions.isPlayerAliveAndSurvival(partner)) {
                GameFunctions.killPlayer(partner, true, partner, DeathReasons.LOVER_SUICIDE);
            }
        }



        dev.hae.btt.role.Executioner.onTargetDeath(victim, killer, identifier);

        if (!(killer instanceof ServerPlayerEntity shooter)) return;


        PlayerState.KEY.get(shooter).hasKilled = 1;

        PlayerState shooterState = PlayerState.KEY.get(shooter);
        var victimRole = gwc.getRole(victim);
        if (victimRole != null && dev.hae.btt.role.Roles.kindOfReal(gwc, victim) == dev.hae.btt.role.Roles.Kind.PRINCIPAL) {
            shooterState.killedPrincipal = true;
        }
        if ((victimRole != null && dev.hae.btt.role.Roles.factionOf(victimRole) == dev.hae.btt.role.Roles.Faction.OUTSIDER)
                || dev.hae.btt.role.Roles.CAMP_OUTSIDER.equals(dev.hae.btt.role.Roles.campOverride(victim))) {
            shooterState.killedOutsider = true;
        }

        if (!dev.hae.btt.role.Roles.isPassengerCampFor(gwc, victim)
                && dev.hae.btt.role.Riot.isExecution(identifier)
                && dev.hae.btt.role.Colonel.executionUser(shooter, identifier)) {
            shooterState.correctExecutions++;
        }

        if (gwc.getRole(victim) != null
                && dev.hae.btt.role.Roles.SOLO_NEUTRALS.contains(gwc.getRole(victim))) {
            dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(shooter).addToBalance(100);
        }


        if (victim instanceof ServerPlayerEntity serverVictim2 && Traits.isKillerSide(serverVictim2)
                && Traits.has(shooter, Traits.GREED) && Traits.isKillerSide(shooter)) {
            var victimShop = dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(victim);
            int inherit = victimShop.balance;
            if (inherit > 0) {
                victimShop.setBalance(0);
                dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(shooter).addToBalance(inherit);
                shooter.sendMessage(Text.translatable("btt.trait.greed.inherit", inherit)
                        .withColor(Traits.GREED.color()), true);
            }
        }


        if ("ARCHENEMY".equals(dev.hae.btt.trait.relationship.Relationships.typeOf(victim))

                && dev.hae.btt.role.Roles.isCivilianPassenger(dev.hae.btt.role.Roles.realRole(gwc, victim))
                && identifier == GameConstants.DeathReasons.GUN
                && dev.hae.btt.role.Colonel.executionUser(shooter, identifier)) {
            java.util.UUID archenemy = dev.hae.btt.trait.relationship.Relationships.partnerOf(victim);
            ServerPlayerEntity archenemyPlayer = archenemy == null ? null : (ServerPlayerEntity) world.getPlayerByUuid(archenemy);
            if (archenemyPlayer != null) {


                dev.hae.btt.victory.SoloWin.request(world, dev.hae.btt.victory.Endings.Ending.ARCHENEMY_WIN, archenemyPlayer);
                return;
            }
        }


        RoleDef d = RoleDefs.get(gwc.getRole(shooter));
        if (d != null) {
            d.dispatchKill(shooter, victim, identifier, gwc);
        }



        if (!dev.hae.btt.role.Colonel.executionUser(shooter, identifier)) return;


        if (identifier != GameConstants.DeathReasons.GUN && identifier != DeathReasons.ARROW
                && identifier != GameConstants.DeathReasons.KNIFE) return;
        if (shooter == victim) return;


        var mood = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(shooter);
        float beforeExecutionMood = mood.getMood();
        mood.setMood(Math.max(0f, mood.getMood() - 0.4f));
        dev.hae.btt.advancement.BttAdvancements.executionSanity(shooter, beforeExecutionMood);

        if (!Roles.isPassengerCampFor(gwc, shooter)) return;


        boolean riotAlive = false;
        for (ServerPlayerEntity p : world.getPlayers()) {

            if (gwc.isRole(p, dev.hae.btt.role.Roles.RIOT)
                    && GameFunctions.isPlayerAliveAndSurvival(p)
                    && !PlayerState.KEY.get(p).isDrunk()) {
                riotAlive = true;
                break;
            }
        }


        boolean misfire = dev.hae.btt.role.Roles.isPassengerCampFor(gwc, victim);



        boolean bowKill = identifier == DeathReasons.ARROW;
        boolean ranger = bowKill && gwc.isRole(shooter, dev.hae.btt.role.Roles.RANGER);

        boolean judgment = dev.hae.btt.encounter.Backgrounds.isActive(world, dev.hae.btt.encounter.Backgrounds.JUDGMENT);



        if (bowKill && !shooter.isCreative()) {
            shooter.getItemCooldownManager().set(net.minecraft.item.Items.BOW, GameConstants.getInTicks(1, 0));
        }
        if (misfire) {
            var definition = dev.hae.btt.role.RoleDefs.get(gwc.getRole(shooter));
            if (definition != null) definition.onMisexecution(shooter);

            if (!ranger && (riotAlive || bowKill || judgment)) {
                GameFunctions.killPlayer(shooter, true, shooter, DeathReasons.SELF_EXECUTION);
            }

            if (!ranger && Traits.has(shooter, Traits.JUSTICE)) {
                GameFunctions.killPlayer(shooter, true, shooter, DeathReasons.SELF_EXECUTION);
            }


            if (!ranger && PlayerState.KEY.get(shooter).virginHonored) {
                GameFunctions.killPlayer(shooter, true, shooter, DeathReasons.SELF_EXECUTION);
            }

            dev.hae.btt.component.GameState.KEY.get(world).misfireCount++;
            if (!riotAlive) {
                for (ServerPlayerEntity p : world.getPlayers()) {
                    if (gwc.canUseKillerFeatures(p)) {
                        dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(p).addToBalance(100);
                    }
                }
            }
        } else {

            if (ranger) {
                GameFunctions.killPlayer(shooter, true, shooter, DeathReasons.SELF_EXECUTION);
            } else if (riotAlive) {
                for (ServerPlayerEntity p : world.getPlayers()) {
                    if (gwc.canUseKillerFeatures(p)) {
                        dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(p).addToBalance(100);
                    }
                }
            }
        }

        if (!ranger && Traits.has(victim, Traits.CHASTITY)) {
            GameFunctions.killPlayer(shooter, true, shooter, DeathReasons.SELF_EXECUTION);
        }

        if (identifier == GameConstants.DeathReasons.GUN && shooter != victim
                && Traits.has(shooter, Traits.RECKLESS) && !bttRecklessFiring) {
            bttRecklessFiring = true;
            try {
                GameFunctions.killPlayer(shooter, true, shooter, DeathReasons.SELF_EXECUTION);
            } finally {
                bttRecklessFiring = false;
            }
        }
    }

    private static boolean bttRecklessFiring;
}
