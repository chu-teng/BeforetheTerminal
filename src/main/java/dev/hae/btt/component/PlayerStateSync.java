package dev.hae.btt.component;

import net.minecraft.nbt.NbtCompound;


public final class PlayerStateSync {
    private PlayerStateSync() {}

    public static NbtCompound forOwner(NbtCompound saved) {
        NbtCompound view = saved.copy();
        view.remove("tenaciousKiller");
        view.remove("jesterExecutioner");
        view.remove("kins");
        view.remove("balloonReps");
        view.remove("advancementRound");
        view.remove("drunkTicks");
        view.remove("philosopherSources");
        view.remove("openingNoticeAt");
        view.remove("openingNoticeRole");
        view.remove("openingNoticeLabel");
        view.remove("initialRole");
        view.remove("decoyRole");
        String prior = saved.getString("priorRole");
        if ("btt:marionette".equals(prior) || "btt:lunatic".equals(prior)) {
            view.putString("priorRole", "");
            view.putInt("priorRoleTicks", 0);
            view.putString("campOverride", "");
        }
        return view;
    }

    public static NbtCompound forOwner(NbtCompound saved, net.minecraft.server.network.ServerPlayerEntity viewer) {
        NbtCompound view = forOwner(saved);
        view.putInt("novelistRequired", (viewer.getServerWorld().getPlayers().size() + 2) / 3);
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(viewer.getWorld());
        view.putInt("observedPassiveColor", dev.hae.btt.perception.PlayerObservation.passiveColor(game, viewer, viewer));
        var role = dev.hae.btt.role.Decoy.displayRole(viewer, dev.hae.btt.role.Roles.realRole(game, viewer));
        view.putString("observedGuideRole", game.getGameStatus() == dev.doctor4t.wathe.cca.GameWorldComponent.GameStatus.STARTING
                || dev.hae.btt.encounter.Memory.isBlind(viewer) || role == null
                ? "" : role.identifier().toString());
        if (dev.hae.btt.perception.NativeRoleView.fullObserver(viewer)) {
            for (String key : new String[]{"priorRole", "campOverride"})
                if (saved.contains(key)) view.put(key, saved.get(key).copy());
        }
        return view;
    }

    public static NbtCompound forTracking(NbtCompound saved) {
        NbtCompound view = new NbtCompound();

        for (String key : new String[]{"voicePitchTicks", "fakeDead", "swallowedBy"}) {
            if (saved.contains(key)) view.put(key, saved.get(key).copy());
        }
        if (view.contains("voicePitchTicks")) view.putInt("voicePitchTicks", saved.getInt("voicePitchTicks") > 0 ? 1 : 0);
        if (saved.contains("voodooPhase")) view.putInt("voodooPhase", saved.getInt("voodooPhase") == 1 ? 1 : 0);
        return view;
    }

    public static NbtCompound forPlayer(NbtCompound saved, net.minecraft.server.network.ServerPlayerEntity target,
                                       net.minecraft.server.network.ServerPlayerEntity viewer) {
        NbtCompound view = forTracking(saved);
        if (dev.hae.btt.perception.NativeRoleView.fullObserver(viewer)) {
            for (String key : new String[]{"priorRole", "campOverride"})
                if (saved.contains(key)) view.put(key, saved.get(key).copy());
        }
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(target.getWorld());
        if (dev.hae.btt.role.Roles.isPassengerCampFor(game, viewer))
            view.putInt("starGlowTicks", saved.getInt("starGlowTicks") > 0 ? 1 : 0);
        dev.hae.btt.perception.PlayerObservation.append(view, target, viewer);
        if (game.getRole(viewer) == dev.hae.btt.role.Roles.VOODOO)
            view.putString("voodooOwner", saved.getString("voodooOwner"));
        return view;
    }
}
