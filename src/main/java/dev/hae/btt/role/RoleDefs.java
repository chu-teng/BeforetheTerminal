package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.ability.RoleSelectionAbilities;
import dev.hae.btt.ability.MultiTargetAbilities;
import dev.hae.btt.ability.TargetedAbilities;
import dev.hae.btt.ability.UntargetedAbilities;

import dev.hae.btt.component.AbilityState;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Map;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.mixin.game.ExecutionMixin;
import dev.hae.btt.mixin.game.KillHookMixin;
import dev.hae.btt.mixin.game.MaidPlatterMixin;
import dev.hae.btt.network.CorpseActionC2SPacket;
import dev.hae.btt.mechanic.SpiritWalk;
import dev.hae.btt.registry.BttItems;






public final class RoleDefs {
    private RoleDefs() {}


    static final int PRIEST_KNIFE_USES = 3;

    public static final int CD_1MIN = GameConstants.getInTicks(1, 0);

    public static final int CD_2MIN = GameConstants.getInTicks(2, 0);

    public static final int CD_30S = GameConstants.getInTicks(0, 30);
    private static final Map<dev.doctor4t.wathe.api.Role, RoleDef> DEFS = new HashMap<>();

    public static RoleDef host(String type) {
        return DEFS.values().stream().filter(d -> d.outsiderHost() != null
                && d.outsiderHost().type().equals(type)).findFirst().orElse(null);
    }
    public static String hostFor(dev.doctor4t.wathe.api.Role role) {
        var def = get(role); return def == null || def.outsiderHost() == null ? "" : def.outsiderHost().type();
    }
    public static String firstHost(java.util.Set<dev.doctor4t.wathe.api.Role> camps) {
        return camps.stream().map(RoleDefs::get).filter(d -> d != null && d.outsiderHost() != null)
                .min(java.util.Comparator.comparingInt(d -> d.outsiderHost().priority()))
                .map(d -> d.outsiderHost().type()).orElse("SURVIVAL");
    }
    public static void init() {
        def(Roles.MARIONETTE).opening(Decoy::marionette);
        def(Roles.LUNATIC).opening(Decoy::lunatic);
        def(Roles.VOODOO).anyDeath(Voodoo::observeDeath).onDeath(Voodoo::ownerDied).targeted(Voodoo::revive)
                .outsider("VOODOO", dev.hae.btt.victory.Endings.Ending.VOODOO_WIN, 2, dev.hae.btt.registry.Sounds.EPILOGUE_VOODOO);
        def(Roles.ANGEL).initialize(Angel::initialize).nearby(Angel::ready, Angel::guard)
                .protection(Angel::protects, Angel::redirect).lethal(Angel::allowLethal).onDeath(Angel::died);
        def(Roles.WARDEN).kit(p -> giveWithInitialCd(p, BttItems.CAGE));
        def(Roles.LEVIATHAN).killerVictory(p -> dev.hae.btt.component.GameState.KEY.get(p.getWorld()).misfireCount >= 3);
        def(Roles.VORTEX).killerVictory(p -> p.getServerWorld().getTime()
                - dev.hae.btt.component.GameState.KEY.get(p.getWorld()).lastExecutionAt >= 2400);
        def(Roles.CRAFTSMAN).initialize(p -> initialAbilityCd(p, CD_1MIN)).nearby(Craftsman::ready, Craftsman::use);
        def(Roles.GAMBLER).initialize(Gambler::initialize).roleChoice(Gambler::guess);
        def(Roles.GOON).beforeTargeted(Goon::intercept);
        def(Roles.ASTRAL).anyDeath((victim, killer, reason) -> KillObjectives.observe(Roles.ASTRAL, victim, killer, reason));
        def(Roles.KIDNAPPER).anyDeath((victim, killer, reason) -> KillObjectives.observe(Roles.KIDNAPPER, victim, killer, reason));
        def(Roles.COLD_WAVE).anyDeath((victim, killer, reason) -> KillObjectives.observe(Roles.COLD_WAVE, victim, killer, reason));


        def(Roles.AGENT).kit(knife());

        def(Roles.GODFATHER).targeted(TargetedAbilities::revenge).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            knife().give(p);
        });



        def(Roles.SCULPTOR).targeted(RoleSelectionAbilities::sculptor).initialize(p -> initialAbilityCd(p, CD_2MIN));

        def(Roles.CANNIBAL).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.KIDNAPPER).targeted((user, target) -> Kidnapper.swallow(user, target, PlayerState.KEY.get(target))).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        });

        def(Roles.STOWAWAY).initialize(p -> {
            initialAbilityCd(p, GameConstants.getInTicks(2, 0));
        }).kit(p -> {
            knife().give(p);
        });
        def(Roles.ACTOR).untargeted(user -> Actor.toggle(user, PlayerState.KEY.get(user), Roles.ACTOR)).kit(p -> knife().give(p));
        def(Roles.WITCH).initialize(p -> {
            PlayerState.KEY.get(p).witchUses = 1;
        }).kit(p -> {

            if (!dev.hae.btt.game.GameModes.isDebate(p.getWorld())) knife().give(p);
        });


        def(Roles.CLEANER).untargeted(Cleaner::purge).initialize(p -> {
            initialAbilityCd(p, CD_2MIN);
        }).kit(p -> {
            giveWithInitialCd(p, BttItems.DAGGER);
        });

        def(Roles.ALCHEMIST).kit(item(BttItems.POISON_NEEDLE));

        def(Roles.TERRORIST).kit(item(BttItems.BOMB));

        def(Roles.SWORDSMAN).kit(p -> giveWithInitialCd(p, dev.hae.btt.registry.BttItems.SWORD));
        def(Roles.VETERAN).kit(knife()).knifeUse(p -> {
            if (dev.hae.btt.game.GameModes.isDebate(p.getWorld())) removeOne(p, WatheItems.KNIFE);
        });
        def(Roles.PRIEST).initialize(p -> PlayerState.KEY.get(p).priestKnifeUses = dev.hae.btt.game.GameModes.isDebate(p.getWorld()) ? 1 : PRIEST_KNIFE_USES)
                .kit(knife()).misexecution(Priest::misexecution);
        revolverKit(Roles.VIGILANTE);
        revolverKit(Roles.RAILWAY_POLICE);
        revolverKit(Roles.JUDGE);
        def(Roles.JUDGE).multiTargeted(RoleDef.distinctTargets(1, 24), MultiTargetAbilities::judge);
        revolverKit(Roles.HUNTER);
        revolverKit(Roles.GUNSLINGER);
        revolverKit(Roles.NIGHT_WATCHMAN);

        RoleDef hunter = def(Roles.HUNTER).targeted(TargetedAbilities::hunter);
        hunter.initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            giveWithInitialCd(p, WatheItems.REVOLVER);
        });
        revolverKit(Roles.COLONEL);
        def(Roles.COLONEL).self(Colonel::toggle);

        def(Roles.MAJO).kit(p -> {
            giveWithInitialCd(p, WatheItems.REVOLVER);
        });

        def(Roles.MESSIAH).targeted(RoleSelectionAbilities::messiah).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        });

        def(Roles.BARTENDER).untargeted(RoleSelectionAbilities::bartender).initialize(p -> initialAbilityCd(p, CD_1MIN));
        def(Roles.JOURNALIST).targeted(TargetedAbilities::journalist).initialize(p -> initialAbilityCd(p, CD_1MIN));



        def(Roles.PROFESSOR).untargeted(UntargetedAbilities::professorVial).initialize(p -> initialAbilityCd(p, GameConstants.getInTicks(2, 0)));


        def(Roles.GARDENER).untargeted(UntargetedAbilities::gardener).initialize(p -> {
            initialAbilityCd(p, CD_30S);
        });


        def(Roles.SUCCUBUS).kit(knife());

        def(Roles.BODYGUARD).targeted(Guard::guard).initialize(p -> initialAbilityCd(p, Guard.GUARD_CD));



        def(Roles.LEECH).targeted(Guard::parasitize).kit(knife());


        def(Roles.COMEDIAN).targeted(Comedian::inflate).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            giveWithInitialCd(p, BttItems.BALLOON);
        });

        def(Roles.POPPY_GROWER);
        def(Roles.HARPY).picks(2).kit(knife()).multiTargeted(RoleDef.distinctTargets(2, 2), Harpy::shroud)
                .fizzleCooldown(Harpy.COOLDOWN).initialize(p -> initialAbilityCd(p, Harpy.COOLDOWN));
        Harpy.register();
        def(Roles.MASTERMIND).kit(knife()).anyDeath(Mastermind::death);
        def(Roles.LIBRARIAN).targeted(TargetedAbilities::isolation)
                .initialize(p -> initialAbilityCd(p, CD_30S));
        def(Roles.WEREWOLF).opening(Werewolf::recruit).onDeath(Werewolf::died)
                .outsider("WEREWOLF", dev.hae.btt.victory.Endings.Ending.WEREWOLF_WIN, 4, dev.hae.btt.registry.Sounds.EPILOGUE_WEREWOLF);
        def(Roles.DAMSEL).opening(Damsel::opening).onDeath(Damsel::died);
        def(Roles.SAINT).onDeath((victim, killer, reason) -> {
            if (!Drunk.isDrunk(victim)
                    && dev.hae.btt.trait.TraitRules.passengerExecution(victim, killer, reason))
                dev.hae.btt.victory.EndingPipeline.factionLoss(victim);
        });
        def(Roles.SMUGGLER).self(Smuggler::use).onDeath((victim, killer, reason) -> Smuggler.releaseOwned(victim))
                .initialize(p -> initialAbilityCd(p, CD_30S));
        def(Roles.EMPATH).onTick(Empath::tick);
        def(Roles.NOBLE);


        def(Roles.RIOT);
        def(Roles.VORTEX);

        def(Roles.VORTEX).onTick((player, world, gwc) -> {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;

            if (PlayerState.KEY.get(player).isDrunk()) return;
            for (var p : world.getPlayers()) {
                if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
                if (p != player && Roles.kindOf(gwc.getRole(p)) == Roles.Kind.CIVILIAN) {
                    PlayerState.KEY.get(p).applyDrunk(2);
                }
            }
        });

        def(Roles.TRAITOR).kit(knife());



        def(Roles.EX_TRAITOR);


        def(Roles.CABALLERO).kit(p -> p.giveItemStack(new ItemStack(Items.BOW)));
        def(Roles.RANGER).kit(p -> p.giveItemStack(new ItemStack(Items.BOW)));


        def(Roles.PSYCHOPATH).kit(p -> giveWithInitialCd(p, BttItems.FIRE_AXE))
                .onTick((player, world, game) -> {
                    var pc = PlayerState.KEY.get(player);
                    boolean axe = GameFunctions.isPlayerAliveAndSurvival(player)
                            && (player.getMainHandStack().isOf(BttItems.FIRE_AXE)
                                || player.getOffHandStack().isOf(BttItems.FIRE_AXE));
                    if (axe && !pc.holdingFireAxe) {
                        for (var audience : player.getServerWorld().getPlayers()) {
                            audience.sendMessage(net.minecraft.text.Text.translatable("btt.action.psychopath.draw", player.getDisplayName()).withColor(Roles.PSYCHOPATH.color()), true);
                            audience.playSoundToPlayer(dev.hae.btt.registry.Sounds.METAL_NOTICE,
                                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                    pc.holdingFireAxe = axe;
                });
        def(Roles.DETECTIVE).targeted(TargetedAbilities::detective).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.INVESTIGATOR).targeted(TargetedAbilities::investigator).initialize(p -> initialAbilityCd(p, CD_1MIN));


        def(Roles.MEDIUM).targeted(Medium::retain).initialize(p -> initialAbilityCd(p, CD_30S));

        def(Roles.EXECUTIONER).targeted(Executioner::mark).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.VIRGIN).targeted(TargetedAbilities::virginHonor).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.STAR).targeted(TargetedAbilities::starMark).initialize(p -> initialAbilityCd(p, CD_30S));

        def(Roles.LAWYER).targeted(TargetedAbilities::lawyer).initialize(p -> {
            initialAbilityCd(p, GameConstants.getInTicks(2, 0));
        }).kit(p -> {
            giveWithInitialCd(p, WatheItems.REVOLVER);
        });

        def(Roles.PHILOSOPHER).untargeted(RoleSelectionAbilities::philosopher).initialize(p -> initialAbilityCd(p, CD_1MIN));


        def(Roles.HIKIKOMORI).kit(p -> giveWithInitialCd(p, BttItems.PIPE));


        def(Roles.PUPPETEER).multiTargeted(RoleDef.distinctTargets(2, 2), Puppeteer::manipulate).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.ASTRAL).untargeted(user -> Astral.toggle(user, dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(user.getWorld())));

        def(Roles.COLD_WAVE).targeted(ColdWave::diagnose).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.LEGION).kit(item(BttItems.POISON_NEEDLE)).poisonRecruit(Legion::recruit).onDeath(Legion::died);
        def(Roles.MAJO).outsider("MAJO", dev.hae.btt.victory.Endings.Ending.MAJO_WIN, 0, dev.hae.btt.registry.Sounds.EPILOGUE_MAJO);
        def(Roles.MESSIAH).outsider("MESSIAH", dev.hae.btt.victory.Endings.Ending.MESSIAH_WIN, 1, dev.hae.btt.registry.Sounds.EPILOGUE_MESSIAH);
        def(Roles.GARDENER).outsider("GARDENER", dev.hae.btt.victory.Endings.Ending.GARDENER_WIN, 6, dev.hae.btt.registry.Sounds.EPILOGUE_GARDENER);
        def(Roles.LEGION).outsider("LEGION", dev.hae.btt.victory.Endings.Ending.LEGION_WIN, 3, dev.hae.btt.registry.Sounds.EPILOGUE_LEGION);

        def(Roles.RIGGER).multiTargeted(RoleDef.distinctTargets(2, 2), MultiTargetAbilities::rigger).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            knife().give(p);
        });

        def(Roles.LOCKSMITH).untargeted(UntargetedAbilities::locksmith).initialize(p -> initialAbilityCd(p, GameConstants.getInTicks(0, 30)));
        def(Roles.PHARMACIST).targeted(TargetedAbilities::pharmacist).initialize(p -> initialAbilityCd(p, CD_1MIN));
        def(Roles.ENGINEER).untargeted(UntargetedAbilities::engineer).initialize(p -> initialAbilityCd(p, CD_1MIN));
        def(Roles.ARCHITECT).untargeted(Architect::open).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.MUYUUBYOU).untargeted(SpiritWalk::toggle)
                .initialize(p -> initialAbilityCd(p, 0))
                .onTick((player, world, gwc) -> SpiritWalk.tick(player, gwc));

        def(Roles.THIEF).initialize(p -> {
            initialAbilityCd(p, GameConstants.getInTicks(0, 10));
        }).kit(p -> {
            p.giveItemStack(new ItemStack(dev.hae.btt.registry.BttItems.MASTER_KEY));
        });

        def(Roles.ARSONIST).untargeted(user -> Arsonist.douse(user, dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(user.getWorld()))).initialize(p -> {
            initialAbilityCd(p, Arsonist.INITIAL_CD_TICKS);
        }).kit(p -> {
            p.giveItemStack(new ItemStack(dev.hae.btt.registry.BttItems.MASTER_KEY));
        });



        def(Roles.PROPHET).multiTargeted((user, picks) -> picks.size() == 3 && new java.util.HashSet<>(picks).size() >= 2, MultiTargetAbilities::prophet).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.ASSASSIN).targeted(RoleSelectionAbilities::assassin).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            knife().give(p);
        });

        def(Roles.MAGICIAN).multiTargeted(RoleDef.distinctTargets(2, 2), (user, game, picks) -> MultiTargetAbilities.magician(user, picks)).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            knife().give(p);
        });

        def(Roles.NOVELIST).targeted(RoleSelectionAbilities::novelist).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.SNAKE_CHARMER).targeted(TargetedAbilities::snakeCharmer).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.IMP).targeted(Imp::mark).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            knife().give(p);
        });

        def(Roles.AMNESIAC);


        def(Roles.MONK).targeted(Monk::deify).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.COLLECTOR).untargeted(Cheater::summon).initialize(p -> initialAbilityCd(p, GameConstants.getInTicks(0, 30)));








        def(Roles.PRIEST).knifeUse(player -> {
            var state = PlayerState.KEY.get(player);
            if (--state.priestKnifeUses <= 0) removeOne(player, WatheItems.KNIFE);
        });


        def(Roles.WITCH).onKill((shooter, victim, reason, gwc) -> {
            if (reason != GameConstants.DeathReasons.KNIFE) return;
            PlayerState.KEY.get(shooter).witchUses = 0;
            removeOne(shooter, WatheItems.KNIFE);
        });




        def(Roles.PSYCHOPATH).onKill((shooter, victim, reason, gwc) -> {
            if (reason != GameConstants.DeathReasons.BAT && reason != DeathReasons.AXE) return;

            shooter.getItemCooldownManager().set(BttItems.FIRE_AXE, GameConstants.getInTicks(1, 0));
            shooter.getItemCooldownManager().set(WatheItems.BAT, GameConstants.getInTicks(1, 0));
        });




        def(Roles.VIGILANTE).onTick((player, world, gwc) ->
                PlayerMoodComponent.KEY.get(player).setMood(1.0f));


        def(Roles.ATTENDANT).onTick((player, world, gwc) -> {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                dev.doctor4t.wathe.cca.GameTimeComponent.KEY.get(world).addTime(-1);
            }
        });


        def(Roles.NIGHT_WATCHMAN).onTick((player, world, gwc) -> {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
            dev.hae.btt.trait.TraitStatusEffects.maintain(player,
                    net.minecraft.entity.effect.StatusEffects.NIGHT_VISION, true);
        });


        def(Roles.JESTER).onKill((shooter, victim, reason, gwc) -> {
            if (dev.hae.btt.game.GameModes.modeOf(gwc) == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.SCRIPT
                    && PlayerPsychoComponent.KEY.get(shooter).getPsychoTicks() > 0
                    && victim.getUuidAsString().equals(PlayerState.KEY.get(shooter).jesterExecutioner))
                dev.hae.btt.game.ScriptObjectives.complete(shooter);
        }).onTick((player, world, gwc) -> {
            PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
            if (psycho.getPsychoTicks() > 0) {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    if (player.getInventory().getStack(i).isOf(WatheItems.BAT)) {
                        if (player.getInventory().selectedSlot != i) {
                            player.getInventory().selectedSlot = i;
                        }
                        break;
                    }
                }
            } else if (hasItem(player, WatheItems.BAT)) {
                removeOne(player, WatheItems.BAT);
            }
        });










            }




    public static boolean isNearbySkill(dev.doctor4t.wathe.api.Role role) {
        if (get(role) != null && get(role).hasNearby()) return true;
        return role != null && java.util.Set.of(Roles.PHARMACIST, Roles.COMEDIAN, Roles.LIBRARIAN, Roles.BODYGUARD, Roles.LEECH, Roles.MONK,
                Roles.KIDNAPPER, Roles.IMP, Roles.INVESTIGATOR, Roles.EXECUTIONER, Roles.COLD_WAVE,
                Roles.LAWYER, Roles.VIRGIN, Roles.DETECTIVE).contains(role);
    }


    public static boolean isAnyTarget(dev.doctor4t.wathe.api.Role role) {
        var definition = get(role);
        return role == Roles.ACTOR || definition != null && (definition.hasMultiTargeted()
                || definition.hasTargeted() && !isNearbySkill(role));
    }


    public static boolean isImmediateTarget(dev.doctor4t.wathe.api.Role role) {
        var definition = get(role);
        return definition != null && definition.hasTargeted()
                && !isNearbySkill(role) && !GuessPool.isPickRole(role);
    }

    public static RoleDef get(dev.doctor4t.wathe.api.Role role) {
        return role == null ? null : DEFS.get(role);
    }

    public static boolean killerVictory(ServerWorld world) {
        if (dev.hae.btt.component.GameState.KEY.get(world).killerObjectiveMet) return true;
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(player) || Drunk.isDrunk(player)) continue;
            var definition = get(game.getRole(player));
            if (definition != null && definition.killerVictory(player)) return true;
        }
        return false;
    }

    public static int defCount() {
        return DEFS.size();
    }

    private static RoleDef def(dev.doctor4t.wathe.api.Role role) {
        return DEFS.computeIfAbsent(role, RoleDef::of);
    }

    private static RoleDef.Kit knife() {

        return p -> giveWithInitialCd(p, WatheItems.KNIFE);
    }





    private static RoleDef.Kit item(Item item) {
        return p -> giveWithInitialCd(p, item);
    }











    public static void giveWithInitialCd(ServerPlayerEntity p, Item item) {
        p.giveItemStack(new ItemStack(item));
        p.getItemCooldownManager().set(item, CD_30S);
    }







    private static void initialAbilityCd(ServerPlayerEntity p, int ticks) {
        AbilityState a = AbilityState.KEY.get(p);
        a.setCooldown(ticks <= 0 ? 0 : CD_30S);
        a.sync();
    }

    private static void revolverKit(dev.doctor4t.wathe.api.Role role) {

        def(role).kit(p -> giveWithInitialCd(p, WatheItems.REVOLVER));
    }



    static boolean hasItem(ServerPlayerEntity player, Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(item)) return true;
        }
        return false;
    }


    public static void removeOne(ServerPlayerEntity player, Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                stack.setCount(0);
                player.getInventory().markDirty();
                return;
            }
        }
    }
}
