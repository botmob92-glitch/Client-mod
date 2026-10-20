package com.example.clientmod.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Turns world boxes into 2D screen rectangles so we can draw ESP boxes on the HUD
 * (no custom world rendering needed, which keeps it working across render changes).
 */
public final class Projector {
    private final Vec3d eye, f, r, u;
    private final double scale;
    private final int w, h;

    public Projector(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        eye = p.getEyePos();
        double yaw = Math.toRadians(p.getYaw()), pitch = Math.toRadians(p.getPitch());
        double cp = Math.cos(pitch);
        f = new Vec3d(-Math.sin(yaw) * cp, -Math.sin(pitch), Math.cos(yaw) * cp); // forward
        r = new Vec3d(-Math.cos(yaw), 0, -Math.sin(yaw));                          // right
        u = r.crossProduct(f);                                                     // up
        w = mc.getWindow().getScaledWidth();
        h = mc.getWindow().getScaledHeight();
        double fov = Math.toRadians(mc.options.getFov().getValue());
        scale = (h / 2.0) / Math.tan(fov / 2.0);
    }

    public Vec3d eye() { return eye; }

    /** Returns {minX, minY, maxX, maxY} on screen, or null if off-screen / behind you. */
    public int[] project(Box box) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            Vec3d c = new Vec3d((i & 1) == 0 ? box.minX : box.maxX,
                                (i & 2) == 0 ? box.minY : box.maxY,
                                (i & 4) == 0 ? box.minZ : box.maxZ);
            Vec3d d = c.subtract(eye);
            double z = d.dotProduct(f);
            if (z < 0.05) return null;
            double sx = w / 2.0 + d.dotProduct(r) / z * scale;
            double sy = h / 2.0 - d.dotProduct(u) / z * scale;
            minX = Math.min(minX, sx); maxX = Math.max(maxX, sx);
            minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
        }
        if (maxX < 0 || minX > w || maxY < 0 || minY > h) return null;
        return new int[]{(int) minX, (int) minY, (int) maxX, (int) maxY};
    }

    /** Filled + outlined rectangle, with an optional label above it. */
    public static void drawBox(DrawContext ctx, MinecraftClient mc, int[] b, int rgb, String label) {
        int edge = 0xFF000000 | rgb;
        int fill = 0x33000000 | (rgb & 0xFFFFFF);
        int x1 = b[0], y1 = b[1], x2 = Math.max(b[2], b[0] + 3), y2 = Math.max(b[3], b[1] + 3);
        ctx.fill(x1, y1, x2, y2, fill);
        ctx.fill(x1, y1, x2, y1 + 1, edge);
        ctx.fill(x1, y2 - 1, x2, y2, edge);
        ctx.fill(x1, y1, x1 + 1, y2, edge);
        ctx.fill(x2 - 1, y1, x2, y2, edge);
        if (label != null) ctx.drawTextWithShadow(mc.textRenderer, label, x1, y1 - 10, edge);
    }
}
