package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Turns your camera toward a target. With "Lock on" it picks a target once and keeps looking at it
 * (FOV no longer matters) until it dies or leaves range.
 */
public class AimAssistModule extends Module {
    private final Setting<Boolean> players = add(Setting.bool("Players", true));
    private final Setting<Boolean> hostile = add(Setting.bool("Hostile mobs", false));
    private final Setting<Boolean> passive = add(Setting.bool("Passive mobs", false));
    private final Setting<Boolean> lockOn = add(Setting.bool("Lock on (always look)", false));
    private final Setting<String> aimAt = add(Setting.mode("Aim at", "Body", "Body", "Head"));
    private final Setting<Double> range = add(Setting.number("Range", 4.5, 1.0, 8.0, 0.5));
    private final Setting<Double> fov = add(Setting.number("FOV", 90.0, 10.0, 360.0, 10.0));
    private final Setting<Double> smooth = add(Setting.number("Smoothness", 6.0, 1.0, 20.0, 1.0));

    private Entity locked;

    public AimAssistModule() { super("Aim Assist", "Follows targets near your crosshair.", Category.COMBAT); }

    @Override
    public void onDisable() { locked = null; }

    private boolean wanted(Entity e) {
        if (e instanceof PlayerEntity pl) return players.get() && !pl.isSpectator();
        if (e instanceof ArmorStandEntity) return false;
        if (e instanceof Monster) return hostile.get();
        return passive.get();
    }

    private boolean valid(ClientPlayerEntity p, Entity e) {
        return e instanceof LivingEntity && e != p && e.isAlive() && wanted(e) && p.distanceTo(e) <= range.get();
    }

    private Vec3d point(Entity e) {
        return aimAt.get().equals("Head") ? e.getEyePos() : e.getBoundingBox().getCenter();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.currentScreen != null) return;

        Entity target = null;
        if (lockOn.get() && locked != null && valid(p, locked)) target = locked;

        if (target == null) {
            locked = null;
            Vec3d eye = p.getEyePos();
            double bestAngle = Double.MAX_VALUE;
            for (Entity e : mc.world.getEntities()) {
                if (!valid(p, e)) continue;
                float[] rot = RotationUtil.rotationTo(eye, point(e));
                double angle = Math.hypot(MathHelper.wrapDegrees(rot[0] - p.getYaw()), rot[1] - p.getPitch());
                if (angle > fov.get() / 2.0 || angle >= bestAngle) continue;
                bestAngle = angle;
                target = e;
            }
            if (target == null) return;
            if (lockOn.get()) locked = target;
        }

        float[] rot = RotationUtil.rotationTo(p.getEyePos(), point(target));
        float dYaw = MathHelper.wrapDegrees(rot[0] - p.getYaw());
        float dPitch = rot[1] - p.getPitch();
        float s = smooth.get().floatValue();
        p.setYaw(p.getYaw() + dYaw / s);
        p.setPitch(MathHelper.clamp(p.getPitch() + dPitch / s, -90f, 90f));
    }
}
