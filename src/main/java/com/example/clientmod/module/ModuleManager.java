package com.example.clientmod.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new AimAssistModule());
        modules.add(new AutoAttackModule());
        modules.add(new SpeedModule());
        modules.add(new StorageEspModule());
        modules.add(new OreEspModule());
        modules.add(new EntityEspModule());
        modules.add(new SchematicBuilderModule());
        // Next: OreEsp, StorageEsp (Render), AutoLibrarian (Misc)
    }

    public List<Module> in(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.category == c) out.add(m);
        return out;
    }

    public void hud(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        for (Module m : modules) if (m.isEnabled()) m.onHud(ctx, mc);
    }

    public void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        for (Module m : modules) if (m.isEnabled()) m.onTick(mc);
    }
}
