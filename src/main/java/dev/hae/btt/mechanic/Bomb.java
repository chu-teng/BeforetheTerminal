package dev.hae.btt.mechanic;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Roles;
import dev.hae.btt.registry.BttItems;






public final class Bomb {
    private Bomb() {}


    public static final int QUIET_TICKS = 100;

    public static final int COUNTDOWN_TICKS = 300;

    public static final int TRANSFER_CD_TICKS = 60;

    public static final int PLACE_CD_TICKS = 600;


    public static boolean place(ServerPlayerEntity user, ServerPlayerEntity target) {
        if (target == user) return false;
        PlayerState tc = PlayerState.KEY.get(target);
        if (tc.bombPlaced) {
            return false;
        }
        tc.bombPlaced = true;
        tc.bombBeeping = false;
        tc.bombTimer = QUIET_TICKS;
        tc.bombSource = user.getUuid().toString();
        tc.bombLastSec = -1;
        tc.sync();
        return true;
    }


    public static boolean transfer(ServerPlayerEntity holder, ServerPlayerEntity target) {
        PlayerState hc = PlayerState.KEY.get(holder);
        if (!hc.bombPlaced || !hc.bombBeeping || hc.bombTransferCd > 0) return false;
        if (target == holder || !GameFunctions.isPlayerAliveAndSurvival(target)) return false;
        PlayerState tc = PlayerState.KEY.get(target);
        if (tc.bombPlaced) return false;
        tc.bombPlaced = true;
        tc.bombBeeping = true;
        tc.beepTimer = hc.beepTimer;
        tc.bombSource = hc.bombSource;
        tc.bombLastSec = -1;
        tc.bombTransferCd = TRANSFER_CD_TICKS;
        hc.bombPlaced = false;
        hc.bombBeeping = false;
        removeBombItem(holder);
        giveBombItem(target);

        if (GameWorldComponent.KEY.get(holder.getWorld()).isRole(holder, Roles.TERRORIST)) giveBombItem(holder);
        hc.sync();
        tc.sync();
        holder.getWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        return true;
    }




    public static void reveal(ServerPlayerEntity carrier) {
        giveBombItem(carrier);
    }


    public static void clear(ServerPlayerEntity carrier) {
        removeBombItem(carrier);
    }


    private static void giveBombItem(ServerPlayerEntity player) {
        if (countBombItem(player) == 0) player.giveItemStack(new ItemStack(BttItems.BOMB));
    }

    private static void removeBombItem(ServerPlayerEntity player) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isOf(BttItems.BOMB)) {
                inventory.removeStack(i, 1);
                break;
            }
        }
    }

    private static int countBombItem(ServerPlayerEntity player) {
        var inventory = player.getInventory();
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isOf(BttItems.BOMB)) count++;
        }
        return count;
    }

    public static void tickWorld(net.minecraft.server.world.ServerWorld world) {
        for (var p : world.getPlayers()) {
            PlayerState bc = PlayerState.KEY.get(p);
            if (bc.bombTransferCd > 0) bc.bombTransferCd--;
            if (!bc.bombPlaced) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) {
                bc.bombPlaced = false;
                bc.bombBeeping = false;
                Bomb.clear(p);
                bc.sync();
                continue;
            }
            if (!bc.bombBeeping) {
                if (bc.bombTimer > 0) bc.bombTimer--;
                else {
                    bc.bombBeeping = true;
                    bc.beepTimer = Bomb.COUNTDOWN_TICKS;
                    Bomb.reveal(p);
                bc.sync();
                }
            } else if (bc.beepTimer > 0) {
                if (bc.beepTimer % 6 == 0) {

                    world.playSound(null, p.getBlockPos(), Sounds.BOMB_BEEP,
                            net.minecraft.sound.SoundCategory.PLAYERS, 2.0F, 1.0F);
                }
                int sec = (bc.beepTimer + 19) / 20;
                if (sec != bc.bombLastSec) {
                    bc.bombLastSec = sec;

                    p.sendMessage(net.minecraft.text.Text.translatable("btt.action.bomb.countdown", sec)
                            .formatted(net.minecraft.util.Formatting.BOLD)
                            .withColor(dev.hae.btt.role.Roles.TERRORIST.color()), true);
                }
                bc.beepTimer--;
            } else {
                bc.bombPlaced = false;
                bc.bombBeeping = false;
                Bomb.clear(p);
                bc.sync();

                world.playSound(null, p.getBlockPos(), dev.doctor4t.wathe.index.WatheSounds.ITEM_GRENADE_EXPLODE,
                        net.minecraft.sound.SoundCategory.PLAYERS, 3.0F, 1.0F);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION,
                        p.getX(), p.getY() + 0.5, p.getZ(), 2, 0, 0, 0, 0);
                ServerPlayerEntity bomber = null;
                if (!bc.bombSource.isEmpty() && world.getPlayerByUuid(java.util.UUID.fromString(bc.bombSource)) instanceof ServerPlayerEntity s) {
                    bomber = s;
                }
                GameFunctions.killPlayer(p, true, bomber, DeathReasons.BOMB);
            }
        }
    }

}
