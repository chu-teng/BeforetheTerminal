package dev.hae.btt.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.Packet;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.component.PlayerState;

public class VoiceChatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return BeforeTheTerminal.MOD_ID;
    }


    private static VoicechatServerApi serverApi;

    private static final java.util.Map<java.util.UUID, de.maxhenkel.voicechat.api.Group> STOMACH_GROUPS = new java.util.HashMap<>();

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi server) serverApi = server;
        VoicechatPlugin.super.initialize(api);
    }

    @Override
    public void registerEvents(de.maxhenkel.voicechat.api.events.EventRegistration registration) {
        registration.registerEvent(de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent.class,
                event -> serverApi = event.getVoicechat());
        registration.registerEvent(MicrophonePacketEvent.class, this::mutedSpeakerEvent, 100);
        registration.registerEvent(MicrophonePacketEvent.class, event -> {
            if (event.isCancelled()) return;
            ServerPlayerEntity speaker = serverPlayerOf(event.getSenderConnection());
            if (speaker != null) speaker.getServer().execute(() -> {
                if (dev.hae.btt.trait.TraitRules.active(speaker) && GameFunctions.isPlayerAliveAndSurvival(speaker))
                    PlayerState.KEY.get(speaker).speakingUntil = speaker.getServerWorld().getTime() + 10;
            });
            walkieTalkieEvent(event);
            broadcastEvent(event);
        });
        registration.registerEvent(EntitySoundPacketEvent.class, this::mutedListenerEvent, 100);
        registration.registerEvent(LocationalSoundPacketEvent.class, this::mutedListenerEvent, 100);
        registration.registerEvent(de.maxhenkel.voicechat.api.events.StaticSoundPacketEvent.class, this::mutedListenerEvent, 100);
    }








    public static void joinStomach(ServerPlayerEntity kidnapper, ServerPlayerEntity victim) {
        if (serverApi == null) return;
        de.maxhenkel.voicechat.api.Group group = STOMACH_GROUPS.get(kidnapper.getUuid());

        if (group != null && serverApi.getGroup(group.getId()) == null) {
            STOMACH_GROUPS.remove(kidnapper.getUuid());
            group = null;
        }
        if (group == null) {
            String name = kidnapper.getName().getString();
            if (name.length() > 12) name = name.substring(0, 12);
            group = serverApi.groupBuilder()
                    .setPersistent(false)
                    .setHidden(true)
                    .setType(de.maxhenkel.voicechat.api.Group.Type.NORMAL)
                    .setName("btt-" + name)
                    .build();
            STOMACH_GROUPS.put(kidnapper.getUuid(), group);
        }
        VoicechatConnection connection = serverApi.getConnectionOf(victim.getUuid());
        if (connection != null) connection.setGroup(group);
    }

    public static void detachRemnant(ServerPlayerEntity player) {
        if (serverApi == null) return;
        var connection = serverApi.getConnectionOf(player.getUuid());
        if (connection != null && connection.getGroup() != null) connection.setGroup(null);
    }


    public static void leaveStomach(ServerPlayerEntity victim) {
        if (serverApi == null) return;
        VoicechatConnection connection = serverApi.getConnectionOf(victim.getUuid());
        if (connection != null) connection.setGroup(null);
    }






    private static void registerClientVoiceHooks(EventRegistration registration) {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()
                != net.fabricmc.api.EnvType.CLIENT) return;
        try {
            Class<?> receiver = Class.forName("dev.hae.btt.client.voice.BttVoiceClientReceiver");
            receiver.getMethod("register", EventRegistration.class).invoke(null, registration);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to wire BTT voice receiver", e);
        }
    }








    public void walkieTalkieEvent(MicrophonePacketEvent event) {
        ServerPlayerEntity speaker = serverPlayerOf(event.getSenderConnection());
        if (speaker == null) return;
        if (!SeatAssignment.isBttMode(speaker.getWorld())) return;

        if (!GameFunctions.isPlayerAliveAndSurvival(speaker)
                && !dev.hae.btt.role.BlackDeath.isBodylessPathogen(speaker)) return;
        if (!dev.hae.btt.item.WalkieTalkieItem.isHeld(speaker)) return;
        VoicechatServerApi api = event.getVoicechat();
        double near = api.getVoiceChatDistance();
        for (ServerPlayerEntity listener : speaker.getServerWorld().getPlayers()) {
            if (listener == speaker) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(listener)
                    && !dev.hae.btt.role.BlackDeath.isBodylessPathogen(listener)) continue;
            if (!dev.hae.btt.item.WalkieTalkieItem.isCarried(listener)) continue;
            if (speaker.squaredDistanceTo(listener) <= near * near) continue;
            VoicechatConnection con = api.getConnectionOf(listener.getUuid());
            if (con == null) continue;
            api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
                    .position(api.createPosition(listener.getX(), listener.getY(), listener.getZ()))
                    .distance(8f)
                    .build());
        }
    }






    public void broadcastEvent(MicrophonePacketEvent event) {
        ServerPlayerEntity speaker = serverPlayerOf(event.getSenderConnection());
        if (speaker == null) return;
        if (!dev.hae.btt.voice.ConsoleBroadcast.isBroadcasting(speaker)) return;
        VoicechatServerApi api = event.getVoicechat();
        for (ServerPlayerEntity listener : speaker.getServerWorld().getPlayers()) {
            if (listener == speaker) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(listener)) continue;
            VoicechatConnection con = api.getConnectionOf(listener.getUuid());
            if (con == null) continue;
            api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
                    .position(api.createPosition(speaker.getX(), speaker.getY(), speaker.getZ()))
                    .distance((float) api.getVoiceChatDistance())
                    .build());
        }
    }


    public void mutedSpeakerEvent(MicrophonePacketEvent event) {
        ServerPlayerEntity speaker = serverPlayerOf(event.getSenderConnection());
        if (speaker == null) return;
        if (PlayerState.KEY.get(speaker).cageDeadline > speaker.getWorld().getTime()) { event.cancel(); return; }
        if (!SeatAssignment.isBttMode(speaker.getWorld())) return;
        if (PlayerState.KEY.get(speaker).isMuted()) { event.cancel(); return; }
        boolean remnant = dev.hae.btt.role.Voodoo.isRemnant(speaker);
        var api = event.getVoicechat();
        for (var listener : speaker.getServerWorld().getPlayers()) {
            if (speaker == listener) continue;
            boolean relay = remnant ? dev.hae.btt.role.Voodoo.hearsRemnant(listener, speaker)
                    : dev.hae.btt.role.Voodoo.isRemnant(listener) && dev.hae.btt.role.Voodoo.hearsLiving(listener, speaker);
            if (!relay) continue;
            var connection = api.getConnectionOf(listener.getUuid());
            if (connection != null) api.sendLocationalSoundPacketTo(connection, event.getPacket().locationalSoundPacketBuilder()
                    .position(api.createPosition(listener.getX(), listener.getY(), listener.getZ())).distance(8f).build());
        }
        if (remnant) event.cancel();
    }


    public <T extends Packet> void mutedListenerEvent(SoundPacketEvent<T> event) {
        ServerPlayerEntity recipient = serverPlayerOf(event.getReceiverConnection());
        if (recipient == null) return;
        if (PlayerState.KEY.get(recipient).cageDeadline > recipient.getWorld().getTime()) { event.cancel(); return; }
        if (!SeatAssignment.isBttMode(recipient.getWorld())) return;
        if (PlayerState.KEY.get(recipient).isMuted()) { event.cancel(); return; }
        if (event.getPacket() instanceof de.maxhenkel.voicechat.api.packets.SoundPacket sound) {
            var sender = recipient.getServerWorld().getPlayerByUuid(sound.getSender());
            if (sender instanceof ServerPlayerEntity speaker) {
                if (dev.hae.btt.role.Voodoo.isRemnant(speaker)
                        && !dev.hae.btt.role.Voodoo.hearsRemnant(recipient, speaker)) event.cancel();
                if (dev.hae.btt.role.Voodoo.isRemnant(recipient)
                        && !dev.hae.btt.role.Voodoo.hearsLiving(recipient, speaker)
                        && !dev.hae.btt.role.Voodoo.isRemnant(speaker)) event.cancel();
            } else if (dev.hae.btt.role.Voodoo.isRemnant(recipient)) event.cancel();
        }
    }

    private static ServerPlayerEntity serverPlayerOf(VoicechatConnection connection) {
        if (connection == null || connection.getPlayer() == null) return null;
        return connection.getPlayer().getPlayer() instanceof ServerPlayerEntity player ? player : null;
    }
}
