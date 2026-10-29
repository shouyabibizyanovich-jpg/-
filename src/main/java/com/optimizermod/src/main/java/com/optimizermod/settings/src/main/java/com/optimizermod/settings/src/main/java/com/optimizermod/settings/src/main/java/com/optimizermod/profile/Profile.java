package com.optimizermod.profile;

import com.optimizermod.settings.GraphicsSettings;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Профили производительности. Порядок полей GraphicsSettings:
 * дальность прорисовки, дальность симуляции, графика (0 быстрая / 1 красивая),
 * частицы (0 все / 1 меньше / 2 минимум), облака (0 выкл / 1 быстрые / 2 красивые),
 * тени сущностей, сглаживание биомов, макс. FPS (260 = без лимита), VSync.
 */
public enum Profile {

    POTATO("potato", new GraphicsSettings(2, 5, 0, 2, 0, false, 0, 260, false)),
    LOW("low", new GraphicsSettings(6, 6, 0, 1, 0, false, 1, 260, false)),
    MEDIUM("medium", new GraphicsSettings(10, 8, 1, 1, 1, true, 2, 120, false)),
    HIGH("high", new GraphicsSettings(16, 12, 1, 0, 2, true, 4, 120, false)),
    CUSTOM("custom", null);

    /** Профили с фиксированными значениями (без «Пользовательского»). */
    public static final List<Profile> PRESETS = List.of(POTATO, LOW, MEDIUM, HIGH);

    private final String id;
    private final GraphicsSettings preset;

    Profile(String id, GraphicsSettings preset) {
        this.id = id;
        this.preset = preset;
    }

    public String id() {
        return id;
    }

    public boolean isPreset() {
        return preset != null;
    }

    /** Копия значений профиля или null для «Пользовательского». */
    public GraphicsSettings preset() {
        return preset == null ? null : preset.copy();
    }

    public Text title() {
        return Text.translatable("optimizermod.profile." + id);
    }

    public Text description() {
        return Text.translatable("optimizermod.profile." + id + ".desc");
    }
}
