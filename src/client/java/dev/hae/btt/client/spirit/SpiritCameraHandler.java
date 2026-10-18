package dev.hae.btt.client.spirit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import dev.hae.btt.component.PlayerState;











public final class SpiritCameraHandler {
    private SpiritCameraHandler() {}

    private static final MinecraftClient MC = MinecraftClient.getInstance();


    private static final int CAMERA_ID = -421;

    private static SpiritCamera spiritCamera;
    private static boolean active = false;

    private static boolean inputFrozen = false;

    public static boolean isActive() {
        return active;
    }

    public static SpiritCamera getSpiritCamera() {
        return spiritCamera;
    }

    public static void enable() {
        if (active || MC.player == null || MC.world == null) return;

        MC.chunkCullingEnabled = false;
        MC.gameRenderer.setRenderHand(false);

        PlayerState pc = PlayerState.KEY.get(MC.player);
        spiritCamera = new SpiritCamera(CAMERA_ID);
        spiritCamera.setBodyPosition(pc.bodyX, pc.bodyY, pc.bodyZ);
        spiritCamera.applyPosition(MC.player);
        spiritCamera.spawn();
        MC.setCameraEntity(spiritCamera);

        active = true;
    }

    public static void disable() {
        if (!active) return;

        MC.chunkCullingEnabled = true;
        MC.gameRenderer.setRenderHand(true);
        if (MC.player != null) {
            MC.setCameraEntity(MC.player);
        }
        if (spiritCamera != null) {
            spiritCamera.despawn();
            spiritCamera.input = new Input();
            spiritCamera = null;
        }
        if (MC.player != null) {
            MC.player.input = new KeyboardInput(MC.options);
        }
        inputFrozen = false;

        active = false;
    }


    public static void tick() {
        if (MC.player == null) {
            inputFrozen = false;
            return;
        }

        boolean projecting = PlayerState.KEY.get(MC.player).isProjecting();
        if (projecting && !active) {
            enable();
        } else if (!projecting && active) {
            disable();
        }


        PlayerState selfPc = PlayerState.KEY.get(MC.player);
        boolean fakeDead = selfPc.isFakeDead();

        boolean swallowed = selfPc.isSwallowed();
        boolean freeze = active || fakeDead || swallowed;
        if (freeze) {



            if (!inputFrozen && MC.player.input instanceof KeyboardInput) {
                Input frozen = new Input();
                frozen.sneaking = MC.player.input.sneaking;
                MC.player.input = frozen;
                inputFrozen = true;
            }
            if (active) MC.gameRenderer.setRenderHand(false);
        } else if (inputFrozen) {


            MC.player.input = new KeyboardInput(MC.options);
            inputFrozen = false;
        }
    }
}
