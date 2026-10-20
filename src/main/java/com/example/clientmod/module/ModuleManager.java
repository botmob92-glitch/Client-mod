package com.example.clientmod.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new SpeedModule());
        // Next: AimAssist, AttackAura, OreEsp, StorageEsp, AutoLibrarian
    }

    public void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        for (Module m : modules) if (m.isEnabled()) m.onTick(mc);
    }
}
