package dev.hae.btt.game;

import dev.hae.btt.role.RoleTransitions;

import dev.hae.btt.encounter.EncounterRules;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;

import java.util.Random;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.network.ActionReceiver;
import dev.hae.btt.trait.relationship.Relationships;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Guard;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.ability.shop.ShopGate;
import dev.hae.btt.trait.Traits;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.item.ItemUse;
import dev.hae.btt.BeforeTheTerminal;










public final class GameEvents {
    private GameEvents() {}

    private static final Random RANDOM = new Random();


    public static void onConfirmedDeath(ServerPlayerEntity dead, Identifier reason, boolean spawnBody) {
        dev.hae.btt.combat.DeathRules.onConfirmedDeath(dead, reason, spawnBody);
    }

    public static void init() {
        ensureWatheClientConfig();
        registerKitDispatch();
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                server.execute(() -> LateJoin.placeSpectator(handler.getPlayer())));
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity victim)
                    || !(source.getAttacker() instanceof PlayerEntity attacker) || attacker == victim) return true;
            if (!dev.hae.btt.role.Priest.protects(victim, attacker, DeathReasons.MAGIC)) return true;
            dev.hae.btt.combat.DeathResolution.shieldBreak(victim, attacker);
            return false;
        });
        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> dev.hae.btt.role.Warden.died(victim));
        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return;
            var definition = RoleDefs.get(GameWorldComponent.KEY.get(victim.getWorld()).getRole(victim));
            if (definition != null) definition.dispatchDeath(victim, killer, reason);
            java.util.Set<dev.doctor4t.wathe.api.Role> observed = new java.util.HashSet<>();
            for (var observer : victim.getServerWorld().getPlayers()) {
                var observerDef = RoleDefs.get(GameWorldComponent.KEY.get(victim.getWorld()).getRole(observer));
                if (observerDef != null && observed.add(observerDef.role)) observerDef.observeDeath(victim, killer, reason);
            }
        });
        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (SeatAssignment.isBttMode(victim.getWorld())
                    && dev.hae.btt.trait.TraitRules.execution(victim, killer, reason)) {
                dev.hae.btt.component.GameState.KEY.get(victim.getWorld()).lastExecutionAt = victim.getWorld().getTime();
            }
        });
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> !(victim instanceof ServerPlayerEntity player)
                || dev.hae.btt.trait.TraitRules.allowDeath(player, killer, reason));
        registerJesterPsycho();
        dev.hae.btt.game.ActionRewards.register();



        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            net.minecraft.server.network.ServerPlayerEntity quitter = handler.getPlayer();
            if (quitter == null) return;


            dev.hae.btt.trait.Traits.onDisconnect(quitter.getUuid());
            if (!SeatAssignment.isBttMode(quitter.getWorld())) return;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(quitter.getWorld());
            if (!gwc.isRunning()) return;
            if (!GameFunctions.isPlayerAliveAndSurvival(quitter)) return;


            GameFunctions.killPlayer(quitter, true, null, dev.hae.btt.combat.DeathReasons.ESCAPE);
        });
        registerPharmacistPoison();
        ActionReceiver.register();
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!(victim instanceof ServerPlayerEntity player) || !EncounterRules.stormProtected(player)
                    || dev.hae.btt.trait.TraitRules.execution(victim, killer, reason)
                    || dev.hae.btt.trait.TraitRules.voluntaryDeath(victim, killer, reason)) return true;
            dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
            return false;
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && EncounterRules.stormProtected(player)
                    && source.getAttacker() != player
                    && !source.isOf(net.minecraft.entity.damage.DamageTypes.GENERIC_KILL)
                    && !source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
                dev.hae.btt.combat.DeathResolution.shieldBreak(player, source.getAttacker() instanceof PlayerEntity attacker ? attacker : null);
                player.setHealth(1.0f);
                return false;
            }
            return true;
        });




        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(
                (entity, damageSource) -> {
                    if (!(entity instanceof ServerPlayerEntity victim)) return;
                    if (!SeatAssignment.isBttMode(victim.getWorld())) return;
                    GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());
                    if (!gwc.isRunning()) return;
                    if (gwc.getRole(victim) == null) return;
                    dev.hae.btt.combat.DeathResolution.notifyDeath(
                            victim, null, GameConstants.DeathReasons.GENERIC, false);
                });
        ShopGate.init();
        registerMaidGive();
        registerPostmanGive();
        registerDebateReport();
        registerPsychopathShield();
        registerProfessorShield();
        registerGuardImmunity();
        registerKidnapperImmunity();
        guardHmlPool();
        registerTraitHooks();
        dev.hae.btt.game.Trophies.register();

        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
                dev.hae.btt.network.WelcomeLineS2CPacket.ID,
                dev.hae.btt.network.WelcomeLineS2CPacket.CODEC);

        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) return net.minecraft.util.ActionResult.PASS;
            if (!SeatAssignment.isBttMode(world)) return net.minecraft.util.ActionResult.PASS;
            var state = world.getBlockState(hit.getBlockPos());

            if (state.getBlock() instanceof net.minecraft.block.NoteBlock) {
                dev.hae.btt.game.Needs.onBlockUse(sp, "MUSIC");
            }
            return net.minecraft.util.ActionResult.PASS;
        });
    }












    private static void registerTraitHooks() {

        registerTraitHooksInner();
    }


    public static boolean aviateurNotMurder(Identifier reason) {
        if (reason == null) return false;
        if (reason.equals(GameConstants.DeathReasons.FELL_OUT_OF_TRAIN)) return true;
        if (reason.equals(dev.hae.btt.combat.DeathReasons.COLLAPSE)) return true;
        return dev.hae.btt.combat.DeathReasons.isSuicide(reason);
    }

    private static void registerTraitHooksInner() {


        CanSeePoison.EVENT.register(player -> {
            if (player == null) return false;
            if (!SeatAssignment.isBttMode(player.getWorld())) return false;
            return GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Roles.PHARMACIST)
                    || Traits.has(player, Traits.PURITY);
        });



        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!(victim instanceof ServerPlayerEntity serverVictim)) return true;
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;


            if (!GameWorldComponent.KEY.get(victim.getWorld()).isRunning()) return true;

            if (Traits.vetoDeath(serverVictim, killer, reason)) {
                dev.hae.btt.role.Riot.reloadDerringerIfShot(killer, reason);
                return false;
            }
            if (dev.hae.btt.role.Riot.vetoExecution(serverVictim, killer, reason)) {
                dev.hae.btt.role.Riot.reloadDerringerIfShot(killer, reason);
                return false;
            }
            return true;
        });




        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return;
            if (!GameWorldComponent.KEY.get(victim.getWorld()).isRole(victim, Roles.AVIATEUR)) return;
            PlayerState aviateurPc = PlayerState.KEY.get(victim);
            if (aviateurPc.aviateurUsed || aviateurNotMurder(reason)) return;
            aviateurPc.aviateurUsed = true;
            if (!GameFunctions.isPlayerAliveAndSurvival(victim)) {
                victim.changeGameMode(net.minecraft.world.GameMode.ADVENTURE);
            }
            victim.teleport(victim.getServerWorld(), aviateurPc.roomX, aviateurPc.roomY, aviateurPc.roomZ,
                    victim.getYaw(), victim.getPitch());
            victim.giveItemStack(new net.minecraft.item.ItemStack(
                    dev.doctor4t.wathe.index.WatheItems.REVOLVER));

            dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(victim).setMood(1.0f);
            aviateurPc.sync();
        });
        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return;
            if (victim.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                Traits.onDeath(victim, sw);
            }
        });

        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return;
            if (victim.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                dev.hae.btt.role.Medium.onDeath(victim, sw);
            }
        });

        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return;
            if (victim.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                dev.hae.btt.trait.relationship.Relationships.onDeath(victim, sw);
            }
        });


        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return;
            if (!(victim.getWorld() instanceof net.minecraft.server.world.ServerWorld sw)) return;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());

            dev.hae.btt.role.Roles.Kind kind = dev.hae.btt.role.Roles.kindOf(gwc.getRole(victim));
            boolean neutral = kind != null && kind.isNeutralKind();
            if (!neutral) return;
            for (ServerPlayerEntity godfather : sw.getPlayers()) {
                if (!dev.hae.btt.role.Roles.hasCurrentOrPriorRole(gwc, godfather, dev.hae.btt.role.Roles.GODFATHER)) continue;
                java.util.UUID mark = dev.hae.btt.component.CurseState.KEY.get(godfather).target;
                if (mark == null) continue;
                if (!(sw.getPlayerByUuid(mark) instanceof ServerPlayerEntity marked)) continue;
                if (marked == victim) continue;
                if (!GameFunctions.isPlayerAliveAndSurvival(marked)) continue;
                GameFunctions.killPlayer(marked, true, godfather, dev.hae.btt.combat.DeathReasons.REVENGE);
            }
        });
    }





    public static void refillSprint(ServerPlayerEntity player) {
        try {
            java.lang.reflect.Field f = player.getClass().getDeclaredField("sprintingTicks");
            f.setAccessible(true);
            f.setFloat(player, 1.0E9F);
        } catch (Throwable ignored) {

        }
    }



    private static void registerPsychopathShield() {
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());
            if (!gwc.isRunning()) return true;
            if (!gwc.isRole(victim, Roles.PSYCHOPATH)) return true;

            net.minecraft.item.ItemStack handStack = victim.getMainHandStack();
            boolean holdsWeapon = handStack.isOf(WatheItems.BAT) || handStack.isOf(BttItems.FIRE_AXE);
            if (!holdsWeapon) return true;
            if (ItemUse.cooling(victim, handStack.getItem())) return true;
            dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
            return false;
        });

        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {

            if (reason != GameConstants.DeathReasons.BAT && reason != dev.hae.btt.combat.DeathReasons.AXE) return true;
            if (!(killer instanceof PlayerEntity k)) return true;
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());
            if (!gwc.isRole(k, Roles.PSYCHOPATH)) return true;

            if (ItemUse.cooling(k, BttItems.FIRE_AXE)) return false;
            if (ItemUse.cooling(k, WatheItems.BAT)) return false;
            return true;
        });



        dev.doctor4t.wathe.api.event.AllowPlayerPunching.EVENT.register((attacker, victim) -> {
            if (!SeatAssignment.isBttMode(attacker.getWorld())) return false;
            if (!GameWorldComponent.KEY.get(attacker.getWorld()).isRunning()) return false;
            if (!GameFunctions.isPlayerAliveAndSurvival(attacker)
                    || !GameFunctions.isPlayerAliveAndSurvival(victim)) return false;
            var mainHand = attacker.getMainHandStack();
            return mainHand.isOf(dev.hae.btt.registry.BttItems.DAGGER)
                    || mainHand.isOf(dev.hae.btt.registry.BttItems.SWORD)


                    || mainHand.isOf(dev.hae.btt.registry.BttItems.PIPE)



                    || mainHand.isOf(dev.hae.btt.registry.BttItems.FIRE_AXE);
        });
    }



    private static void registerMaidGive() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity user)) return ActionResult.PASS;
            if (!SeatAssignment.isBttMode(world)) return ActionResult.PASS;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
            if (!gwc.isRunning()) return ActionResult.PASS;
            if (!gwc.isRole(user, Roles.MAID)) return ActionResult.PASS;
            if (entity == null || entity == user) return ActionResult.PASS;
            ItemStack held = user.getMainHandStack();
            if (held.isEmpty() || !isFoodOrDrink(held)) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;


            String need = isDrink(held) ? "DRINK" : "EAT";
            if (!dev.hae.btt.game.Needs.has(target, need)) return ActionResult.PASS;
            if (!target.getInventory().insertStack(held.copy())) return ActionResult.PASS;
            user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

            return ActionResult.SUCCESS;
        });
    }


    private static boolean isFoodOrDrink(ItemStack stack) {
        return isDrink(stack) || stack.contains(net.minecraft.component.DataComponentTypes.FOOD);
    }

    private static boolean isDrink(ItemStack stack) {
        return stack.isOf(WatheItems.OLD_FASHIONED) || stack.isOf(WatheItems.MARTINI)
                || stack.isOf(WatheItems.MOJITO) || stack.isOf(WatheItems.COSMOPOLITAN)
                || stack.isOf(WatheItems.CHAMPAGNE);
    }



    private static void registerPostmanGive() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity user)) return ActionResult.PASS;
            if (!SeatAssignment.isBttMode(world)) return ActionResult.PASS;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
            if (!gwc.isRunning()) return ActionResult.PASS;
            if (entity == null || entity == user) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;
            if (!GameFunctions.isPlayerAliveAndSurvival(user)
                    || !GameFunctions.isPlayerAliveAndSurvival(target)) return ActionResult.PASS;

            boolean targetIsPostman = gwc.isRole(target, Roles.POSTMAN);
            if (!gwc.isRole(user, Roles.POSTMAN) && !targetIsPostman) return ActionResult.PASS;
            ItemStack held = user.getMainHandStack();
            if (held.isEmpty()) return ActionResult.PASS;
            if (!target.getInventory().insertStack(held.copyWithCount(1))) return ActionResult.PASS;
            held.decrement(1);

            if (targetIsPostman) {
            }
            return ActionResult.SUCCESS;
        });
    }




    private static void registerDebateReport() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity user)) return ActionResult.PASS;
            if (!(entity instanceof dev.doctor4t.wathe.entity.PlayerBodyEntity body)) return ActionResult.PASS;
            if (!GameModes.isDebate(world)) return ActionResult.PASS;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
            if (!gwc.isRunning()) return ActionResult.PASS;
            if (!GameFunctions.isPlayerAliveAndSurvival(user)) return ActionResult.PASS;

            if (dev.hae.btt.component.DebateState.KEY.get(world).phase
                    != dev.hae.btt.component.DebateState.PHASE_IDLE) return ActionResult.PASS;
            Debate.report(user, body);
            return ActionResult.SUCCESS;
        });

        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;
            return !Debate.vetoDamage(victim.getWorld(), reason);
        });
    }



    private static void registerKitDispatch() {
        ModdedRoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                RoleDef d = RoleDefs.get(role);
                if (d != null) {
                    d.dispatchInitialization(serverPlayer);
                    if (!RoleTransitions.isConverting(serverPlayer)) d.dispatchEquipment(serverPlayer);
                }

                if (!RoleTransitions.isConverting(serverPlayer) && Roles.kindOf(role) == Roles.Kind.PRINCIPAL) {
                    serverPlayer.giveItemStack(new ItemStack(WatheItems.CROWBAR));
                }
            }
        });
    }



    private static void registerJesterPsycho() {
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;

            if (reason != GameConstants.DeathReasons.GUN
                    && reason != dev.hae.btt.combat.DeathReasons.ARROW
                    && reason != GameConstants.DeathReasons.KNIFE) return true;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());
            if (!gwc.isRole(victim, Roles.JESTER)) return true;

            if (PlayerState.KEY.get(victim).isDrunk()) return true;
            if (killer == null || killer == victim || !dev.hae.btt.role.Colonel.executionUser(killer, reason)) return true;
            dev.doctor4t.wathe.cca.PlayerPsychoComponent psycho = dev.doctor4t.wathe.cca.PlayerPsychoComponent.KEY.get(victim);
            if (psycho.getPsychoTicks() > 0) return true;
            PlayerState.KEY.get(victim).jesterExecutioner = killer.getUuidAsString();
            dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
            psycho.startPsycho();
            psycho.setPsychoTicks(GameConstants.getInTicks(2, 0));

            psycho.setArmour(2 + dev.hae.btt.component.GameState.KEY.get(victim.getWorld()).misfireCount);
            if (victim instanceof ServerPlayerEntity jester) {

                dev.hae.btt.record.GameRecordManager.recordTalentUse(jester, "jester_psycho", null);
            }
            victim.giveItemStack(new ItemStack(WatheItems.BAT));
            for (int i = 0; i < victim.getInventory().size(); i++) {
                if (victim.getInventory().getStack(i).isOf(WatheItems.BAT)) {
                    victim.getInventory().selectedSlot = i;
                    break;
                }
            }
            return false;
        });
    }



    private static void registerProfessorShield() {
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;
            PlayerState shieldPc = PlayerState.KEY.get(victim);
            if (dev.hae.btt.role.Gunslinger.unblockable(killer, reason)) return true;
            if (shieldPc.monkShields > 0) {
        dev.hae.btt.combat.DeathResolution.blocked(victim, killer, true);
                shieldPc.monkShields--;
                shieldPc.sync();
                dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
                return false;
            }
            if (!shieldPc.hasProfessorShield()) return true;
            dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
        dev.hae.btt.combat.DeathResolution.blocked(victim, killer, true);
            shieldPc.consumeProfessorShield();
            if (victim instanceof ServerPlayerEntity shielded) {

                dev.hae.btt.record.GameRecordManager.recordTalentUse(shielded, "professor_vial", null);
            }

            return false;
        });
    }



    private static void registerGuardImmunity() {
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!(victim instanceof ServerPlayerEntity serverVictim)) return true;
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;


            if (dev.hae.btt.trait.TraitRules.voluntaryDeath(victim, killer, reason)
                    || !dev.hae.btt.trait.TraitRules.harmfulDeath(reason)) return true;

            if (dev.hae.btt.role.Gunslinger.unblockable(killer, reason)) return true;
            boolean blocked = Guard.vetoDeath(serverVictim);
            if (blocked) dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
            return !blocked;
        });
    }





    private static void registerKidnapperImmunity() {
        AllowPlayerDeath.EVENT.register((victim, killer, reason) -> {
            if (!SeatAssignment.isBttMode(victim.getWorld())) return true;
            return !PlayerState.KEY.get(victim).isSwallowed();
        });
    }



    private static void registerPharmacistPoison() {
        CanSeePoison.EVENT.register(player -> {
            if (!SeatAssignment.isBttMode(player.getWorld())) return false;
            return GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Roles.PHARMACIST);
        });
    }















    private static void guardHmlPool() {
        org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.load();
        boolean changed = false;

        for (dev.doctor4t.wathe.api.Role role : dev.doctor4t.wathe.api.WatheRoles.ROLES) {
            if (!BeforeTheTerminal.MOD_ID.equals(role.identifier().getNamespace())) continue;
            if (!org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().toString())) {
                org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.instance().disabled.add(role.identifier().toString());
                changed = true;
            }
        }



        for (org.agmas.harpymodloader.modifiers.Modifier mod : org.agmas.harpymodloader.modifiers.HMLModifiers.MODIFIERS) {
            if (!BeforeTheTerminal.MOD_ID.equals(mod.identifier.getNamespace())) continue;
            String key = mod.identifier().toString();
            if (!org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(key)) {
                org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.add(key);
                changed = true;
            }
        }
        if (changed) {
            org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.save();
            BeforeTheTerminalGameMode.LOGGER.info("[BTT] HML pool isolated: {} roles / {} modifiers disabled (murder 局不提供本 mod 内容)",
                    org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.instance().disabled.size(),
                    org.agmas.harpymodloader.config.HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.size());
        }
    }



    private static void ensureWatheClientConfig() {
        try {
            var cfg = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("wathe.json");
            if (java.nio.file.Files.notExists(cfg)) {
                java.nio.file.Files.writeString(cfg, "{\"ultraPerfMode\": false, \"disableScreenShake\": false}");
                BeforeTheTerminalGameMode.LOGGER.info("[BTT] pre-created config/wathe.json");
            }
        } catch (Exception e) {
            BeforeTheTerminalGameMode.LOGGER.warn("[BTT] could not pre-create wathe config: {}", e.toString());
        }
    }
}
