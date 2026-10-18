package dev.hae.btt.perception;

import net.minecraft.nbt.NbtCompound;


public final class NativeEffectView {
    private NativeEffectView() {}

    public static NbtCompound poison(NbtCompound saved, boolean owner) {
        NbtCompound view = new NbtCompound();
        if (owner) {
            view.putInt("poisonTicks", saved.getInt("poisonTicks"));
            view.putInt("initialPoisonTicks", saved.getInt("initialPoisonTicks"));
        }
        return view;
    }

    public static NbtCompound psycho(NbtCompound saved, boolean owner) {
        if (owner) return saved.copy();
        NbtCompound view = new NbtCompound();

        view.putInt("psychoTicks", saved.getInt("psychoTicks") > 0 ? 2 : 0);
        return view;
    }
}
