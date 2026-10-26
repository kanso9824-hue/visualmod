package com.example.visualmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class MenuScreen extends Screen {
    private static final int W = 150, H = 20, GAP = 24;

    public MenuScreen() { super(Text.literal("Visual Mod")); }

    @Override
    protected void init() {
        Config c = Config.I;
        int lx = width / 2 - W - 6, rx = width / 2 + 6, y = 40;

        toggle(lx, y, "Trigger bot", () -> c.triggerBot, v -> c.triggerBot = v);
        toggle(lx, y + GAP, "Crits only", () -> c.triggerCrit, v -> c.triggerCrit = v);
        slider(lx, y + GAP * 2, "Min delay (ticks)", 0, 10, c.triggerMinDelay, 0, v -> c.triggerMinDelay = v);
        slider(lx, y + GAP * 3, "Max delay (ticks)", 0, 20, c.triggerMaxDelay, 0, v -> c.triggerMaxDelay = v);
        toggle(lx, y + GAP * 4, "Aim assist", () -> c.aimAssist, v -> c.aimAssist = v);
        toggle(lx, y + GAP * 5, "Only while attacking", () -> c.aimClickOnly, v -> c.aimClickOnly = v);
        slider(lx, y + GAP * 6, "Range", 1, 6, c.aimRange, 1, v -> c.aimRange = v);
        slider(lx, y + GAP * 7, "FOV", 10, 180, c.aimFov, 0, v -> c.aimFov = v);
        slider(lx, y + GAP * 8, "Speed", 0.5, 10, c.aimSpeed, 1, v -> c.aimSpeed = v);

        toggle(rx, y, "Fullbright", () -> c.fullbright, v -> c.fullbright = v);
        toggle(rx, y + GAP, "ESP players", () -> c.espPlayers, v -> c.espPlayers = v);
        toggle(rx, y + GAP * 2, "ESP mobs", () -> c.espMobs, v -> c.espMobs = v);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 75, y + GAP * 9 + 6, 150, H).build());
    }

    private void toggle(int x, int y, String label, BooleanSupplier get, Consumer<Boolean> set) {
        ButtonWidget[] ref = new ButtonWidget[1];
        ref[0] = ButtonWidget.builder(text(label, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(text(label, get.getAsBoolean()));
        }).dimensions(x, y, W, H).build();
        addDrawableChild(ref[0]);
    }

    private static Text text(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "§aON" : "§cOFF"));
    }

    private void slider(int x, int y, String label, double min, double max, double cur, int decimals, DoubleConsumer set) {
        addDrawableChild(new Slider(x, y, label, min, max, cur, decimals, set));
    }

    private static class Slider extends SliderWidget {
        private final String label; private final double min, max; private final int dec; private final DoubleConsumer set;

        Slider(int x, int y, String label, double min, double max, double cur, int dec, DoubleConsumer set) {
            super(x, y, W, H, Text.empty(), (cur - min) / (max - min));
            this.label = label; this.min = min; this.max = max; this.dec = dec; this.set = set;
            updateMessage();
        }

        private double real() {
            double v = min + value * (max - min);
            double f = Math.pow(10, dec);
            return Math.round(v * f) / f;
        }

        @Override protected void updateMessage() {
            if (label == null) return;
            setMessage(Text.literal(label + ": " + (dec == 0 ? String.valueOf((int) real()) : String.valueOf(real()))));
        }

        @Override protected void applyValue() { set.accept(real()); }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void removed() { Config.save(); }
}
