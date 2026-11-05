package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.ability.RoleSelectionAbilities;
import dev.hae.btt.ability.MultiTargetAbilities;
import dev.hae.btt.ability.TargetedAbilities;
import dev.hae.btt.ability.UntargetedAbilities;

import dev.hae.btt.component.AbilityState;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
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

    public static dev.doctor4t.wathe.api.Role hostRoleForEnding(dev.hae.btt.victory.Endings.Ending ending) {
        if (ending == null) return null;
        return DEFS.values().stream()
                .filter(d -> d.outsiderHost() != null && d.outsiderHost().ending() == ending)
                .map(d -> d.role).findFirst().orElse(null);
    }
    public static String hostFor(dev.doctor4t.wathe.api.Role role) {
        var def = get(role); return def == null || def.outsiderHost() == null ? "" : def.outsiderHost().type();
    }




    public static String firstHost(java.util.Set<dev.doctor4t.wathe.api.Role> camps) {
        var order = Roles.registeredRoleIds();
        return camps.stream().map(RoleDefs::get).filter(d -> d != null && d.outsiderHost() != null)
                .min(java.util.Comparator.comparingInt(d -> order.indexOf(d.role.identifier())))
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
        def(Roles.CRAFTSMAN).initialize(p -> initialAbilityCd(p, CD_1MIN)).nearby(Craftsman::ready, Craftsman::use);
        def(Roles.MORTICIAN).anyDeath(Investigations::morticianDeath);
        def(Roles.GAMBLER).initialize(Gambler::initialize).roleChoice(Gambler::guess);
        def(Roles.POLITICIAN).beforeTargeted(Goon::intercept);
        def(Roles.ASTRAL).anyDeath((victim, killer, reason) -> KillObjectives.observe(Roles.ASTRAL, victim, killer, reason));
        def(Roles.KIDNAPPER).anyDeath((victim, killer, reason) -> KillObjectives.observe(Roles.KIDNAPPER, victim, killer, reason));
        def(Roles.MESSENGER).anyDeath((victim, killer, reason) -> KillObjectives.observe(Roles.MESSENGER, victim, killer, reason));


        def(Roles.DUCE).kit(knife());
        def(Roles.GODFATHER).opening(Godfather::opening).replacement(Godfather::transformed)
                .outsider("GODFATHER", dev.hae.btt.victory.Endings.Ending.GODFATHER_WIN, 4, dev.hae.btt.registry.Sounds.EPILOGUE_GODFATHER);



        def(Roles.SCULPTOR).kit(knife()).targeted(RoleSelectionAbilities::sculptor).initialize(p -> initialAbilityCd(p, CD_2MIN));

        def(Roles.CANNIBAL).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.KIDNAPPER).targeted((user, target) -> Kidnapper.swallow(user, target, PlayerState.KEY.get(target))).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        });

        def(Roles.STOWAWAY).fizzleCooldown(CD_1MIN).targeted(RoleSelectionAbilities::stowawayCorpse).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            knife().give(p);
        });
        def(Roles.ACTOR).untargeted(user -> Actor.toggle(user, PlayerState.KEY.get(user), Roles.ACTOR)).kit(p -> knife().give(p));
        def(Roles.WITCH).initialize(p -> {
            PlayerState.KEY.get(p).witchUses = 3;
        }).kit(p -> {

            if (!dev.hae.btt.game.GameModes.isDebate(p.getWorld())) knife().give(p);
        });


        def(Roles.CLEANER).untargeted(Cleaner::purge).initialize(p -> {
            initialAbilityCd(p, CD_2MIN);
        }).kit(p -> {
            giveWithInitialCd(p, BttItems.DAGGER);
        });

        def(Roles.REVOLUCIONARIO).kit(knife());
        def(Roles.ALCHEMIST).kit(item(BttItems.POISON_NEEDLE)).self(dev.hae.btt.mechanic.Mines::placeGas).initialize(p -> initialAbilityCd(p, CD_2MIN));

        def(Roles.TERRORIST).kit(item(BttItems.BOMB)).self(dev.hae.btt.mechanic.Mines::place).initialize(p -> initialAbilityCd(p, CD_2MIN));

        def(Roles.KHESHIG).kit(p -> giveWithInitialCd(p, dev.hae.btt.registry.BttItems.SWORD));
        def(Roles.VETERAN).kit(knife()).knifeUse(p -> {
            if (dev.hae.btt.game.GameModes.isDebate(p.getWorld())) removeOne(p, WatheItems.KNIFE);
        });
        def(Roles.TLAMACAZQUI).initialize(p -> PlayerState.KEY.get(p).priestKnifeUses = dev.hae.btt.game.GameModes.isDebate(p.getWorld()) ? 1 : PRIEST_KNIFE_USES)
                .kit(knife()).misexecution(Priest::misexecution);
        revolverKit(Roles.VIGILANTE);
        def(Roles.VIGILANTE).outgoingInteraction((user, target) -> {
            var held = user.getMainHandStack();
            if (!held.isOf(WatheItems.REVOLVER)) return false;
            if (!target.getInventory().insertStack(held.copyWithCount(1))) return false;
            held.decrement(1);
            return true;
        });
        revolverKit(Roles.CHESS_PLAYER);

        revolverKit(Roles.RAILWAY_POLICE);
        revolverKit(Roles.JUDGE);
        def(Roles.JUDGE).multiTargeted(RoleDef.distinctTargets(1, 24), MultiTargetAbilities::judge);
        revolverKit(Roles.HUNTER);
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
        def(Roles.HARLOT).fizzleCooldown(CD_30S).kit(knife()).targeted((user, target) -> {
            var ability = AbilityState.KEY.get(user);
            if (ability.cooldown > 0) return;
            PlayerState.KEY.get(target).applyDrunk(CD_30S);
            PlayerState.KEY.get(target).sync();
            ability.setCooldown(CD_30S); ability.sync();
        }).initialize(p -> initialAbilityCd(p, CD_30S));

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
        def(Roles.LIBRARIAN).kit(knife()).targeted(TargetedAbilities::isolation)
                .initialize(p -> initialAbilityCd(p, CD_30S));
        def(Roles.WEREWOLF).kit(knife()).killerVictory(p -> dev.hae.btt.component.GameState.KEY.get(p.getWorld()).misfireCount >= 3);
        def(Roles.DAMSEL).opening(Damsel::opening).onDeath(Damsel::died);
        def(Roles.SAINT).onDeath((victim, killer, reason) -> {
            if (!Drunk.isDrunk(victim)
                    && dev.hae.btt.trait.TraitRules.passengerExecution(victim, killer, reason))
                dev.hae.btt.victory.EndingPipeline.factionLoss(victim);
        });
        def(Roles.SMUGGLER).self(Smuggler::use).onDeath((victim, killer, reason) -> Smuggler.releaseOwned(victim))
                .initialize(p -> initialAbilityCd(p, CD_30S));
        def(Roles.MAID).onTick(Maid::tick);
        def(Roles.NOBLE);


        def(Roles.REVOLUCIONARIO);
        def(Roles.BARBER).kit(knife()).anyDeath(Investigations::barberDeath).picks(2).multiTargeted((caster, picks) -> RoleDef.distinctTargets(2, 2).test(caster, picks)
                        && dev.hae.btt.component.GameState.KEY.get(caster.getWorld()).roleEffects.getBoolean("barber:" + caster.getUuidAsString()), Investigations::barber);

        def(Roles.DELINQUENT).kit(knife());



        def(Roles.TRAITOR);
        def(Roles.HERMIT).initialize(p -> { PlayerState.KEY.get(p).monkShields++; PlayerState.KEY.get(p).sync(); });


        def(Roles.CABALLERO).kit(p -> p.giveItemStack(new ItemStack(Items.BOW)));
        def(Roles.XIA).kit(p -> p.giveItemStack(new ItemStack(Items.BOW)));
        def(Roles.PSYCHOPATH).kit(p -> giveWithInitialCd(p, BttItems.FIRE_AXE));
        def(Roles.DETECTIVE).picks(2).multiTargeted(RoleDef.distinctTargets(2, 2), Investigations::deduce).targeted(TargetedAbilities::detective).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.INVESTIGATOR).targeted(TargetedAbilities::investigator).initialize(p -> initialAbilityCd(p, CD_1MIN));


        def(Roles.MEDIUM).targeted(Medium::retain).initialize(p -> initialAbilityCd(p, CD_30S));

        def(Roles.EXECUTIONER).targeted(Executioner::mark).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.VIRGIN).targeted(TargetedAbilities::virginHonor).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.STAR).targeted(TargetedAbilities::starMark).initialize(p -> initialAbilityCd(p, CD_30S));

        def(Roles.TOYMAKER).self(dev.hae.btt.role.Toymaker::poisonNext).fizzleCooldown(CD_1MIN).kit(p -> giveWithInitialCd(p, BttItems.BEAR_TRAP));
        def(Roles.LAWYER).targeted(TargetedAbilities::lawyer).initialize(p -> {
            initialAbilityCd(p, CD_1MIN);
        }).kit(p -> {
            giveWithInitialCd(p, WatheItems.KNIFE);
        });

        def(Roles.PHILOSOPHER).untargeted(RoleSelectionAbilities::philosopher).initialize(p -> initialAbilityCd(p, CD_1MIN));
        def(Roles.HIKIKOMORI).wake(LucidDream::begin);
        def(Roles.PSYCHIC).kit(p -> giveWithInitialCd(p, BttItems.AMULET)).onTick(Psychic::tick);


        def(Roles.PUPPETEER).multiTargeted(RoleDef.distinctTargets(2, 2), Puppeteer::manipulate).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.ASTRAL).untargeted(user -> Astral.toggle(user, dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(user.getWorld())));

        def(Roles.MESSENGER).targeted(ColdWave::diagnose).initialize(p -> initialAbilityCd(p, CD_1MIN));

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


        def(Roles.PHARMACIST).targeted(TargetedAbilities::pharmacist).initialize(p -> initialAbilityCd(p, CD_1MIN));
        def(Roles.SCRIBE).self(Scribe::read).fizzleCooldown(CD_30S);
        def(Roles.ENGINEER).untargeted(UntargetedAbilities::engineer).kit(p -> giveWithInitialCd(p, WatheItems.LOCKPICK)).initialize(p -> initialAbilityCd(p, CD_1MIN));
        def(Roles.ARCHITECT).untargeted(Architect::open).initialize(p -> initialAbilityCd(p, CD_1MIN));

        def(Roles.MUYUUBYOU).untargeted(SpiritWalk::toggle).wake(SpiritWalk::beginTimed).onTick((player, world, gwc) -> SpiritWalk.tick(player, gwc));

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








        def(Roles.TLAMACAZQUI).knifeUse(player -> {
            var state = PlayerState.KEY.get(player);
            if (--state.priestKnifeUses <= 0) removeOne(player, WatheItems.KNIFE);
        });

        def(Roles.WITCH).knifeUse(player -> {
            if (--PlayerState.KEY.get(player).witchUses <= 0) removeOne(player, WatheItems.KNIFE);
        });





        def(Roles.RAILWAY_POLICE).onTick((player, world, gwc) ->
                PlayerMoodComponent.KEY.get(player).setMood(1.0f));


        def(Roles.CONDUCTOR).opening((player, assigned) -> dev.hae.btt.game.OpeningNotices.direction(player, false));
        def(Roles.ATTENDANT).opening((player, assigned) -> dev.hae.btt.game.OpeningNotices.direction(player, true)).onTick((player, world, gwc) -> {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                dev.doctor4t.wathe.cca.GameTimeComponent.KEY.get(world).addTime(-1);
            }
        });


        def(Roles.NIGHT_WATCHMAN).onTick((player, world, gwc) -> {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
            dev.hae.btt.trait.TraitStatusEffects.maintain(player,
                    net.minecraft.entity.effect.StatusEffects.NIGHT_VISION, true);
        });


        def(Roles.JESTER).anyDeath(Jester::death).onTick((player, world, gwc) -> {
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
                Roles.KIDNAPPER, Roles.IMP, Roles.INVESTIGATOR, Roles.EXECUTIONER, Roles.MESSENGER,
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
