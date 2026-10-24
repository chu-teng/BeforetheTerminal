package dev.hae.btt.voice;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.packets.SoundPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;


final class VoiceLoudness {
    private VoiceLoudness() {}
    private record Channel(UUID sender, UUID channel) {}
    private static final class Decoder {
        final OpusDecoder opus;
        long sequence = Long.MIN_VALUE;
        double rms;
        Decoder(OpusDecoder opus) { this.opus = opus; }
    }
    private static final Map<Channel, Decoder> DECODERS = new HashMap<>();

    static void heard(VoicechatServerApi api, ServerPlayerEntity listener, SoundPacket packet) {
        if (!dev.hae.btt.role.Librarian.mayBeIsolated(listener.getUuid())) return;
        double rms;
        synchronized (DECODERS) {
            var key = new Channel(packet.getSender(), packet.getChannelId());
            var decoder = DECODERS.computeIfAbsent(key, ignored -> new Decoder(api.createDecoder()));
            if (packet.getSequenceNumber() != decoder.sequence) {
                try {
                    short[] pcm = decoder.opus.decode(packet.getOpusEncodedData());
                    double squares = 0;
                    for (short sample : pcm) { double value = sample / 32768.0; squares += value * value; }
                    decoder.rms = pcm.length == 0 ? 0 : Math.sqrt(squares / pcm.length);
                } catch (RuntimeException ignored) { decoder.rms = 0; }
                decoder.sequence = packet.getSequenceNumber();
            }
            rms = decoder.rms;
        }
        double measured = rms;
        listener.getServer().execute(() -> {
            var speaker = listener.getServerWorld().getPlayerByUuid(packet.getSender());
            if (!(speaker instanceof ServerPlayerEntity source)) return;
            double attenuation = 1;
            if (packet instanceof de.maxhenkel.voicechat.api.packets.LocationalSoundPacket located) {
                var position = located.getPosition();
                double distance = Math.sqrt(listener.squaredDistanceTo(position.getX(), position.getY(), position.getZ()));
                attenuation = located.getDistance() <= 0 ? 0 : Math.max(0, 1 - distance / located.getDistance());
            } else if (packet instanceof de.maxhenkel.voicechat.api.packets.EntitySoundPacket entity) {
                var origin = listener.getServerWorld().getPlayerByUuid(entity.getEntityUuid());
                attenuation = origin == null || entity.getDistance() <= 0 ? 0
                        : Math.max(0, 1 - listener.distanceTo(origin) / entity.getDistance());
            }
            dev.hae.btt.role.Librarian.heard(source, listener, measured * attenuation);
        });
    }

    static void disconnect(UUID player) {
        synchronized (DECODERS) {
            DECODERS.entrySet().removeIf(entry -> {
                if (!entry.getKey().sender().equals(player)) return false;
                entry.getValue().opus.close();
                return true;
            });
        }
    }

    static void clear() {
        synchronized (DECODERS) {
            DECODERS.values().forEach(decoder -> decoder.opus.close());
            DECODERS.clear();
        }
    }
}
