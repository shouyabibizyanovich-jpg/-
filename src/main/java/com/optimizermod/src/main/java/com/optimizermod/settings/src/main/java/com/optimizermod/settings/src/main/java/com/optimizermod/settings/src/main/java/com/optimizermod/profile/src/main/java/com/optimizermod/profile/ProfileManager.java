package com.optimizermod.profile;

import com.optimizermod.OptimizerModClient;
import com.optimizermod.settings.GameSettingsApplier;
import com.optimizermod.settings.GraphicsOption;
import com.optimizermod.settings.GraphicsSettings;

/**
 * Хранит активный профиль и применяет изменения через GameSettingsApplier.
 * Пока состояние только в памяти, сохранение в файл появится на этапе конфигурации.
 */
public final class ProfileManager {

    private static Profile active = Profile.CUSTOM;
    private static Profile lastPreset = Profile.MEDIUM;
    private static GraphicsSettings custom;

    private ProfileManager() {
    }

    public static Profile active() {
        return active;
    }

    /** Профиль, к которому вернёт кнопка сброса. */
    public static Profile resetTarget() {
        return lastPreset;
    }

    /**
     * Сверяет профиль с реальными настройками игры (их могли изменить в ванильном меню).
     * Вызывается при открытии меню.
     */
    public static void syncWithGame() {
        GraphicsSettings now = GameSettingsApplier.readCurrent();
        if (active.isPreset()) {
            if (active.preset().equals(now)) {
                return;
            }
        } else if (custom != null && custom.equals(now)) {
            return;
        }
        for (Profile p : Profile.PRESETS) {
            if (p.preset().equals(now)) {
                active = p;
                lastPreset = p;
                return;
            }
        }
        active = Profile.CUSTOM;
        custom = now;
    }

    /** Выбор профиля из меню: значения сразу применяются в игре. */
    public static void select(Profile profile) {
        if (profile.isPreset()) {
            GameSettingsApplier.apply(profile.preset());
            lastPreset = profile;
        } else if (custom != null) {
            GameSettingsApplier.apply(custom);
        } else {
            custom = GameSettingsApplier.readCurrent();
        }
        active = profile;
        OptimizerModClient.LOGGER.info("Profile selected: {}", profile.id());
    }

    /** Ручное изменение одного параметра: профиль становится «Пользовательским». */
    public static void change(GraphicsOption option, int value) {
        GraphicsSettings s = GameSettingsApplier.readCurrent();
        option.set(s, value);
        GameSettingsApplier.apply(s);
        custom = GameSettingsApplier.readCurrent();
        active = Profile.CUSTOM;
    }

    /** Возврат к значениям последнего выбранного готового профиля. */
    public static void resetToPreset() {
        select(lastPreset);
    }
}
