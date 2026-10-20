package com.example.clientmod.module;

import com.example.clientmod.render.Projector;
import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

/** Boxes around players / mobs through walls. */
public class EntityEspModule extends Module {
    private final Setting<Boolean> players = add(Setting.bool("Players", true));
    private final Setting<Boolean> hostile = add(Setting.bool("Hostile mobs", true));
    private final Setting<Boolean> passive = add(Setting.bool("Passive mobs", false));
    private final Setting<Boolean> labels = add(Setting.bool("Names", true));
    private final Setting<Double> range = add(Setting.number("Range", 48.0, 16.0, 96.0, 8.0));

    public EntityEspModule() { super("Entity ESP", "Boxes around players and mobs.", Category.RENDER); }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        Projector pr = new Projector(mc);
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity) || e == mc.player || !e.isAlive() || e instanceof ArmorStandEntity) continue;
            if (mc.player.distanceTo(e) > range.get()) continue;
            int rgb;
            if (e instanceof PlayerEntity) { if (!players.get()) continue; rgb = 0x40C4FF; }
            else if (e instanceof Monster) { if (!hostile.get()) continue; rgb = 0xEF5350; }
            else { if (!passive.get()) continue; rgb = 0x66BB6A; }
            int[] b = pr.project(e.getBoundingBox());
            if (b == null) continue;
            Projector.drawBox(ctx, mc, b, rgb, labels.get() ? e.getName().getString() : null);
        }
    }
}
