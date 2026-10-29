package com.optimizermod;

import com.optimizermod.gui.OptimizerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главная точка входа клиентского мода OptimizerMod.
 * Вызывается Fabric только на клиенте.
 */
public final class OptimizerModClient implements ClientModInitializer {

    public static final String MOD_ID = "optimizermod";
    public static final Logger LOGGER = LoggerFactory.getLogger("OptimizerMod");

    /** Клавиша открытия меню. Переназначается в настройках управления Minecraft. */
    public static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("OptimizerMod initializing (Minecraft 1.20.1, Fabric)");

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.optimizermod.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.optimizermod"
        ));

        // Проверка клавиши раз в тик, а не каждый кадр.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new OptimizerScreen());
                }
            }
        });

        // Следующие этапы:
        // 6. Config   -> загрузка настроек
        // 3. Profiles -> применение профиля
        // 4. HUD
        // 5. AutoOptimizer

        LOGGER.info("OptimizerMod initialized");
    }
}
