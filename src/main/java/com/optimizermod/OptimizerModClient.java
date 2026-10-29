package com.optimizermod;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главная точка входа клиентского мода OptimizerMod.
 * Вызывается Fabric только на клиенте.
 */
public final class OptimizerModClient implements ClientModInitializer {

    public static final String MOD_ID = "optimizermod";
    public static final Logger LOGGER = LoggerFactory.getLogger("OptimizerMod");

    @Override
    public void onInitializeClient() {
        LOGGER.info("OptimizerMod initializing (Minecraft 1.20.1, Fabric)");

        // Порядок инициализации по этапам (пока пусто):
        // 6. Config  -> загрузка настроек
        // 3. Profiles -> применение профиля
        // 2. Keybinds + Screen -> меню на Right Shift
        // 4. HUD
        // 5. AutoOptimizer

        LOGGER.info("OptimizerMod initialized");
    }
}
