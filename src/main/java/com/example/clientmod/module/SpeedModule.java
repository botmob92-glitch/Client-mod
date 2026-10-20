package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class SpeedModule extends Module {
    private final Setting<Double> multiplier = add(Setting.number("Multiplier", 1.5, 1.0, 5.0, 0.25));
    private final Setting<Boolean> onlyGround = add(Setting.bool("Only on ground", false));

    public SpeedModule() { super("Speed", "Move faster."); }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || p.isSneaking() || p.isTouchingWater()) return;
        if (onlyGround.get() && !p.isOnGround()) return;

        Vec3d v = p.getVelocity();
        double horizontal = Math.hypot(v.x, v.z);
        if (horizontal < 0.01) return; // not moving

        double target = 0.2873 * multiplier.get(); // ~ vanilla sprint speed * multiplier
        double scale = target / horizontal;
        p.setVelocity(v.x * scale, v.y, v.z * scale);
    }
}
