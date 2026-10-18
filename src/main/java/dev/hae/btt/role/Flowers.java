package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.registry.BttItems;


public final class Flowers {
    private Flowers() {}


    public static final int SEED_TICKS = 600;
    public static final int SPROUT_TICKS = 1200;
    public static final double PICK_RADIUS = 1.2;
    public static final double HINT_RADIUS = 20.0;


    public static final Item SPROUT_ITEM = Items.MANGROVE_PROPAGULE;

    public static final Item BLOOM_ITEM = Items.LILY_OF_THE_VALLEY;


    public static boolean isFlower(ItemStack stack) {
        return stack.isOf(SPROUT_ITEM) || stack.isOf(BLOOM_ITEM);
    }

    public static final class Flower {
        public ServerWorld world;
        public BlockPos pos;
        public UUID gardener;
        public int stage;
        public int stageTicks;
        public net.minecraft.entity.Entity entity;
    }

    private static final List<Flower> FLOWERS = new ArrayList<>();

    public static int count(ServerWorld world) {
        int n = 0;
        for (Flower f : FLOWERS) if (f.world == world) n++;
        return n;
    }

    public static void clear() {
        for (Flower f : FLOWERS) discard(f);
        FLOWERS.clear();
    }


    public static void forgetTransientState() { FLOWERS.clear(); }
    public static void clearWorld(ServerWorld world) {
        for (var flower : new ArrayList<>(FLOWERS)) if (flower.world == world) { discard(flower); FLOWERS.remove(flower); }
        for (var bloom : world.getEntitiesByType(dev.hae.btt.registry.BttEntities.BLOOM, entity -> true)) bloom.discard();
    }


    public static boolean plant(ServerPlayerEntity gardener) {
        ServerWorld world = gardener.getServerWorld();
        for (Flower f : FLOWERS) {
            if (f.world == world && f.pos.getSquaredDistance(gardener.getBlockPos()) < 20 * 20) {
                return false;
            }
        }
        Flower f = new Flower();
        f.world = world;
        f.pos = gardener.getBlockPos();
        f.gardener = gardener.getUuid();
        f.stage = 0;
        f.stageTicks = 0;
        FLOWERS.add(f);
        return true;
    }


    public static void tick(ServerWorld world) {
        List<Flower> mine = new ArrayList<>();
        for (Flower f : FLOWERS) if (f.world == world) mine.add(f);
        for (Flower f : mine) {
            if (f.stage == 2 && f.entity != null) f.pos = f.entity.getBlockPos();
            f.stageTicks++;
            if (f.stage == 0 && f.stageTicks >= SEED_TICKS) {
                f.stage = 1;
                f.stageTicks = 0;
                spawnEntity(f, new ItemStack(SPROUT_ITEM));
                world.playSound(null, f.pos, Sounds.FLOWER_GROW,
                        net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);

                hintNearby(f, net.minecraft.text.Text.translatable("btt.action.gardener.nearby"));
            } else if (f.stage == 1 && f.stageTicks >= SPROUT_TICKS) {
                f.stage = 2;
                f.stageTicks = 0;
                discard(f);
                var bloom = new dev.hae.btt.entity.BloomEntity(dev.hae.btt.registry.BttEntities.BLOOM, world);
                bloom.setGardener(f.gardener);
                bloom.refreshPositionAndAngles(f.pos.getX() + 0.5, f.pos.getY(), f.pos.getZ() + 0.5, 0, 0);
                world.spawnEntity(bloom);
                f.entity = bloom;
                world.playSound(null, f.pos, Sounds.FLOWER_GROW,
                        net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.8F);
            }
            if (f.entity != null && f.entity.isRemoved()) { FLOWERS.remove(f); continue; }


            if (f.stage == 1) {
                if (nearestPlayer(f) != null) {
                    discard(f);
                    FLOWERS.remove(f);
                }
                continue;
            }

        }
    }

    private static ServerPlayerEntity nearestPlayer(Flower f) {
        ServerPlayerEntity best = null;
        double bestDist = PICK_RADIUS * PICK_RADIUS;
        for (ServerPlayerEntity p : f.world.getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (p.getUuid().equals(f.gardener)) continue;
            double d = p.getBlockPos().getSquaredDistance(f.pos);
            if (d <= bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private static void hintNearby(Flower f, net.minecraft.text.Text message) {
        for (ServerPlayerEntity p : f.world.getPlayers()) {
            if (p.getBlockPos().getSquaredDistance(f.pos) <= HINT_RADIUS * HINT_RADIUS) {

                p.sendMessage(message.copy().formatted(net.minecraft.util.Formatting.GRAY), true);
            }
        }
    }

    private static void spawnEntity(Flower f, ItemStack stack) {
        if (f.entity != null) f.entity.discard();
        ItemEntity entity = new ItemEntity(f.world,
                f.pos.getX() + 0.5, f.pos.getY() + 0.5, f.pos.getZ() + 0.5, stack);
        entity.setPickupDelay(Integer.MAX_VALUE);
        f.world.spawnEntity(entity);
        f.entity = entity;
    }

    private static void discard(Flower f) {
        if (f.entity != null) f.entity.discard();
    }
}
