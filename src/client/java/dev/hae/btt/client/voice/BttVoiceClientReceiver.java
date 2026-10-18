package dev.hae.btt.client.voice;

import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.component.PlayerState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;










public final class BttVoiceClientReceiver {

    private static final float RATIO = 1.35f;
    private static final float RAMP_OUT_TICKS = 10f;

    private static final Map<UUID, BttVoiceShifter> SHIFTERS = new ConcurrentHashMap<>();

    private BttVoiceClientReceiver() {}

    public static void register(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, BttVoiceClientReceiver::onEntitySound);
    }

    private static void onEntitySound(ClientReceiveSoundEvent.EntitySound event) {
        if (event.isCancelled()) return;
        short[] pcm = event.getRawAudio();
        if (pcm == null || pcm.length == 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || !SeatAssignment.isBttMode(client.world)) return;
        UUID speaker = event.getEntityId();
        if (speaker == null) return;
        PlayerEntity player = client.world.getPlayerByUuid(speaker);
        if (player == null) {
            SHIFTERS.remove(speaker);
            return;
        }
        PlayerState component = PlayerState.KEY.get(player);
        if (component.voicePitchTicks <= 0) {
            SHIFTERS.remove(speaker);
            return;
        }
        float ratio = RATIO;
        if (component.voicePitchTicks < RAMP_OUT_TICKS) {
            ratio = 1.0f + (RATIO - 1.0f) * (component.voicePitchTicks / RAMP_OUT_TICKS);
        }
        BttVoiceShifter shifter = SHIFTERS.computeIfAbsent(speaker, id -> new BttVoiceShifter());
        event.setRawAudio(shifter.process(pcm, ratio));
    }
}
