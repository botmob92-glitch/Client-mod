package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Smoothly turns your camera toward the closest player near your crosshair. */
public class AimAssistModule extends Module {
    private final Setting<Double> range = add(Setting.number("Range", 4.5, 1.0, 8.0, 0.5));
    private final Setting<Double> fov = add(Setting.number("FOV", 90.0, 10.0, 360.0, 10.0));
    private final Setting<Double> smooth = add(Setting.number("Smoothness", 6.0, 1.0, 20.0, 1.0));

    public AimAssistModule() { super("Aim Assist", "Follows players near your crosshair.", Category.COMBAT); }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.currentScreen != null) return;

        Vec3d eye = p.getEyePos();
        float bestYaw = 0, bestPitch = 0;
        double bestAngle = Double.MAX_VALUE;

        for (PlayerEntity other : mc.world.getPlayers()) {
            if (other == p || !other.isAlive() || other.isSpectator()) continue;
            if (p.distanceTo(other) > range.get()) continue;

            float[] rot = RotationUtil.rotationTo(eye, other.getBoundingBox().getCenter());
            float dYaw = MathHelper.wrapDegrees(rot[0] - p.getYaw());
            float dPitch = rot[1] - p.getPitch();
            double angle = Math.hypot(dYaw, dPitch);
            if (angle > fov.get() / 2.0 || angle >= bestAngle) continue;

            bestAngle = angle;
            bestYaw = dYaw;
            bestPitch = dPitch;
        }

        if (bestAngle == Double.MAX_VALUE) return;
        float s = smooth.get().floatValue();
        p.setYaw(p.getYaw() + bestYaw / s);
        p.setPitch(MathHelper.clamp(p.getPitch() + bestPitch / s, -90f, 90f));
    }
}
