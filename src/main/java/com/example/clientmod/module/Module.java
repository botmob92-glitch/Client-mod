package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected <S extends Setting<?>> S add(S setting) { settings.add(setting); return setting; }

    public boolean isEnabled() { return enabled; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    /** Called every client tick while enabled and in a world. */
    public void onTick(MinecraftClient mc) {}
}
