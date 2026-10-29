package com.optimizermod.gui;

import com.optimizermod.OptimizerModClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Главное меню OptimizerMod: заголовок, вкладки, область содержимого.
 * Рисуется вручную (без виджетов Minecraft), чтобы выглядеть аккуратно и не зависеть от версии.
 */
public final class OptimizerScreen extends Screen {

    private enum Tab {
        PROFILES("profiles", 3),
        GRAPHICS("graphics", 3),
        HUD("hud", 4),
        AUTO("auto", 5);

        final String id;
        final int stage;

        Tab(String id, int stage) {
            this.id = id;
            this.stage = stage;
        }

        Text title() {
            return Text.translatable("optimizermod.tab." + id);
        }

        Text description() {
            return Text.translatable("optimizermod.tab." + id + ".desc");
        }
    }

    private static final Tab[] TABS = Tab.values();

    private static final int HEADER_H = 28;
    private static final int TAB_H = 24;

    private static final int ACCENT = 0xFF3DDC84;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int MUTED = 0xFFA0A8B0;
    private static final int PANEL_BG = 0xF0141A22;
    private static final int HEADER_BG = 0xFF0E131A;
    private static final int TAB_ACTIVE_BG = 0xFF1E2833;
    private static final int TAB_HOVER_BG = 0xFF19222C;
    private static final int CLOSE_HOVER_BG = 0xFFB3261E;

    /** Запоминаем последнюю открытую вкладку до перезапуска игры. */
    private static Tab currentTab = Tab.PROFILES;

    private int px, py, pw, ph;

    public OptimizerScreen() {
        super(Text.translatable("optimizermod.screen.title"));
    }

    @Override
    protected void init() {
        pw = Math.min(this.width - 16, 380);
        ph = Math.min(this.height - 16, 230);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
    }

    @Override
    public boolean shouldPause() {
        // Игра идёт, пока меню открыто: изменения графики будет видно сразу.
        return false;
    }

    // ---------- геометрия ----------

    private int tabW() {
        return pw / TABS.length;
    }

    private int tabX(int i) {
        return px + i * tabW();
    }

    private int tabY() {
        return py + HEADER_H;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private boolean overClose(double mx, double my) {
        return inside(mx, my, px + pw - HEADER_H, py, HEADER_H, HEADER_H);
    }

    // ---------- отрисовка ----------

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // затемнение мира и панель
        context.fill(0, 0, this.width, this.height, 0x99000000);
        context.fill(px, py, px + pw, py + ph, PANEL_BG);

        // шапка
        context.fill(px, py, px + pw, py + HEADER_H, HEADER_BG);
        context.fill(px, py + HEADER_H - 1, px + pw, py + HEADER_H, ACCENT);
        context.drawTextWithShadow(this.textRenderer, this.title.copy().formatted(Formatting.BOLD),
                px + 10, py + (HEADER_H - 8) / 2, ACCENT);

        // кнопка закрытия
        boolean closeHover = overClose(mouseX, mouseY);
        if (closeHover) {
            context.fill(px + pw - HEADER_H, py, px + pw, py + HEADER_H - 1, CLOSE_HOVER_BG);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("X"),
                px + pw - HEADER_H / 2, py + (HEADER_H - 8) / 2, TEXT);

        // вкладки
        for (int i = 0; i < TABS.length; i++) {
            Tab tab = TABS[i];
            int x = tabX(i);
            int y = tabY();
            int w = tabW();
            boolean active = tab == currentTab;
            boolean hover = inside(mouseX, mouseY, x, y, w, TAB_H);

            if (active) {
                context.fill(x, y, x + w, y + TAB_H, TAB_ACTIVE_BG);
                context.fill(x, y + TAB_H - 2, x + w, y + TAB_H, ACCENT);
            } else if (hover) {
                context.fill(x, y, x + w, y + TAB_H, TAB_HOVER_BG);
            }

            Text label = active ? tab.title().copy().formatted(Formatting.BOLD) : tab.title();
            context.drawCenteredTextWithShadow(this.textRenderer, label,
                    x + w / 2, y + (TAB_H - 8) / 2, active ? TEXT : MUTED);
        }

        // содержимое вкладки
        int cx = px + 14;
        int cy = tabY() + TAB_H + 14;
        context.drawTextWithShadow(this.textRenderer, currentTab.title().copy().formatted(Formatting.BOLD),
                cx, cy, ACCENT);

        List<OrderedText> lines = this.textRenderer.wrapLines(currentTab.description(), pw - 28);
        int y = cy + 16;
        for (OrderedText line : lines) {
            context.drawTextWithShadow(this.textRenderer, line, cx, y, TEXT);
            y += 11;
        }
        y += 6;
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("optimizermod.tab.placeholder", currentTab.stage), cx, y, MUTED);

        // подсказка внизу (показывает актуальную клавишу, даже если её переназначили)
        Text hint = Text.translatable("optimizermod.screen.close_hint",
                OptimizerModClient.openMenuKey.getBoundKeyLocalizedText());
        context.drawTextWithShadow(this.textRenderer, hint, px + 14, py + ph - 16, MUTED);

        super.render(context, mouseX, mouseY, delta);
    }

    // ---------- ввод ----------

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Та же клавиша, что открывает меню, его и закрывает (учитывает переназначение).
        if (OptimizerModClient.openMenuKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (overClose(mouseX, mouseY)) {
                click();
                this.close();
                return true;
            }
            for (int i = 0; i < TABS.length; i++) {
                if (inside(mouseX, mouseY, tabX(i), tabY(), tabW(), TAB_H)) {
                    if (TABS[i] != currentTab) {
                        currentTab = TABS[i];
                        click();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void click() {
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
