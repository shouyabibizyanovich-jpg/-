package com.optimizermod.settings;

import com.optimizermod.OptimizerModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;

import java.util.Objects;

/**
 * Единственное место, где мод читает и меняет настройки Minecraft (GameOptions).
 * Меняются только реально существующие параметры, и только если значение отличается.
 */
public final class GameSettingsApplier {

    /** В Minecraft 260 FPS означает «без лимита». */
    public static final int FPS_UNLIMITED = 260;

    private static boolean dirty;

    private GameSettingsApplier() {
    }

    /** Максимум дальности прорисовки так же, как у самой игры: 32 только для 64-бит Java и >= 1 ГБ памяти. */
    public static int maxViewDistance() {
        return is64Bit() && Runtime.getRuntime().maxMemory() >= 1000L * 1024L * 1024L ? 32 : 16;
    }

    private static boolean is64Bit() {
        for (String key : new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"}) {
            String value = System.getProperty(key);
            if (value != null && value.contains("64")) {
                return true;
            }
        }
        return false;
    }

    public static GraphicsSettings readCurrent() {
        GameOptions o = MinecraftClient.getInstance().options;
        GraphicsSettings s = new GraphicsSettings();
        s.viewDistance = o.getViewDistance().getValue();
        s.simulationDistance = o.getSimulationDistance().getValue();
        s.graphics = fromGraphicsMode(o.getGraphicsMode().getValue());
        s.particles = fromParticles(o.getParticles().getValue());
        s.clouds = fromClouds(o.getCloudRenderMode().getValue());
        s.entityShadows = o.getEntityShadows().getValue();
        s.biomeBlend = o.getBiomeBlendRadius().getValue();
        s.maxFps = o.getMaxFps().getValue();
        s.vsync = o.getEnableVsync().getValue();
        return s;
    }

    public static void apply(GraphicsSettings s) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return;
        }
        GameOptions o = client.options;
        try {
            set(o.getViewDistance(), clamp(s.viewDistance, 2, maxViewDistance()));
            set(o.getSimulationDistance(), clamp(s.simulationDistance, 5, 32));
            set(o.getGraphicsMode(), toGraphicsMode(s.graphics));
            set(o.getParticles(), toParticles(s.particles));
            set(o.getCloudRenderMode(), toClouds(s.clouds));
            set(o.getEntityShadows(), s.entityShadows);
            set(o.getBiomeBlendRadius(), clamp(s.biomeBlend, 0, 7));
            set(o.getMaxFps(), clamp(s.maxFps, 10, FPS_UNLIMITED));
            set(o.getEnableVsync(), s.vsync);
            dirty = true;
        } catch (RuntimeException e) {
            OptimizerModClient.LOGGER.error("Failed to apply graphics settings", e);
        }
    }

    /** Записывает options.txt, если мы что-то меняли. Вызывается при закрытии меню, а не каждый кадр. */
    public static void saveIfDirty() {
        if (!dirty) {
            return;
        }
        dirty = false;
        try {
            MinecraftClient.getInstance().options.write();
        } catch (RuntimeException e) {
            OptimizerModClient.LOGGER.error("Failed to save options", e);
        }
    }

    private static <T> void set(SimpleOption<T> option, T value) {
        if (!Objects.equals(option.getValue(), value)) {
            option.setValue(value);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static GraphicsMode toGraphicsMode(int v) {
        if (v <= 0) return GraphicsMode.FAST;
        if (v == 1) return GraphicsMode.FANCY;
        return GraphicsMode.FABULOUS;
    }

    private static int fromGraphicsMode(GraphicsMode mode) {
        if (mode == GraphicsMode.FAST) return 0;
        if (mode == GraphicsMode.FANCY) return 1;
        return 2;
    }

    private static ParticlesMode toParticles(int v) {
        if (v <= 0) return ParticlesMode.ALL;
        if (v == 1) return ParticlesMode.DECREASED;
        return ParticlesMode.MINIMAL;
    }

    private static int fromParticles(ParticlesMode mode) {
        if (mode == ParticlesMode.ALL) return 0;
        if (mode == ParticlesMode.DECREASED) return 1;
        return 2;
    }

    private static CloudRenderMode toClouds(int v) {
        if (v <= 0) return CloudRenderMode.OFF;
        if (v == 1) return CloudRenderMode.FAST;
        return CloudRenderMode.FANCY;
    }

    private static int fromClouds(CloudRenderMode mode) {
        if (mode == CloudRenderMode.OFF) return 0;
        if (mode == CloudRenderMode.FAST) return 1;
        return 2;
    }
}
