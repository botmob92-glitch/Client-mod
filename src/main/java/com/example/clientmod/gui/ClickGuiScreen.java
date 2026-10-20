package com.example.clientmod.gui;

import com.example.clientmod.ClientMod;
import com.example.clientmod.module.Module;
import com.example.clientmod.setting.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Simple ClickGUI. Left click a module = toggle, right click = expand its settings.
 * Left/right click a setting = change it (right click steps numbers/modes backwards).
 */
public class ClickGuiScreen extends Screen {
    private static final int X = 20, Y = 20, W = 150, ROW = 14;
    private Module expanded;

    public ClickGuiScreen() { super(Text.literal("ClientMod")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int y = Y;
        ctx.fill(X, y, X + W, y + ROW, 0xFF202020);
        ctx.drawTextWithShadow(textRenderer, "ClientMod", X + 4, y + 3, 0xFFFFFFFF);
        y += ROW;
        for (Module m : ClientMod.MODULES.modules) {
            ctx.fill(X, y, X + W, y + ROW, m.isEnabled() ? 0xFF2E7D32 : 0xFF3A3A3A);
            ctx.drawTextWithShadow(textRenderer, m.name, X + 4, y + 3, 0xFFFFFFFF);
            y += ROW;
            if (m == expanded) {
                for (Setting<?> s : m.settings) {
                    ctx.fill(X, y, X + W, y + ROW, 0xFF262626);
                    ctx.drawTextWithShadow(textRenderer, "  " + s.name + ": " + s.display(), X + 4, y + 3, 0xFFCCCCCC);
                    y += ROW;
                }
            }
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    // 1.21.9+ signature (Click record). If your mappings differ: mouseClicked(double x, double y, int button).
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        boolean right = click.button() == 1;
        int y = Y + ROW;
        for (Module m : ClientMod.MODULES.modules) {
            if (hit(mx, my, y)) {
                if (right) expanded = (expanded == m) ? null : m; else m.toggle();
                return true;
            }
            y += ROW;
            if (m == expanded) {
                for (Setting<?> s : m.settings) {
                    if (hit(mx, my, y)) { s.click(right); return true; }
                    y += ROW;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean hit(double mx, double my, int y) {
        return mx >= X && mx <= X + W && my >= y && my < y + ROW;
    }

    @Override
    public boolean shouldPause() { return false; }
}
