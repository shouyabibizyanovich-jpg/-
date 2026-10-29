package com.optimizermod.settings;

import java.util.Objects;

/**
 * Набор графических параметров, которыми управляет мод.
 * Простой изменяемый контейнер без привязки к классам Minecraft.
 */
public final class GraphicsSettings {

    public int viewDistance;        // дальность прорисовки, чанки
    public int simulationDistance;  // дальность симуляции, чанки (5..32)
    public int graphics;            // 0 = быстрая, 1 = красивая, 2 = fabulous
    public int particles;           // 0 = все, 1 = меньше, 2 = минимум
    public int clouds;              // 0 = выкл, 1 = быстрые, 2 = красивые
    public boolean entityShadows;
    public int biomeBlend;          // 0..7
    public int maxFps;              // 10..260 (260 = без лимита)
    public boolean vsync;

    public GraphicsSettings() {
    }

    public GraphicsSettings(int viewDistance, int simulationDistance, int graphics, int particles,
                            int clouds, boolean entityShadows, int biomeBlend, int maxFps, boolean vsync) {
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
        this.graphics = graphics;
        this.particles = particles;
        this.clouds = clouds;
        this.entityShadows = entityShadows;
        this.biomeBlend = biomeBlend;
        this.maxFps = maxFps;
        this.vsync = vsync;
    }

    public GraphicsSettings copy() {
        return new GraphicsSettings(viewDistance, simulationDistance, graphics, particles,
                clouds, entityShadows, biomeBlend, maxFps, vsync);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphicsSettings that)) return false;
        return viewDistance == that.viewDistance
                && simulationDistance == that.simulationDistance
                && graphics == that.graphics
                && particles == that.particles
                && clouds == that.clouds
                && entityShadows == that.entityShadows
                && biomeBlend == that.biomeBlend
                && maxFps == that.maxFps
                && vsync == that.vsync;
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewDistance, simulationDistance, graphics, particles,
                clouds, entityShadows, biomeBlend, maxFps, vsync);
    }
}
