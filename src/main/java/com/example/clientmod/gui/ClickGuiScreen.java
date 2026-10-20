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
import java.util.List;

/**
 * Navigator-style menu, built for touch (tap only):
 *   left column  = categories
 *   middle       = modules in that category (tap name = open settings, tap switch = on/off)
 *   right        = settings of the selected module
 */
public class ClickGuiScreen extends Screen {
    private static final int BG = 0xF0121212, SIDE = 0xFF1A1A1A, PANEL = 0xFF202020, ROW = 0xFF2A2A2A,
            SEL = 0xFF343434, ACCENT = 0xFF3DDC84, OFF = 0xFF555555, TEXT = 0xFFFFFFFF, DIM = 0xFFAAAAAA;
    private static final int RH = 18;

    private record Hit(int x, int y, int w, int h, Runnable action) {}

    private final List<Hit> hits = new ArrayList<>();
    private Category category = Category.COMBAT;
    private Module selected;

    public ClickGuiScreen() { super(Text.literal("ClientMod")); }

    @Override
    protected void init() {
        for (Module m : ClientMod.MODULES.modules) m.onGuiOpen();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        hits.clear();
        int pw = Math.min(width - 16, 380), ph = Math.min(height - 16, 230);
        int px = (width - pw) / 2, py = (height - ph) / 2;
        int sideW = Math.min(78, pw / 4);
        int listW = Math.min(124, (pw - sideW) / 2);
        int setX = px + sideW + listW, setW = pw - sideW - listW;

        ctx.fill(px, py, px + pw, py + ph, BG);
        ctx.fill(px, py, px + sideW, py + ph, SIDE);
        ctx.drawTextWithShadow(textRenderer, "ClientMod", px + 8, py + 8, ACCENT);

        // Categories
        int y = py + 28;
        for (Category c : Category.values()) {
            boolean on = c == category;
            ctx.fill(px + 4, y, px + sideW - 4, y + RH, on ? SEL : SIDE);
            if (on) ctx.fill(px + 4, y, px + 6, y + RH, ACCENT);
            ctx.drawTextWithShadow(textRenderer, c.label, px + 12, y + 5, on ? TEXT : DIM);
            final Category cc = c;
            hits.add(new Hit(px + 4, y, sideW - 8, RH, () -> { category = cc; selected = null; }));
            y += RH + 2;
        }

        // Modules
        List<Module> mods = ClientMod.MODULES.in(category);
        ctx.fill(px + sideW, py, px + sideW + listW, py + ph, PANEL);
        ctx.drawTextWithShadow(textRenderer, category.label, px + sideW + 8, py + 8, TEXT);
        y = py + 28;
        if (mods.isEmpty()) ctx.drawTextWithShadow(textRenderer, "Coming soon", px + sideW + 8, y + 4, DIM);
        for (Module m : mods) {
            int rx = px + sideW + 4, rw = listW - 8;
            ctx.fill(rx, y, rx + rw, y + RH, m == selected ? SEL : ROW);
            ctx.drawTextWithShadow(textRenderer, m.name, rx + 6, y + 5, TEXT);
            // on/off switch
            int sx = rx + rw - 24, sy = y + 4;
            ctx.fill(sx, sy, sx + 20, sy + 10, m.isEnabled() ? ACCENT : OFF);
            int knob = m.isEnabled() ? sx + 11 : sx + 1;
            ctx.fill(knob, sy + 1, knob + 8, sy + 9, TEXT);
            hits.add(new Hit(sx - 2, y, 26, RH, m::toggle));
            hits.add(new Hit(rx, y, rw - 26, RH, () -> selected = m));
            y += RH + 2;
        }

        // Settings
        ctx.fill(setX, py, px + pw, py + ph, SIDE);
        if (selected == null) {
            ctx.drawTextWithShadow(textRenderer, "Tap a module", setX + 8, py + 8, DIM);
        } else {
            ctx.drawTextWithShadow(textRenderer, selected.name + " settings", setX + 8, py + 8, TEXT);
            y = py + 28;
            if (selected.settings.isEmpty()) ctx.drawTextWithShadow(textRenderer, "No settings", setX + 8, y + 4, DIM);
            for (Setting<?> s : selected.settings) {
                int rx = setX + 4, rw = setW - 8;
                ctx.fill(rx, y, rx + rw, y + RH, ROW);
                ctx.drawTextWithShadow(textRenderer, s.name, rx + 6, y + 5, DIM);
                drawControl(ctx, s, rx, y, rw);
                y += RH + 2;
            }
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    /** Right-aligned control: check box / [-] value [+] / < mode >. */
    private void drawControl(DrawContext ctx, Setting<?> s, int rx, int y, int rw) {
        int right = rx + rw - 4;
        if (s.get() instanceof Boolean b) {
            ctx.fill(right - 20, y + 4, right, y + 14, b ? ACCENT : OFF);
            int knob = b ? right - 9 : right - 19;
            ctx.fill(knob, y + 5, knob + 8, y + 13, TEXT);
            hits.add(new Hit(right - 24, y, 28, RH, () -> s.click(false)));
        } else {
            String v = s.display();
            int vw = textRenderer.getWidth(v);
            int plusX = right - 12, valX = plusX - 6 - vw, minusX = valX - 6 - 12;
            if (s.get() instanceof Double) {
                button(ctx, minusX, y + 3, "-");
                button(ctx, plusX, y + 3, "+");
                ctx.drawTextWithShadow(textRenderer, v, valX, y + 5, TEXT);
                hits.add(new Hit(minusX - 2, y, 16, RH, () -> s.click(true)));
                hits.add(new Hit(plusX - 2, y, 16, RH, () -> s.click(false)));
            } else { // mode: tap value to cycle
                ctx.drawTextWithShadow(textRenderer, "< " + v + " >", right - vw - 16, y + 5, ACCENT);
                hits.add(new Hit(right - vw - 20, y, vw + 24, RH, () -> s.click(false)));
            }
        }
    }

    private void button(DrawContext ctx, int x, int y, String label) {
        ctx.fill(x, y, x + 12, y + 12, SEL);
        ctx.drawTextWithShadow(textRenderer, label, x + 4, y + 2, TEXT);
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
