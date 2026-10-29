package com.optimizermod.settings;

import net.minecraft.text.Text;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

/**
 * Описание одного параметра в меню: тип (ползунок или переключатель), границы и текст.
 */
public enum GraphicsOption {

    VIEW_DISTANCE("view_distance", Kind.SLIDER, 2, 32, 1,
            s -> s.viewDistance, (s, v) -> s.viewDistance = v),
    SIMULATION_DISTANCE("simulation_distance", Kind.SLIDER, 5, 32, 1,
            s -> s.simulationDistance, (s, v) -> s.simulationDistance = v),
    GRAPHICS("graphics", Kind.CYCLE, 0, 1, 1,
            s -> s.graphics, (s, v) -> s.graphics = v),
    PARTICLES("particles", Kind.CYCLE, 0, 2, 1,
            s -> s.particles, (s, v) -> s.particles = v),
    CLOUDS("clouds", Kind.CYCLE, 0, 2, 1,
            s -> s.clouds, (s, v) -> s.clouds = v),
    ENTITY_SHADOWS("entity_shadows", Kind.CYCLE, 0, 1, 1,
            s -> s.entityShadows ? 1 : 0, (s, v) -> s.entityShadows = v != 0),
    BIOME_BLEND("biome_blend", Kind.SLIDER, 0, 7, 1,
            s -> s.biomeBlend, (s, v) -> s.biomeBlend = v),
    MAX_FPS("max_fps", Kind.SLIDER, 10, GameSettingsApplier.FPS_UNLIMITED, 10,
            s -> s.maxFps, (s, v) -> s.maxFps = v),
    VSYNC("vsync", Kind.CYCLE, 0, 1, 1,
            s -> s.vsync ? 1 : 0, (s, v) -> s.vsync = v != 0);

    public enum Kind {
        SLIDER,
        CYCLE
    }

    private final String id;
    private final Kind kind;
    private final int minValue;
    private final int maxValue;
    private final int step;
    private final ToIntFunction<GraphicsSettings> getter;
    private final BiConsumer<GraphicsSettings, Integer> setter;

    GraphicsOption(String id, Kind kind, int minValue, int maxValue, int step,
                   ToIntFunction<GraphicsSettings> getter, BiConsumer<GraphicsSettings, Integer> setter) {
        this.id = id;
        this.kind = kind;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.getter = getter;
        this.setter = setter;
    }

    public Kind kind() {
        return kind;
    }

    public int min() {
        return minValue;
    }

    public int max() {
        if (this == VIEW_DISTANCE) {
            return GameSettingsApplier.maxViewDistance();
        }
        return maxValue;
    }

    public int get(GraphicsSettings s) {
        return getter.applyAsInt(s);
    }

    public void set(GraphicsSettings s, int value) {
        setter.accept(s, value);
    }

    public Text label() {
        return Text.translatable("optimizermod.option." + id);
    }

    public Text description() {
        return Text.translatable("optimizermod.option." + id + ".desc");
    }

    /** Следующее значение для переключателя (по кругу). */
    public int next(int current) {
        return current >= max() ? min() : current + step;
    }

    /** Значение ползунка по положению мыши (0.0 .. 1.0), с округлением до шага. */
    public int fromRatio(double ratio) {
        int lo = min();
        int hi = max();
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        double raw = lo + clamped * (hi - lo);
        int value = lo + (int) Math.round((raw - lo) / step) * step;
        return Math.max(lo, Math.min(hi, value));
    }

    public double ratioOf(int value) {
        int lo = min();
        int hi = max();
        if (hi <= lo) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (value - lo) / (double) (hi - lo)));
    }

    public Text format(int v) {
        switch (this) {
            case GRAPHICS:
                return Text.translatable(v == 0 ? "optimizermod.value.fast"
                        : v == 1 ? "optimizermod.value.fancy" : "optimizermod.value.fabulous");
            case PARTICLES:
                return Text.translatable(v == 0 ? "optimizermod.value.particles_all"
                        : v == 1 ? "optimizermod.value.particles_decreased" : "optimizermod.value.particles_minimal");
            case CLOUDS:
                return Text.translatable(v == 0 ? "optimizermod.value.off"
                        : v == 1 ? "optimizermod.value.clouds_fast" : "optimizermod.value.clouds_fancy");
            case ENTITY_SHADOWS:
            case VSYNC:
                return Text.translatable(v != 0 ? "optimizermod.value.on" : "optimizermod.value.off");
            case BIOME_BLEND: {
                if (v == 0) {
                    return Text.translatable("optimizermod.value.off");
                }
                int n = v * 2 + 1;
                return Text.literal(n + "x" + n);
            }
            case MAX_FPS:
                return v >= GameSettingsApplier.FPS_UNLIMITED
                        ? Text.translatable("optimizermod.value.unlimited")
                        : Text.literal(String.valueOf(v));
            default:
                return Text.literal(String.valueOf(v));
        }
    }
}
