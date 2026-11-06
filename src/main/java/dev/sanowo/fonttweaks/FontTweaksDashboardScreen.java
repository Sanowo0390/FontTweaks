package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Zoomify-style two-pane settings dashboard.
 *
 * Layout: top category tabs, scrollable left settings list with per-row reset,
 * right help/editor/live-preview pane, and fixed bottom action bar. At narrow
 * GUI dimensions the right pane becomes an accessible full-width detail view.
 * Does not depend on Zoomify or a configuration UI library.
 */
public final class FontTweaksDashboardScreen extends Screen implements PreviewScaleSource {
    private static final int EDGE = 8;
    private static final int TAB_Y = 18;
    private static final int TAB_HEIGHT = 20;
    private static final int LIST_TOP = 47;
    private static final int ROW_HEIGHT = 26;

    private enum Tab {
        SIZE("fonttweaks.ui.tab.size"),
        SPACING("fonttweaks.ui.tab.spacing"),
        PIXELS("fonttweaks.ui.tab.pixels"),
        FONTS("fonttweaks.ui.tab.fonts"),
        BLACKLIST("fonttweaks.ui.tab.blacklist");

        private final String key;
        Tab(String key) { this.key = key; }
    }

    private enum NumberSetting {
        GLOBAL("fonttweaks.global", "fonttweaks.ui.desc.global"),
        SCREEN("fonttweaks.screen", "fonttweaks.ui.desc.screen"),
        HUD("fonttweaks.hud", "fonttweaks.ui.desc.hud"),
        BUTTON("fonttweaks.button", "fonttweaks.ui.desc.button"),
        CHAT("fonttweaks.chat", "fonttweaks.ui.desc.chat"),
        GLOBAL_LINE("fonttweaks.global_spacing", "fonttweaks.ui.desc.global_line"),
        WRAPPED_LINE("fonttweaks.spacing", "fonttweaks.ui.desc.wrapped"),
        TOOLTIP_LINE("fonttweaks.tooltip_spacing", "fonttweaks.ui.desc.tooltip");

        private final String title;
        private final String help;
        NumberSetting(String title, String help) {
            this.title = title;
            this.help = help;
        }

        double get(FontTweaksSettings cfg) {
            return switch (this) {
                case GLOBAL -> cfg.globalScale;
                case SCREEN -> cfg.screenScale;
                case HUD -> cfg.hudScale;
                case BUTTON -> cfg.buttonScale;
                case CHAT -> cfg.chatScale;
                case GLOBAL_LINE -> cfg.globalLineSpacing;
                case WRAPPED_LINE -> cfg.wrappedLineSpacing;
                case TOOLTIP_LINE -> cfg.tooltipLineSpacing;
            };
        }

        void set(FontTweaksSettings cfg, double value) {
            switch (this) {
                case GLOBAL -> cfg.globalScale = value;
                case SCREEN -> cfg.screenScale = value;
                case HUD -> cfg.hudScale = value;
                case BUTTON -> cfg.buttonScale = value;
                case CHAT -> cfg.chatScale = value;
                case GLOBAL_LINE -> cfg.globalLineSpacing = value;
                case WRAPPED_LINE -> cfg.wrappedLineSpacing = value;
                case TOOLTIP_LINE -> cfg.tooltipLineSpacing = value;
            }
        }

        boolean affectsChat() {
            return this == GLOBAL || this == CHAT || this == GLOBAL_LINE;
        }
    }

    private static final NumberSetting[] SIZES = {
            NumberSetting.GLOBAL, NumberSetting.SCREEN, NumberSetting.HUD,
            NumberSetting.BUTTON, NumberSetting.CHAT
    };
    private static final NumberSetting[] SPACINGS = {
            NumberSetting.GLOBAL_LINE, NumberSetting.WRAPPED_LINE, NumberSetting.TOOLTIP_LINE
    };

    private final Screen parent;
    private final FontTweaksSettings openingState;
    private Tab tab = Tab.SIZE;
    private int selectedRow;
    private int firstVisibleRow;
    private boolean compactDetail;
    private String pendingBlacklist = "";
    private EditBox numberInput;
    private ValueSlider numberSlider;

    private boolean compact;
    private int listX, listWidth, detailsX, detailsWidth;
    private int bottomY, panelBottom, visibleRows;
    private boolean hasScroll;

    public FontTweaksDashboardScreen(Screen parent) {
        super(Component.translatable("fonttweaks.ui.title"));
        this.parent = parent;
        this.openingState = FontTweaksSettings.snapshot();
    }

    @Override
    public double previewScale() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        NumberSetting numeric = selectedNumber();
        if (numeric == NumberSetting.HUD) return cfg.scaleFor(false);
        if (numeric == NumberSetting.BUTTON) return cfg.buttonTextScale();
        if (numeric == NumberSetting.CHAT) return cfg.chatTextScale();
        return cfg.scaleFor(true);
    }

    private int rowCount() {
        return switch (tab) {
            case SIZE -> SIZES.length;
            case SPACING -> SPACINGS.length;
            case PIXELS -> 2;
            case FONTS -> 3;
            case BLACKLIST -> FontTweaksSettings.get().blacklistedEntries.size();
        };
    }

    private NumberSetting selectedNumber() {
        if (tab == Tab.SIZE) return SIZES[Math.max(0, Math.min(SIZES.length - 1, selectedRow))];
        if (tab == Tab.SPACING) return SPACINGS[Math.max(0, Math.min(SPACINGS.length - 1, selectedRow))];
        return null;
    }

    @Override
    protected void init() {
        // calculate a responsive layout with guaranteed widget bounds
        compact = this.width < 465;
        if (!compact) compactDetail = false;
        bottomY = this.height - 28;
        panelBottom = bottomY - 8;
        listX = EDGE;
        int usableWidth = Math.max(120, this.width - EDGE * 2);
        listWidth = compact ? usableWidth : Math.max(235, (int) (usableWidth * 0.60));
        detailsX = compact ? EDGE : listX + listWidth + 6;
        detailsWidth = compact ? usableWidth : Math.max(110, this.width - EDGE - detailsX);

        selectedRow = Math.max(0, Math.min(selectedRow, Math.max(0, rowCount() - 1)));
        int freeHeight = Math.max(26, panelBottom - LIST_TOP);
        visibleRows = Math.max(1, freeHeight / ROW_HEIGHT);
        hasScroll = rowCount() > visibleRows;
        if (hasScroll) visibleRows = Math.max(1, (freeHeight - 23) / ROW_HEIGHT);
        firstVisibleRow = Math.max(0, Math.min(firstVisibleRow,
                Math.max(0, rowCount() - visibleRows)));

        addTabs();
        if (!compact || !compactDetail) {
            addListButtons();
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.ui.back"), button -> {
                compactDetail = false;
                this.rebuildWidgets();
            }).bounds(EDGE + 5, LIST_TOP + 4, Math.min(84, usableWidth / 2), 20).build());
        }
        if (!compact || compactDetail) addDetailControls();

        int gap = 5;
        int buttonWidth = (usableWidth - gap * 2) / 3;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset_all"), b -> resetEverything())
                .bounds(EDGE, bottomY, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.ui.undo"), b -> undoChanges())
                .bounds(EDGE + buttonWidth + gap, bottomY, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(EDGE + 2 * (buttonWidth + gap), bottomY,
                        usableWidth - 2 * (buttonWidth + gap), 20).build());
    }

    private void addTabs() {
        int n = Tab.values().length, gap = 3;
        int available = Math.max(110, this.width - EDGE * 2);
        int tabWidth = (available - (n - 1) * gap) / n;
        for (int i = 0; i < n; i++) {
            Tab value = Tab.values()[i];
            int x = EDGE + i * (tabWidth + gap);
            this.addRenderableWidget(Button.builder(Component.translatable(value.key), b -> {
                if (tab != value) {
                    tab = value;
                    selectedRow = 0;
                    firstVisibleRow = 0;
                    compactDetail = false;
                    this.rebuildWidgets();
                }
            }).bounds(x, TAB_Y, tabWidth, TAB_HEIGHT).build());
        }
    }

    private void addListButtons() {
        int count = rowCount(), listBottom = panelBottom;
        int resetWidth = 28;
        for (int i = firstVisibleRow; i < Math.min(count, firstVisibleRow + visibleRows); i++) {
            final int index = i;
            int y = LIST_TOP + (i - firstVisibleRow) * ROW_HEIGHT;
            int rowWidth = listWidth - 32;
            this.addRenderableWidget(Button.builder(Component.empty(), button -> onSelectRow(index))
                    .bounds(listX + 3, y, Math.max(30, rowWidth), 22).build());
            this.addRenderableWidget(Button.builder(Component.literal(
                    tab == Tab.BLACKLIST ? "×" : "↶"), button -> resetRow(index))
                    .bounds(listX + listWidth - resetWidth, y, resetWidth - 2, 22).build());
        }
        if (compact && tab == Tab.BLACKLIST && count == 0) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("fonttweaks.blacklist_add"), button -> {
                        compactDetail = true;
                        this.rebuildWidgets();
                    }).bounds(listX + 5, LIST_TOP + 4, Math.max(50, listWidth - 10), 20).build());
        }
        if (hasScroll) {
            int y = listBottom - 21;
            Button up = this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> scrollRows(-1))
                    .bounds(listX + 3, y, 45, 18).build());
            up.active = firstVisibleRow > 0;
            Button down = this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> scrollRows(1))
                    .bounds(listX + listWidth - 48, y, 45, 18).build());
            down.active = firstVisibleRow + visibleRows < count;
        }
    }

    private void onSelectRow(int row) {
        selectedRow = row;
        if (tab == Tab.PIXELS) {
            editSetting(() -> {
                FontTweaksSettings cfg = FontTweaksSettings.get();
                if (row == 0) cfg.pixelationEnabled = !cfg.pixelationEnabled;
                else cfg.pixelBlockSize = cfg.nextPixelBlockSize();
            });
        } else if (tab == Tab.FONTS) {
            editSetting(() -> {
                FontTweaksSettings cfg = FontTweaksSettings.get();
                switch (row) {
                    case 0 -> cfg.resourcePackEnabled = !cfg.resourcePackEnabled;
                    case 1 -> cfg.cycleResourcePackFont();
                    case 2 -> cfg.replaceEnglish = !cfg.replaceEnglish;
                    default -> { }
                }
            });
        } else {
            if (compact) compactDetail = true;
            this.rebuildWidgets();
        }
    }

    private void resetRow(int row) {
        if (tab == Tab.BLACKLIST) {
            FontTweaksSettings cfg = FontTweaksSettings.get();
            if (row < 0 || row >= cfg.blacklistedEntries.size()) return;
            cfg.blacklistedEntries.remove(row);
            selectedRow = Math.min(selectedRow, Math.max(0, cfg.blacklistedEntries.size() - 1));
            FontTweaksSettings.save();
            refreshChat();
            this.rebuildWidgets();
            return;
        }
        if (tab == Tab.SIZE || tab == Tab.SPACING) {
            NumberSetting numeric = tab == Tab.SIZE ? SIZES[row] : SPACINGS[row];
            setNumber(numeric, 1.0);
            selectedRow = row;
            this.rebuildWidgets();
            return;
        }
        editSetting(() -> {
            FontTweaksSettings cfg = FontTweaksSettings.get();
            if (tab == Tab.PIXELS) {
                if (row == 0) cfg.pixelationEnabled = true;
                else cfg.pixelBlockSize = 1;
            } else if (tab == Tab.FONTS) {
                switch (row) {
                    case 0 -> cfg.resourcePackEnabled = false;
                    case 1 -> cfg.resourcePackFont = "pixelmplus12";
                    case 2 -> cfg.replaceEnglish = false;
                    default -> { }
                }
            }
        });
    }

    private void scrollRows(int direction) {
        int target = Math.max(0, Math.min(Math.max(0, rowCount() - visibleRows),
                firstVisibleRow + direction));
        if (firstVisibleRow != target) {
            firstVisibleRow = target;
            this.rebuildWidgets();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0 && (!compact || !compactDetail) && mouseX >= listX
                && mouseX < listX + listWidth && mouseY >= LIST_TOP && mouseY < panelBottom) {
            int previous = firstVisibleRow;
            scrollRows(scrollY > 0 ? -1 : 1);
            if (previous != firstVisibleRow) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void addDetailControls() {
        int x = detailsX + 9;
        int width = Math.max(50, detailsWidth - 18);
        int availableHeight = Math.max(32, panelBottom - LIST_TOP);
        boolean smallDetail = compact && availableHeight < 115;
        int controlY = smallDetail ? panelBottom - 24
                : LIST_TOP + (availableHeight >= 178 ? 83 : availableHeight >= 115 ? 65 : 45);
        NumberSetting numeric = selectedNumber();
        numberInput = null;
        numberSlider = null;
        if (numeric != null) {
            int controlWidth = Math.max(45, width);
            int sliderWidth = smallDetail ? Math.max(32, (controlWidth - 4) / 2) : controlWidth;
            int fieldX = smallDetail ? x + sliderWidth + 4 : x;
            int fieldY = smallDetail ? controlY : controlY + 25;
            int fieldWidth = smallDetail ? Math.max(32, controlWidth - sliderWidth - 4) : controlWidth;
            EditBox field = new EditBox(this.font, fieldX, fieldY, fieldWidth, 20,
                    Component.translatable(numeric.title));
            field.setMaxLength(32);
            field.setTextColor(0xFFFFFFFF);
            field.setTextColorUneditable(0xFFFFFFFF);
            field.setValue(decimal(numeric.get(FontTweaksSettings.get())));
            this.numberInput = this.addRenderableWidget(field);

            ValueSlider slider = new ValueSlider(x, controlY, sliderWidth, numeric);
            this.numberSlider = this.addRenderableWidget(slider);
            field.setResponder(value -> {
                try {
                    double number = Double.parseDouble(value.trim());
                    if (!Double.isFinite(number) || number < 0.5 || number > 2) {
                        field.setTextColor(0xFFFF5555);
                        return;
                    }
                    field.setTextColor(0xFFFFFFFF);
                    setNumber(numeric, number);
                    slider.updateFromField(number);
                } catch (NumberFormatException ex) {
                    field.setTextColor(0xFFFF5555);
                }
            });
            return;
        }

        if (tab == Tab.BLACKLIST) {
            int addWidth = Math.max(38, Math.min(55, width / 4));
            EditBox field = new EditBox(this.font, x, controlY, Math.max(30, width - addWidth - 4),
                    20, Component.translatable("fonttweaks.blacklist_add"));
            field.setMaxLength(4096);
            field.setValue(pendingBlacklist);
            field.setTextColor(0xFFFFFFFF);
            field.setResponder(v -> pendingBlacklist = v);
            this.addRenderableWidget(field);
            this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.blacklist_add"),
                    b -> {
                        if (FontTweaksSettings.get().addBlacklistCharacters(pendingBlacklist)) {
                            FontTweaksSettings.save();
                            refreshChat();
                        }
                        pendingBlacklist = "";
                        selectedRow = Math.max(0, FontTweaksSettings.get().blacklistedEntries.size() - 1);
                        firstVisibleRow = Math.max(0, selectedRow - visibleRows + 1);
                        this.rebuildWidgets();
                    }).bounds(x + width - addWidth, controlY, addWidth, 20).build());
            if (rowCount() > 0 && controlY + 48 < panelBottom) {
                this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.blacklist_delete"),
                        b -> resetRow(selectedRow)).bounds(x, controlY + 25, width, 20).build());
            }
            return;
        }

        // Controls exist in both panes: changing a row and changing its
        // detail action have identical behavior and persistence semantics.
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.ui.change"), b -> {
            onSelectRow(selectedRow);
        }).bounds(x, controlY, width, 20).build());
    }

    private void setNumber(NumberSetting setting, double value) {
        setting.set(FontTweaksSettings.get(), FontTweaksSettings.bounded(value, 0.5, 2.0));
        FontTweaksSettings.save();
        if (setting.affectsChat()) refreshChat();
    }

    private void editSetting(Runnable change) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        boolean wasEnabled = cfg.pixelationEnabled;
        int oldDots = cfg.pixelBlockSize;
        change.run();
        FontTweaksSettings.save();
        if (wasEnabled != cfg.pixelationEnabled || oldDots != cfg.pixelBlockSize) {
            FontTweaksReload.request();
        }
        refreshChat();
        this.rebuildWidgets();
    }

    private void restoreState(FontTweaksSettings replacement) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        boolean wasEnabled = cfg.pixelationEnabled;
        int oldDots = cfg.pixelBlockSize;
        FontTweaksSettings.restore(replacement);
        if (wasEnabled != replacement.pixelationEnabled || oldDots != replacement.pixelBlockSize) {
            FontTweaksReload.request();
        }
        refreshChat();
        pendingBlacklist = "";
        selectedRow = 0;
        firstVisibleRow = 0;
        compactDetail = false;
        this.rebuildWidgets();
    }

    private void resetEverything() { restoreState(new FontTweaksSettings()); }
    private void undoChanges() { restoreState(openingState); }

    private void refreshChat() {
        if (this.minecraft != null && this.minecraft.gui != null) {
            this.minecraft.gui.hud.getChat().rescaleChat();
        }
    }

    private static String decimal(double number) {
        return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
    }

    private String rowTitle(int index) {
        return switch (tab) {
            case SIZE -> Component.translatable(SIZES[index].title).getString();
            case SPACING -> Component.translatable(SPACINGS[index].title).getString();
            case PIXELS -> Component.translatable(index == 0 ? "fonttweaks.ui.pixel_toggle"
                    : "fonttweaks.ui.dot_count").getString();
            case FONTS -> Component.translatable(switch (index) {
                case 0 -> "fonttweaks.ui.font_enable";
                case 1 -> "fonttweaks.ui.font_choice";
                default -> "fonttweaks.ui.font_english";
            }).getString();
            case BLACKLIST -> FontTweaksSettings.get().blacklistedEntries.get(index);
        };
    }

    private String rowValue(int index) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        return switch (tab) {
            case SIZE -> String.format(Locale.ROOT, "%.1f%%", SIZES[index].get(cfg) * 100.0);
            case SPACING -> String.format(Locale.ROOT, "%.1f%%", SPACINGS[index].get(cfg) * 100.0);
            case PIXELS -> index == 0 ? toggleState(cfg.pixelationEnabled) : cfg.pixelBlockSize + "px";
            case FONTS -> switch (index) {
                case 0 -> toggleState(cfg.resourcePackEnabled);
                case 1 -> cfg.resourcePackFont.equals("misaki") ? "Misaki"
                        : cfg.resourcePackFont.equals("auto") ? "Auto" : "PixelMplus";
                default -> toggleState(cfg.replaceEnglish);
            };
            case BLACKLIST -> String.format(Locale.ROOT, "U+%04X",
                    cfg.blacklistedEntries.get(index).codePointAt(0));
        };
    }

    private String toggleState(boolean on) {
        return Component.translatable(on ? "fonttweaks.ui.on" : "fonttweaks.ui.off").getString();
    }

    private String selectedHelpKey() {
        return switch (tab) {
            case SIZE, SPACING -> selectedNumber().help;
            case PIXELS -> selectedRow == 0 ? "fonttweaks.ui.desc.pixel_toggle"
                    : "fonttweaks.ui.desc.pixel_count";
            case FONTS -> switch (selectedRow) {
                case 0 -> "fonttweaks.ui.desc.font_enable";
                case 1 -> "fonttweaks.ui.desc.font_choice";
                default -> "fonttweaks.ui.desc.font_english";
            };
            case BLACKLIST -> "fonttweaks.ui.desc.blacklist";
        };
    }

    private String crop(String value, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (this.font.width(value) <= maxWidth) return value;
        String suffix = "…";
        while (!value.isEmpty() && this.font.width(value + suffix) > maxWidth) {
            value = value.substring(0, value.offsetByCodePoints(value.length(), -1));
        }
        return value + suffix;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Background must be emitted before buttons, otherwise it overlays
        // them in the 26.3 deferred GUI renderer.
        graphics.fill(EDGE - 2, LIST_TOP - 3, this.width - EDGE + 2,
                Math.max(LIST_TOP, panelBottom + 2), 0x9B101015);
        if (!compact) {
            graphics.fill(detailsX - 3, LIST_TOP - 3, detailsX - 1, panelBottom + 2, 0x99CCCCCC);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        // Selected category uses a bright outline instead of disabled text.
        int tabWidth = (Math.max(110, this.width - EDGE * 2) - (Tab.values().length - 1) * 3)
                / Tab.values().length;
        graphics.outline(EDGE + tab.ordinal() * (tabWidth + 3), TAB_Y,
                tabWidth, TAB_HEIGHT, 0xFF7EC8FF);

        graphics.text(this.font, Component.translatable("fonttweaks.ui.title"),
                EDGE + 1, 5, 0xFFFFFFFF);

        if (!compact || !compactDetail) {
            int count = rowCount();
            for (int index = firstVisibleRow; index < Math.min(count, firstVisibleRow + visibleRows); index++) {
                int y = LIST_TOP + (index - firstVisibleRow) * ROW_HEIGHT;
                if (index == selectedRow) {
                    graphics.fill(listX + 4, y + 2, listX + listWidth - 30, y + 21, 0x3459A1E8);
                }
                int valueMax = listWidth - 52;
                String value = crop(rowValue(index), Math.max(28, valueMax / 2));
                int valueWidth = this.font.width(value);
                int valueX = listX + listWidth - 38 - valueWidth;
                String label = crop(rowTitle(index), valueX - (listX + 9) - 4);
                graphics.text(this.font, label, listX + 9, y + 7, 0xFFFFFFFF);
                graphics.text(this.font, value, valueX, y + 7, 0xFFE7E7E7);
            }
            if (tab == Tab.BLACKLIST && count == 0) {
                graphics.text(this.font, Component.translatable("fonttweaks.ui.empty"),
                        listX + 12, LIST_TOP + 11, 0xFFBFBFBF);
            }
            if (hasScroll) {
                graphics.centeredText(this.font,
                        Component.literal((firstVisibleRow + 1) + "-" +
                                Math.min(count, firstVisibleRow + visibleRows) + " / " + count),
                        listX + listWidth / 2, panelBottom - 17, 0xFFB4B4B4);
            }
        }

        if (!compact || compactDetail) {
            renderDetails(graphics);
        } else if (!hasScroll
                && LIST_TOP + Math.min(rowCount(), visibleRows) * ROW_HEIGHT < panelBottom - 18) {
            graphics.text(this.font, Component.translatable("fonttweaks.ui.compact_hint"),
                    EDGE + 5, panelBottom - 11, 0xFFAAAAAA);
        }
    }

    private void renderDetails(GuiGraphicsExtractor graphics) {
        int x = detailsX + 9, width = Math.max(50, detailsWidth - 18);
        int height = panelBottom - LIST_TOP;
        int titleY = LIST_TOP + (compact ? 33 : 12);
        String title = rowCount() == 0
                ? Component.translatable("fonttweaks.ui.empty").getString()
                : rowTitle(selectedRow);
        graphics.text(this.font, crop(title, width), x, titleY, 0xFFFFFFFF);

        if (height >= 125 && !compact) {
            graphics.textWithWordWrap(this.font,
                    Component.translatable(selectedHelpKey()), x, LIST_TOP + 30, width, 0xFFBDBDBD);
        } else if (!(compact && height < 115)) {
            graphics.text(this.font,
                    crop(Component.translatable(selectedHelpKey()).getString(), width),
                    x, titleY + 14, 0xFFBABABA);
        }

        int previewY = panelBottom - 48;
        int controlY = LIST_TOP + (height >= 178 ? 83 : height >= 115 ? 65 : 45);
        if (previewY > controlY + (selectedNumber() == null ? 28 : 50) && height > 160) {
            graphics.text(this.font, Component.translatable("fonttweaks.ui.live_preview"),
                    x, previewY, 0xFFFFDD84);
            FontTweaksRenderContext.Kind prev =
                    FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
            try {
                graphics.text(this.font, Component.literal("日本語 ABC 012"), x, previewY + 13,
                        0xFFFFFFFF);
            } finally {
                FontTweaksRenderContext.pop(prev);
            }
            String status = "GUI " + this.minecraft.getWindow().getGuiScale()
                    + "  |  " + FontTweaksSettings.get().pixelBlockSize + "px  "
                    + toggleState(FontTweaksSettings.get().pixelationEnabled);
            graphics.text(this.font, crop(status, width), x, previewY + 28, 0xFF9ADDEB);
        }
    }

    @Override
    public void onClose() {
        FontTweaksSettings.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    private final class ValueSlider extends AbstractSliderButton {
        private final NumberSetting target;

        ValueSlider(int x, int y, int width, NumberSetting target) {
            super(x, y, width, 20, Component.empty(),
                    (target.get(FontTweaksSettings.get()) - 0.5) / 1.5);
            this.target = target;
            updateMessage();
        }

        void updateFromField(double number) {
            this.value = (number - 0.5) / 1.5;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (target != null) {
                this.setMessage(Component.literal(String.format(Locale.ROOT, "%.2f%%",
                        target.get(FontTweaksSettings.get()) * 100.0)));
            }
        }

        @Override
        protected void applyValue() {
            double number = Math.round((0.5 + this.value * 1.5) * 1000.0) / 1000.0;
            setNumber(target, number);
            if (numberInput != null) numberInput.setValue(decimal(number));
            updateMessage();
        }
    }
}
