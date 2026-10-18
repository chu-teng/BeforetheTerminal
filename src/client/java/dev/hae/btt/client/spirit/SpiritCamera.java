package dev.hae.btt.client.spirit;

import com.mojang.authlib.GameProfile;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import dev.hae.btt.client.mixin.game.KeyBindingAccessor;

import java.util.Collections;
import java.util.UUID;
















public class SpiritCamera extends ClientPlayerEntity {

    private static final MinecraftClient MC = MinecraftClient.getInstance();


    static final double MAX_RADIUS = 30.0;


    private static final float FLY_SPEED = 0.07f * (0.09f / 0.4f);


    private static final double VERTICAL_SPEED = 0.2;

    private double bodyX;
    private double bodyY;
    private double bodyZ;

    private static final ClientPlayNetworkHandler DUMMY_HANDLER = new ClientPlayNetworkHandler(
            MC,
            MC.getNetworkHandler().getConnection(),
            new ClientConnectionState(
                    new GameProfile(UUID.randomUUID(), "SpiritCamera"),
                    MC.getTelemetryManager().createWorldSession(false, null, null),
                    DynamicRegistryManager.Immutable.EMPTY,
                    FeatureSet.empty(),
                    null,
                    MC.getCurrentServerEntry(),
                    MC.currentScreen,
                    Collections.emptyMap(),
                    MC.inGameHud.getChatHud().toChatState(),
                    false,
                    Collections.emptyMap(),
                    ServerLinks.EMPTY
            )
    ) {
        @Override
        public void sendPacket(Packet<?> packet) {

        }
    };

    public SpiritCamera(int id) {
        super(MC, MC.world, DUMMY_HANDLER, MC.player.getStatHandler(), MC.player.getRecipeBook(), false, false);
        setId(id);
        setPose(EntityPose.SWIMMING);
        setNoGravity(true);
        this.noClip = true;
        getAbilities().flying = true;
        input = new KeyboardInput(MC.options);
    }

    public void applyPosition(Entity entity) {
        double y = getSwimmingY(entity);
        refreshPositionAndAngles(entity.getX(), y, entity.getZ(), entity.getYaw(), entity.getPitch());
        renderPitch = getPitch();
        renderYaw = getYaw();
        lastRenderPitch = renderPitch;
        lastRenderYaw = renderYaw;
    }


    private static double getSwimmingY(Entity entity) {
        if (entity.getPose() == EntityPose.SWIMMING) {
            return entity.getY();
        }
        return entity.getY() - entity.getEyeHeight(EntityPose.SWIMMING) + entity.getEyeHeight(entity.getPose());
    }

    public void spawn() {
        if (clientWorld != null) {
            clientWorld.addEntity(this);
        }
    }

    public void despawn() {
        if (clientWorld != null && clientWorld.getEntityById(getId()) != null) {
            clientWorld.removeEntity(getId(), RemovalReason.DISCARDED);
        }
    }

    public void setBodyPosition(double x, double y, double z) {
        this.bodyX = x;
        this.bodyY = y;
        this.bodyZ = z;
    }



    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public boolean isTouchingWater() {
        return false;
    }


    @Override
    public boolean isSwimming() {
        return false;
    }

    @Override
    protected void onSwimmingStart() {
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public boolean shouldSlowDown() {
        return false;
    }

    @Override
    public void setPose(EntityPose pose) {
        super.setPose(EntityPose.SWIMMING);
    }


    @Override
    public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
        return MC.player.getStatusEffect(effect);
    }



    @Override
    public void tickMovement() {



        this.noClip = true;
        getAbilities().flying = true;
        getAbilities().setFlySpeed(FLY_SPEED);

        double beforeY = getY();
        super.tickMovement();


        double want = 0;
        if (jumpHeld()) want += VERTICAL_SPEED;
        if (input.sneaking) want -= VERTICAL_SPEED;
        setPosition(getX(), beforeY + want, getZ());
        setVelocity(getVelocity().x, 0, getVelocity().z);
        setOnGround(false);

        clampPosition();
    }


    static boolean jumpHeld() {
        return ((KeyBindingAccessor) MC.options.jumpKey).btt$isPressedRaw();
    }


    private void clampPosition() {
        double newX = getX();
        double newY = getY();
        double newZ = getZ();
        boolean clamped = false;

        try {
            Box playArea = MapVariablesWorldComponent.KEY.get(MC.world).getPlayArea();
            if (playArea != null) {
                double cx = MathHelper.clamp(newX, playArea.minX, playArea.maxX);
                double cy = MathHelper.clamp(newY, playArea.minY, playArea.maxY);
                double cz = MathHelper.clamp(newZ, playArea.minZ, playArea.maxZ);
                if (cx != newX || cy != newY || cz != newZ) {
                    newX = cx;
                    newY = cy;
                    newZ = cz;
                    clamped = true;
                }
            }
        } catch (Exception ignored) {
        }

        double dx = newX - bodyX;
        double dy = newY - bodyY;
        double dz = newZ - bodyZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > MAX_RADIUS) {
            double scale = MAX_RADIUS / dist;
            newX = bodyX + dx * scale;
            newY = bodyY + dy * scale;
            newZ = bodyZ + dz * scale;
            clamped = true;
        }

        if (clamped) {
            setPosition(newX, newY, newZ);
        }
    }
}
