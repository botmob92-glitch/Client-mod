package com.example.clientmod.gui;

import com.example.clientmod.ClientMod;
import com.example.clientmod.module.Category;
import com.example.clientmod.module.Module;
import com.example.clientmod.setting.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Customi Client menu. Touch friendly (tap only), with a fade/slide-in, animated switches,
 * hover highlights, slider bars and falling petals.
 *   left = categories, middle = modules, right = settings of the selected module.
 */
public class ClickGuiScreen extends Screen {
    private static final int ACCENT = 0xFFFF7EB6, ACCENT2 = 0xFFB388FF, OFF = 0xFF555566,
            TEXT = 0xFFFFFFFF, DIM = 0xFFA0A0B8;
    private static final int RH = 18;

    private record Hit(int x, int y, int w, int h, Runnable action) {}

    private final List<Hit> hits = new ArrayList<>();
    private final Map<Module, Float> anim = new HashMap<>();
    private Category category = Category.COMBAT;
    private Module selected;
    private float alpha = 1f;
    private long openNanos, lastNanos;
    private int mouseXs, mouseYs;

    // Falling petals / little flowers.
    private static final class Petal {
        float x, y, speed, drift, phase;
        int size, color;
        boolean flower;
    }
    private static final int[] PETAL_COLORS = {0xB0FFB7D5, 0xB0FFFFFF, 0xB0FFD1E3, 0xB0D1B8FF};
    private final List<Petal> petals = new ArrayList<>();
    private final Random rng = new Random();

    public ClickGuiScreen() { super(Text.literal(ClientMod.NAME)); }

    @Override
    protected void init() {
        for (Module m : ClientMod.MODULES.modules) m.onGuiOpen();
        petals.clear();
        for (int i = 0; i < 40; i++) {
            Petal p = new Petal();
            respawn(p);
            p.y = rng.nextFloat() * height;
            petals.add(p);
        }
        openNanos = lastNanos = System.nanoTime();
    }

    // ---- small drawing helpers (all colours get the open-animation alpha) ----

    private int al(int argb) {
        int a = (int) (((argb >>> 24) & 0xFF) * alpha);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private static int lerpColor(int c1, int c2, float f) {
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (c1 >>> shift) & 0xFF, b = (c2 >>> shift) & 0xFF;
            out |= ((int) (a + (b - a) * f) & 0xFF) << shift;
        }
        return out;
    }

    /** Rounded-corner rectangle (corners cut by 1px), no overlapping fills. */
    private void rect(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        col = al(col);
        c.fill(x1 + 1, y1, x2 - 1, y1 + 1, col);
        c.fill(x1, y1 + 1, x2, y2 - 1, col);
        c.fill(x1 + 1, y2 - 1, x2 - 1, y2, col);
    }

    private void text(DrawContext c, String s, int x, int y, int col) {
        c.drawTextWithShadow(textRenderer, s, x, y, al(col));
    }

    private boolean over(int x, int y, int w, int h) {
        return mouseXs >= x && mouseXs < x + w && mouseYs >= y && mouseYs < y + h;
    }

    private void respawn(Petal p) {
        p.x = rng.nextFloat() * width;
        p.y = -4;
        p.speed = 14 + rng.nextFloat() * 20;
        p.drift = 6 + rng.nextFloat() * 10;
        p.phase = rng.nextFloat() * 6.28f;
        p.size = 1 + rng.nextInt(2);
        p.flower = rng.nextInt(4) == 0;
        p.color = PETAL_COLORS[rng.nextInt(PETAL_COLORS.length)];
    }

    private void drawPetals(DrawContext ctx, float dt, float t) {
        for (Petal p : petals) {
            p.y += p.speed * dt;
            p.x += (float) Math.sin(t * 1.3f + p.phase) * p.drift * dt;
            if (p.y > height + 4 || p.x < -6 || p.x > width + 6) respawn(p);
            int x = (int) p.x, y = (int) p.y, s = p.size, col = al(p.color);
            if (p.flower) {
                ctx.fill(x + s, y, x + 2 * s, y + s, col);
                ctx.fill(x, y + s, x + s, y + 2 * s, col);
                ctx.fill(x + 2 * s, y + s, x + 3 * s, y + 2 * s, col);
                ctx.fill(x + s, y + 2 * s, x + 2 * s, y + 3 * s, col);
                ctx.fill(x + s, y + s, x + 2 * s, y + 2 * s, al(0xC0FFE680));
            } else {
                ctx.fill(x, y, x + s + 1, y + s, col);
            }
        }
    }

    // ---- main render ----

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        hits.clear();
        mouseXs = mouseX;
        mouseYs = mouseY;
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        float t = Math.min(1f, (now - openNanos) / 2.6e8f);
        float open = 1 - (1 - t) * (1 - t) * (1 - t); // ease-out
        alpha = Math.max(0.06f, open);

        int pw = Math.min(width - 16, 400), ph = Math.min(height - 16, 236);
        int px = (width - pw) / 2, py = (height - ph) / 2 + (int) ((1 - open) * 14);
        int sideW = Math.min(80, pw / 4);
        int listW = Math.min(126, (pw - sideW) / 2);
        int setX = px + sideW + listW, setW = pw - sideW - listW;

        // Panel: soft shadow, purple-tinted gradient, darker sidebar, glowing top line.
        rect(ctx, px - 2, py - 2, px + pw + 2, py + ph + 2, 0x40000000);
        ctx.fillGradient(px, py, px + pw, py + ph, al(0xF0241A38), al(0xF00D0D16));
        ctx.fill(px, py, px + sideW, py + ph, al(0x55000000));
        ctx.fillGradient(px, py, px + pw, py + 2, al(ACCENT), al(ACCENT2 & 0x00FFFFFF));

        // Title
        text(ctx, "Customi", px + 8, py + 8, ACCENT);
        text(ctx, "Client", px + 8, py + 18, TEXT);

        // Categories
        int y = py + 36;
        for (Category c : Category.values()) {
            boolean on = c == category;
            boolean hov = over(px + 4, y, sideW - 8, RH);
            if (on) {
                rect(ctx, px + 4, y, px + sideW - 4, y + RH, 0x40FF7EB6);
                ctx.fill(px + 4, y + 3, px + 6, y + RH - 3, al(ACCENT));
            } else if (hov) {
                rect(ctx, px + 4, y, px + sideW - 4, y + RH, 0x22FFFFFF);
            }
            text(ctx, c.label, px + 12, y + 5, on ? TEXT : DIM);
            final Category cc = c;
            hits.add(new Hit(px + 4, y, sideW - 8, RH, () -> { category = cc; selected = null; }));
            y += RH + 2;
        }

        // Modules
        List<Module> mods = ClientMod.MODULES.in(category);
        text(ctx, category.label, px + sideW + 8, py + 8, TEXT);
        y = py + 28;
        if (mods.isEmpty()) text(ctx, "Coming soon", px + sideW + 8, y + 4, DIM);
        for (Module m : mods) {
            int rx = px + sideW + 4, rw = listW - 8;
            float a = anim.getOrDefault(m, m.isEnabled() ? 1f : 0f);
            a += ((m.isEnabled() ? 1f : 0f) - a) * Math.min(1f, dt * 14f);
            anim.put(m, a);

            int base = m == selected ? 0x44FFFFFF : (over(rx, y, rw, RH) ? 0x30FFFFFF : 0x1EFFFFFF);
            rect(ctx, rx, y, rx + rw, y + RH, base);
            text(ctx, m.name, rx + 6, y + 5, lerpColor(DIM, TEXT, a));
            drawSwitch(ctx, rx + rw - 26, y + 4, a);
            hits.add(new Hit(rx + rw - 28, y, 28, RH, m::toggle));
            hits.add(new Hit(rx, y, rw - 28, RH, () -> selected = m));
            y += RH + 2;
        }

        // Settings
        ctx.fill(setX, py + 2, setX + 1, py + ph, al(0x22FFFFFF));
        if (selected == null) {
            text(ctx, "Tap a module", setX + 10, py + 8, DIM);
        } else {
            text(ctx, selected.name, setX + 10, py + 8, ACCENT);
            text(ctx, textRenderer.trimToWidth(selected.description, setW - 14), setX + 10, py + 18, DIM);
            y = py + 32;
            for (Setting<?> s : selected.settings) {
                int rx = setX + 6, rw = setW - 12;
                rect(ctx, rx, y, rx + rw, y + RH, over(rx, y, rw, RH) ? 0x30FFFFFF : 0x1EFFFFFF);
                text(ctx, s.name, rx + 6, y + 4, TEXT);
                drawControl(ctx, s, rx, y, rw);
                y += RH + 2;
            }
            // Keybind (read only here; change it in Options > Controls)
            String key = selected.key == null ? "-" : selected.key.getBoundKeyLocalizedText().getString();
            int rx = setX + 6, rw = setW - 12;
            rect(ctx, rx, y, rx + rw, y + RH, 0x14FFFFFF);
            text(ctx, "Keybind", rx + 6, y + 5, DIM);
            text(ctx, key, rx + rw - 6 - textRenderer.getWidth(key), y + 5, ACCENT2);
            text(ctx, "Set keys in Options > Controls", rx + 6, y + RH + 4, DIM);
        }

        drawPetals(ctx, dt, now / 1e9f);
        super.render(ctx, mouseX, mouseY, delta);
    }

    /** Animated on/off switch; a = 0 (off) .. 1 (on). */
    private void drawSwitch(DrawContext ctx, int x, int y, float a) {
        rect(ctx, x, y, x + 22, y + 10, lerpColor(OFF, ACCENT, a));
        int knob = x + 1 + (int) (11 * a);
        rect(ctx, knob, y + 1, knob + 9, y + 9, TEXT);
    }

    /** Right-aligned control: switch / [-] value [+] with a slider bar / < mode >. */
    private void drawControl(DrawContext ctx, Setting<?> s, int rx, int y, int rw) {
        int right = rx + rw - 6;
        if (s.get() instanceof Boolean b) {
            drawSwitch(ctx, right - 22, y + 4, b ? 1f : 0f);
            hits.add(new Hit(right - 26, y, 30, RH, () -> s.click(false)));
        } else if (s.get() instanceof Double d) {
            String v = s.display();
            int vw = textRenderer.getWidth(v);
            int plusX = right - 12, valX = plusX - 6 - vw, minusX = valX - 6 - 12;
            button(ctx, minusX, y + 3, "-");
            button(ctx, plusX, y + 3, "+");
            text(ctx, v, valX, y + 4, TEXT);
            hits.add(new Hit(minusX - 2, y, 16, RH, () -> s.click(true)));
            hits.add(new Hit(plusX - 2, y, 16, RH, () -> s.click(false)));
            // slider bar along the bottom of the row
            float f = (float) ((d - s.min) / (s.max - s.min));
            int bx = rx + 6, bw = rw - 12;
            ctx.fill(bx, y + RH - 3, bx + bw, y + RH - 1, al(0x30FFFFFF));
            ctx.fill(bx, y + RH - 3, bx + (int) (bw * f), y + RH - 1, al(ACCENT));
        } else {
            String v = s.display();
            int vw = textRenderer.getWidth(v);
            text(ctx, "< " + v + " >", right - vw - 16, y + 4, ACCENT);
            hits.add(new Hit(right - vw - 20, y, vw + 24, RH, () -> s.click(false)));
        }
    }

    private void button(DrawContext ctx, int x, int y, String label) {
        rect(ctx, x, y, x + 12, y + 11, over(x, y, 12, 11) ? 0x55FFFFFF : 0x33FFFFFF);
        text(ctx, label, x + 4, y + 2, TEXT);
    }

    // 1.21.9+ signature (Click record).
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        for (Hit h : hits) {
            if (mx >= h.x && mx < h.x + h.w && my >= h.y && my < h.y + h.h) {
                h.action.run();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean shouldPause() { return false; }
}
