package com.optimizermod.gui;

import com.optimizermod.OptimizerModClient;
import com.optimizermod.profile.Profile;
import com.optimizermod.profile.ProfileManager;
import com.optimizermod.settings.GameSettingsApplier;
import com.optimizermod.settings.GraphicsOption;
import com.optimizermod.settings.GraphicsSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Главное меню OptimizerMod: вкладки «Профили» и «Графика» уже рабочие,
 * «HUD» и «Авто» пока заглушки (этапы 4 и 5).
 * Рисуется вручную, без виджетов Minecraft.
 */
public final class OptimizerScreen extends Screen {

    private enum Tab {
        PROFILES("profiles", 0),
        GRAPHICS("graphics", 0),
        HUD("hud", 4),
        AUTO("auto", 5);

        final String id;
        final int stage; // 0 = вкладка уже готова

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
    private static final GraphicsOption[] OPTIONS = GraphicsOption.values();
    private static final Profile[] PROFILES = Profile.values();

    private static final int HEADER_H = 28;
    private static final int TAB_H = 24;
    private static final int CARD_H = 22;
    private static final int CARD_GAP = 2;
    private static final int RESET_H = 14;
    private static final long FLASH_MS = 2500L;

    private static final int ACCENT = 0xFF3DDC84;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int MUTED = 0xFFA0A8B0;
    private static final int PANEL_BG = 0xF0141A22;
    private static final int HEADER_BG = 0xFF0E131A;
    private static final int TAB_ACTIVE_BG = 0xFF1E2833;
    private static final int TAB_HOVER_BG = 0xFF19222C;
    private static final int CLOSE_HOVER_BG = 0xFFB3261E;
    private static final int BOX_BG = 0xFF1E2833;
    private static final int BOX_HOVER = 0xFF283645;
    private static final int SLIDER_FILL = 0xFF245C41;
    private static final int PILL_BG = 0xFF1F8A55;

    /** Запоминаем последнюю открытую вкладку до перезапуска игры. */
    private static Tab currentTab = Tab.PROFILES;

    private int px, py, pw, ph;

    /** Снимок текущих настроек игры, обновляется только после изменений, а не каждый кадр. */
    private GraphicsSettings snapshot;
    private GraphicsOption dragOption;
    private int dragValue;
    private long flashUntil;
    private Text hoverDesc;

    public OptimizerScreen() {
        super(Text.translatable("optimizermod.screen.title"));
    }

    @Override
    protected void init() {
        pw = Math.min(this.width - 16, 380);
        ph = Math.min(this.height - 8, 250);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
        ProfileManager.syncWithGame();
        snapshot = GameSettingsApplier.readCurrent();
    }

    @Override
    public boolean shouldPause() {
        // Игра идёт, пока меню открыто: изменения графики видно сразу.
        return false;
    }

    @Override
    public void removed() {
        dragOption = null;
        GameSettingsApplier.saveIfDirty();
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

    private int contentX() {
        return px + 14;
    }

    private int contentW() {
        return pw - 28;
    }

    private int contentTop() {
        return tabY() + TAB_H + 8;
    }

    private int labelW() {
        return contentW() / 2;
    }

    private int boxX() {
        return contentX() + labelW();
    }

    private int boxW() {
        return contentW() - labelW();
    }

    private int cardY(int i) {
        return contentTop() + i * (CARD_H + CARD_GAP);
    }

    private int rowH() {
        int avail = py + ph - 22 - contentTop() - RESET_H - 4;
        int h = avail / OPTIONS.length;
        return Math.max(11, Math.min(16, h));
    }

    private int resetY() {
        return contentTop() + OPTIONS.length * rowH() + 4;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private boolean overClose(double mx, double my) {
        return inside(mx, my, px + pw - HEADER_H, py, HEADER_H, HEADER_H);
    }

    private static void outline(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + 1, color);
        c.fill(x, y + h - 1, x + w, y + h, color);
        c.fill(x, y, x + 1, y + h, color);
        c.fill(x + w - 1, y, x + w, y + h, color);
    }

    // ---------- отрисовка ----------

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hoverDesc = null;

        context.fill(0, 0, this.width, this.height, 0x99000000);
        context.fill(px, py, px + pw, py + ph, PANEL_BG);

        renderHeader(context, mouseX, mouseY);
        renderTabs(context, mouseX, mouseY);

        switch (currentTab) {
            case PROFILES -> renderProfiles(context, mouseX, mouseY);
            case GRAPHICS -> renderGraphics(context, mouseX, mouseY);
            default -> renderPlaceholder(context);
        }

        // Внизу: пояснение к параметру под курсором или подсказка про закрытие.
        Text footer = hoverDesc != null
                ? hoverDesc
                : Text.translatable("optimizermod.screen.close_hint",
                        OptimizerModClient.openMenuKey.getBoundKeyLocalizedText());
        context.drawTextWithShadow(this.textRenderer, footer, px + 14, py + ph - 16, MUTED);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY) {
        int textY = py + (HEADER_H - 8) / 2;

        context.fill(px, py, px + pw, py + HEADER_H, HEADER_BG);
        context.fill(px, py + HEADER_H - 1, px + pw, py + HEADER_H, ACCENT);
        context.drawTextWithShadow(this.textRenderer, this.title.copy().formatted(Formatting.BOLD),
                px + 10, textY, ACCENT);

        // кнопка закрытия
        if (overClose(mouseX, mouseY)) {
            context.fill(px + pw - HEADER_H, py, px + pw, py + HEADER_H - 1, CLOSE_HOVER_BG);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("X"),
                px + pw - HEADER_H / 2, textY, TEXT);

        // активный профиль
        Text profileLabel = Text.translatable("optimizermod.screen.active_profile",
                ProfileManager.active().title());
        int labelW = this.textRenderer.getWidth(profileLabel);
        int labelX = px + pw - HEADER_H - 8 - labelW;
        context.drawTextWithShadow(this.textRenderer, profileLabel, labelX, textY, TEXT);

        // краткая метка «Применено» после выбора профиля или сброса
        if (System.currentTimeMillis() < flashUntil) {
            Text applied = Text.translatable("optimizermod.screen.applied");
            int aw = this.textRenderer.getWidth(applied);
            int ax = labelX - aw - 16;
            context.fill(ax - 5, py + 6, ax + aw + 5, py + HEADER_H - 7, PILL_BG);
            context.drawTextWithShadow(this.textRenderer, applied, ax, textY, TEXT);
        }
    }

    private void renderTabs(DrawContext context, int mouseX, int mouseY) {
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
    }

    private void renderProfiles(DrawContext context, int mouseX, int mouseY) {
        int x = contentX();
        int w = contentW();
        Profile activeProfile = ProfileManager.active();

        for (int i = 0; i < PROFILES.length; i++) {
            Profile p = PROFILES[i];
            int y = cardY(i);
            boolean active = p == activeProfile;
            boolean hover = inside(mouseX, mouseY, x, y, w, CARD_H);

            context.fill(x, y, x + w, y + CARD_H, hover ? BOX_HOVER : BOX_BG);
            if (active) {
                outline(context, x, y, w, CARD_H, ACCENT);
                context.fill(x, y, x + 3, y + CARD_H, ACCENT);
            }

            context.drawTextWithShadow(this.textRenderer, p.title().copy().formatted(Formatting.BOLD),
                    x + 10, y + 3, active ? ACCENT : TEXT);
            context.drawTextWithShadow(this.textRenderer, p.description(), x + 10, y + 12, MUTED);

            if (active) {
                Text badge = Text.translatable("optimizermod.profile.active");
                context.drawTextWithShadow(this.textRenderer, badge,
                        x + w - 8 - this.textRenderer.getWidth(badge), y + 3, ACCENT);
            }
        }
    }

    private void renderGraphics(DrawContext context, int mouseX, int mouseY) {
        int x = contentX();
        int w = contentW();
        int bx = boxX();
        int bw = boxW();
        int top = contentTop();
        int rowH = rowH();

        for (int i = 0; i < OPTIONS.length; i++) {
            GraphicsOption opt = OPTIONS[i];
            int y = top + i * rowH;
            boolean hover = inside(mouseX, mouseY, x, y, w, rowH);
            if (hover) {
                hoverDesc = opt.description();
            }

            int value = dragOption == opt ? dragValue : opt.get(snapshot);
            int textY = y + (rowH - 8) / 2;

            context.drawTextWithShadow(this.textRenderer, opt.label(), x + 2, textY, TEXT);

            int boxTop = y + 1;
            int boxBottom = y + rowH - 1;
            context.fill(bx, boxTop, bx + bw, boxBottom, hover ? BOX_HOVER : BOX_BG);
            if (opt.kind() == GraphicsOption.Kind.SLIDER) {
                int fillW = (int) (bw * opt.ratioOf(value));
                context.fill(bx, boxTop, bx + fillW, boxBottom, SLIDER_FILL);
            }
            context.drawCenteredTextWithShadow(this.textRenderer, opt.format(value),
                    bx + bw / 2, textY, TEXT);
        }

        // кнопка сброса к профилю
        int ry = resetY();
        boolean resetHover = inside(mouseX, mouseY, x, ry, w, RESET_H);
        context.fill(x, ry, x + w, ry + RESET_H, resetHover ? BOX_HOVER : BOX_BG);
        outline(context, x, ry, w, RESET_H, ACCENT);
        Text reset = Text.translatable("optimizermod.screen.reset", ProfileManager.resetTarget().title());
        context.drawCenteredTextWithShadow(this.textRenderer, reset, x + w / 2, ry + (RESET_H - 8) / 2, TEXT);
    }

    private void renderPlaceholder(DrawContext context) {
        int cx = contentX();
        int cy = contentTop() + 6;
        context.drawTextWithShadow(this.textRenderer, currentTab.title().copy().formatted(Formatting.BOLD),
                cx, cy, ACCENT);

        List<OrderedText> lines = this.textRenderer.wrapLines(currentTab.description(), contentW());
        int y = cy + 16;
        for (OrderedText line : lines) {
            context.drawTextWithShadow(this.textRenderer, line, cx, y, TEXT);
            y += 11;
        }
        y += 6;
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("optimizermod.tab.placeholder", currentTab.stage), cx, y, MUTED);
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
            if (currentTab == Tab.PROFILES && clickProfiles(mouseX, mouseY)) {
                return true;
            }
            if (currentTab == Tab.GRAPHICS && clickGraphics(mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickProfiles(double mx, double my) {
        for (int i = 0; i < PROFILES.length; i++) {
            if (inside(mx, my, contentX(), cardY(i), contentW(), CARD_H)) {
                ProfileManager.select(PROFILES[i]);
                afterApply();
                return true;
            }
        }
        return false;
    }

    private boolean clickGraphics(double mx, double my) {
        int rowH = rowH();
        for (int i = 0; i < OPTIONS.length; i++) {
            int y = contentTop() + i * rowH;
            if (!inside(mx, my, contentX(), y, contentW(), rowH)) {
                continue;
            }
            GraphicsOption opt = OPTIONS[i];
            if (opt.kind() == GraphicsOption.Kind.SLIDER) {
                // Ползунок: значение показывается сразу, применяется при отпускании.
                if (mx >= boxX()) {
                    dragOption = opt;
                    dragValue = opt.fromRatio((mx - boxX()) / boxW());
                }
            } else {
                ProfileManager.change(opt, opt.next(opt.get(snapshot)));
                snapshot = GameSettingsApplier.readCurrent();
                click();
            }
            return true;
        }
        if (inside(mx, my, contentX(), resetY(), contentW(), RESET_H)) {
            ProfileManager.resetToPreset();
            afterApply();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragOption != null && button == 0) {
            dragValue = dragOption.fromRatio((mouseX - boxX()) / boxW());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragOption != null && button == 0) {
            GraphicsOption opt = dragOption;
            int value = dragValue;
            dragOption = null;
            if (value != opt.get(snapshot)) {
                ProfileManager.change(opt, value);
                snapshot = GameSettingsApplier.readCurrent();
                click();
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void afterApply() {
        snapshot = GameSettingsApplier.readCurrent();
        flashUntil = System.currentTimeMillis() + FLASH_MS;
        click();
    }

    private void click() {
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
