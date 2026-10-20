package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <S extends Setting<?>> S add(S setting) { settings.add(setting); return setting; }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
    }

    public void onGuiOpen() {}
    /** Called every frame while enabled, to draw on the HUD (ESP boxes etc). */
    public void onHud(DrawContext ctx, MinecraftClient mc) {}
    public void onEnable() {}
    public void onDisable() {}
    /** Called every client tick while enabled and in a world. */
    public void onTick(MinecraftClient mc) {}
}
