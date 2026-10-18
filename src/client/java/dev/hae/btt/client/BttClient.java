package dev.hae.btt.client;

import com.google.common.collect.Maps;
import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.util.WatheItemTooltips;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.network.AbilityC2SPacket;
import dev.hae.btt.network.CorpseEatC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import dev.hae.btt.client.ui.AbilityKey;
import dev.hae.btt.registry.BttEntities;

public class BttClient implements ClientModInitializer {

    public static int insanityTime = 0;
    public static KeyBinding abilityBind;

    public static KeyBinding guideBind;

    public static KeyBinding instinctModeBind;

    public static int lastDebatePhase = 0;
    public static PlayerEntity target;
    public static PlayerBodyEntity targetBody;

    public static Map<UUID, UUID> SHUFFLED_PLAYER_ENTRIES_CACHE = Maps.newHashMap();

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BttEntities.BLOOM, net.minecraft.client.render.entity.ZombieEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(dev.hae.btt.network.AdminMenuS2CPacket.ID,
                (payload, context) -> context.client().execute(() -> context.client().setScreen(
                        new dev.hae.btt.client.ui.AdminSettingsScreen(payload))));
        dev.hae.btt.api.ClientBridge.memoryOpener = () -> {
            var client = MinecraftClient.getInstance();
            if (!dev.hae.btt.encounter.Memory.isBlind(client.player)
                    || client.player.getItemCooldownManager().isCoolingDown(dev.hae.btt.registry.BttItems.SLIP)
                    || dev.hae.btt.component.PlayerState.KEY.get(client.player).memoryNextGuess > client.world.getTime()) return;
            var pool = dev.hae.btt.role.Roles.allRoles().stream()
                    .map(dev.hae.btt.client.ui.RolePickScreen.Choice::of).toList();
            client.setScreen(new dev.hae.btt.client.ui.RolePickScreen(Text.translatable("btt.memory.slip"), List.of(), pool,
                    choice -> {
                        ClientPlayNetworking.send(new dev.hae.btt.network.MemoryGuessC2SPacket(choice.value()));
                        client.setScreen(null);
                    }, null));
        };
        abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + BeforeTheTerminal.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.wathe.keybinds"));

        guideBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + BeforeTheTerminal.MOD_ID + ".guide", InputUtil.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_K, "category.wathe.keybinds"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            dev.hae.btt.client.NewbieTips.tick(client);
            insanityTime++;
            if (insanityTime >= 20*6) {
                insanityTime = 0;
                List<UUID> keys = new ArrayList<UUID>(WatheClient.PLAYER_ENTRIES_CACHE.keySet());
                List<UUID> originalkeys = new ArrayList<UUID>(WatheClient.PLAYER_ENTRIES_CACHE.keySet());
                Collections.shuffle(keys);
                int i = 0;
                for (UUID o : originalkeys) {
                    SHUFFLED_PLAYER_ENTRIES_CACHE.put(o, keys.get(i));
                    i++;
                }
            }

            dev.hae.btt.client.spirit.SpiritCameraHandler.tick();

            dev.hae.btt.client.input.ScreenMovement.tick();

            if (guideBind.wasPressed()) client.setScreen(new dev.hae.btt.client.ui.GuideBookScreen());

            if (dev.hae.btt.client.InstinctMode.isToggleMode()
                    && dev.doctor4t.wathe.client.WatheClient.instinctKeybind.wasPressed()) {
                dev.hae.btt.client.InstinctMode.setToggledOn(!dev.hae.btt.client.InstinctMode.isToggledOn());
            }

            dev.hae.btt.client.ui.MapVoteScreen.tick(client);

            if (client.world != null) {
                int phase = dev.hae.btt.component.DebateState.KEY.get(client.world).phase;
                if (phase == dev.hae.btt.component.DebateState.PHASE_VOTING
                        && lastDebatePhase != dev.hae.btt.component.DebateState.PHASE_VOTING) {
                    dev.hae.btt.client.ui.LetterScreen.open(client);
                }
                lastDebatePhase = phase;
            }





            if (abilityBind.wasPressed() && client.world != null) {
                if (dev.hae.btt.client.ui.AbilityKey.tryDebateVote(client)) {

                } else if (dev.hae.btt.component.MapVoteState.KEY.get(client.world).active) {
                    client.setScreen(new dev.hae.btt.client.ui.MapVoteScreen());
                } else if (dev.hae.btt.game.SeatAssignment.isBttMode(client.world)) {
                    dev.hae.btt.client.ui.AbilityKey.handlePress(client);
                }
            }
        });

        EntityRendererRegistry.register(BttEntities.POISON_GAS_BOMB, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(BttEntities.POISON_GAS_CLOUD, EmptyEntityRenderer::new);

        EntityRendererRegistry.register(BttEntities.FLYING_SWORD,
                dev.hae.btt.client.renderer.FlyingSwordEntityRenderer::new);




        dev.hae.btt.client.renderer.DoorHighlightRenderer.register();




        dev.hae.btt.client.util.BttItemTooltips.register();

        dev.hae.btt.client.NewbieTips.register();

        dev.hae.btt.client.AggressionTint.register();



        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(
                dev.hae.btt.registry.BttBlocks.LANTERN_TRAP, net.minecraft.client.render.RenderLayer.getCutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(
                dev.hae.btt.registry.BttBlocks.WIRE_TRAP, net.minecraft.client.render.RenderLayer.getCutout());
        dev.hae.btt.api.ClientBridge.consoleOpener = () ->
                MinecraftClient.getInstance().setScreen(new dev.hae.btt.client.ui.ConsoleScreen());

        ClientPlayNetworking.registerGlobalReceiver(dev.hae.btt.network.DoorHighlightS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        dev.hae.btt.client.renderer.DoorHighlightRenderer
                                .onHighlight(payload.pos(), payload.blasted())));

        ClientPlayNetworking.registerGlobalReceiver(dev.hae.btt.network.WelcomeLineS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        dev.hae.btt.client.util.AnnouncePremise.acceptServerLine(
                                payload.kind(), payload.arg1(), payload.arg2())));

    }

    public void tooltipHelper(Item item, ItemStack itemStack, List<Text> list) {
        if (itemStack.isOf(item)) {
            list.addAll(TextUtils.getTooltipForItem(item, Style.EMPTY.withColor(WatheItemTooltips.REGULAR_TOOLTIP_COLOR)));
        }
    }
}
