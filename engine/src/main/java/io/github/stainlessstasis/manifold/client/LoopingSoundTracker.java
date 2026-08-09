package io.github.stainlessstasis.manifold.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class LoopingSoundTracker<T> {
    private final Map<T, WeakReference<AbstractTickableSoundInstance>> active = new WeakHashMap<>();

    public void playIfNeeded(T owner, boolean shouldPlay, Supplier<AbstractTickableSoundInstance> factory) {
        if (!shouldPlay) return;

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        WeakReference<AbstractTickableSoundInstance> ref = active.get(owner);
        AbstractTickableSoundInstance existing = ref != null ? ref.get() : null;
        if (existing != null && soundManager.isActive(existing)) return;

        AbstractTickableSoundInstance instance = factory.get();
        soundManager.play(instance);
        active.put(owner, new WeakReference<>(instance));
    }
}