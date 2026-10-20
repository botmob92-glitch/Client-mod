package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/** Attacks like pressing the attack button. Can require you to be looking at the target. */
public class AutoAttackModule extends Module {
    private final Setting<Boolean> mustLook = add(Setting.bool("Must look at target", true));
    private final Setting<String> critMode = add(Setting.mode("Crits", "Normal", "Normal", "Only crits"));
    private final Setting<Boolean> playersOnly = add(Setting.bool("Players only", true));
    private final Setting<Double> range = add(Setting.number("Range", 3.0, 1.0, 6.0, 0.25));
    private final Setting<Double> minCharge = add(Setting.number("Min charge", 0.9, 0.0, 1.0, 0.1));

    public AutoAttackModule() { super("Auto Attack", "Attacks targets for you.", Category.COMBAT); }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (p.getAttackCooldownProgress(0.5f) < minCharge.get()) return;

        Entity target = findTarget(mc, p);
        if (target == null) return;

        if (critMode.get().equals("Only crits") && !canCrit(p)) return;

        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
    }

    private Entity findTarget(MinecraftClient mc, ClientPlayerEntity p) {
        if (mustLook.get()) {
            Entity looked = mc.targetedEntity; // what your crosshair is on
            return valid(p, looked) ? looked : null;
        }
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlayerEntity other : mc.world.getPlayers()) {
            if (!valid(p, other)) continue;
            double d = p.distanceTo(other);
            if (d < bestDist) { bestDist = d; best = other; }
        }
        return best;
    }

    private boolean valid(ClientPlayerEntity p, Entity e) {
        if (e == null || e == p || !e.isAlive()) return false;
        if (playersOnly.get() && !(e instanceof PlayerEntity)) return false;
        return p.distanceTo(e) <= range.get();
    }

    /** Vanilla crit rules: falling, not on ground, not climbing/swimming/riding. */
    private boolean canCrit(ClientPlayerEntity p) {
        return p.fallDistance > 0 && !p.isOnGround() && !p.isClimbing()
                && !p.isTouchingWater() && !p.hasVehicle();
    }
}
