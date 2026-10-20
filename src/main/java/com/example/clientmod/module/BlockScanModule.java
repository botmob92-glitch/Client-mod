package com.example.clientmod.module;

import com.example.clientmod.render.Projector;
import com.example.clientmod.setting.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Base for modules that highlight certain blocks around you (storage ESP, ore ESP...). */
public abstract class BlockScanModule extends Module {
    protected final Setting<Double> range = add(Setting.number("Range", 20.0, 8.0, 32.0, 4.0));
    protected final Setting<Boolean> labels = add(Setting.bool("Labels", true));
    protected final Setting<Boolean> glow = add(Setting.bool("Glow", true));

    private record Found(BlockPos pos, int color, String label) {}
    private final List<Found> found = new ArrayList<>();
    private int timer;

    protected BlockScanModule(String name, String description) { super(name, description, Category.RENDER); }

    /** RGB colour (0xRRGGBB) to highlight this block with, or -1 to ignore it. */
    protected abstract int colorFor(BlockState state);

    @Override
    public void onDisable() { found.clear(); timer = 0; }

    @Override
    public void onTick(MinecraftClient mc) {
        if (--timer > 0) return;
        timer = 20;
        found.clear();
        BlockPos center = mc.player.getBlockPos();
        int r = range.get().intValue();
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dy = -r; dy <= r; dy++) {
            int y = center.getY() + dy;
            if (mc.world.isOutOfHeightLimit(y)) continue;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    m.set(center.getX() + dx, y, center.getZ() + dz);
                    BlockState st = mc.world.getBlockState(m);
                    if (st.isAir()) continue;
                    int c = colorFor(st);
                    if (c < 0) continue;
                    found.add(new Found(m.toImmutable(), c, st.getBlock().getName().getString()));
                }
            }
        }
        Vec3d eye = mc.player.getEyePos();
        found.sort(Comparator.comparingDouble(f -> Vec3d.ofCenter(f.pos()).squaredDistanceTo(eye)));
        if (found.size() > 300) found.subList(300, found.size()).clear();
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || found.isEmpty()) return;
        Projector pr = new Projector(mc);
        for (Found f : found) {
            int[] b = pr.project(new Box(f.pos()));
            if (b == null) continue;
            String label = null;
            if (labels.get()) {
                int dist = (int) pr.eye().distanceTo(Vec3d.ofCenter(f.pos()));
                label = f.label() + " " + dist + "m";
            }
            Projector.drawBox(ctx, mc, b, f.color(), label, glow.get());
        }
    }
}
