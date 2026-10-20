package com.example.clientmod.module;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {
    private RotationUtil() {}

    /** Returns {yaw, pitch} needed to look from 'from' toward 'to'. */
    public static float[] rotationTo(Vec3d from, Vec3d to) {
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new float[]{yaw, pitch};
    }

    /**
     * Eased turn: big angles turn fast, then slow down as the aim point gets close,
     * like a hand easing onto a spot. Returns the angle (degrees) still left to turn.
     */
    public static double turnToward(ClientPlayerEntity p, float yaw, float pitch, double maxStep, double ease) {
        float dYaw = MathHelper.wrapDegrees(yaw - p.getYaw());
        float dPitch = pitch - p.getPitch();
        double dist = Math.hypot(dYaw, dPitch);
        if (dist < 0.01) return 0;
        double step = Math.min(dist, Math.min(maxStep, Math.max(0.5, dist * ease)));
        double k = step / dist;
        p.setYaw(p.getYaw() + (float) (dYaw * k));
        p.setPitch(MathHelper.clamp(p.getPitch() + (float) (dPitch * k), -90f, 90f));
        return dist - step;
    }
}
