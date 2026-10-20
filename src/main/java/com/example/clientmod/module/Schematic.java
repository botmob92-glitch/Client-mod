package com.example.clientmod.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a Litematica (.litematic) file into a list of (relative position, block).
 * v1 limitation: only the block TYPE is kept (default state), so rotation/facing is ignored.
 */
public final class Schematic {
    public record Entry(BlockPos rel, Block block) {}

    public final List<Entry> entries = new ArrayList<>();

    public static Schematic load(Path file) throws IOException {
        NbtCompound root = NbtIo.readCompressed(file, NbtSizeTracker.ofUnlimitedBytes());
        NbtCompound regions = root.getCompoundOrEmpty("Regions");
        Schematic out = new Schematic();

        for (String name : regions.getKeys()) {
            NbtCompound region = regions.getCompoundOrEmpty(name);
            NbtCompound pos = region.getCompoundOrEmpty("Position");
            NbtCompound size = region.getCompoundOrEmpty("Size");
            int sx = size.getInt("x", 0), sy = size.getInt("y", 0), sz = size.getInt("z", 0);
            // Sizes can be negative: the region then extends backwards from Position.
            int minX = pos.getInt("x", 0) + (sx < 0 ? sx + 1 : 0);
            int minY = pos.getInt("y", 0) + (sy < 0 ? sy + 1 : 0);
            int minZ = pos.getInt("z", 0) + (sz < 0 ? sz + 1 : 0);
            sx = Math.abs(sx); sy = Math.abs(sy); sz = Math.abs(sz);

            NbtList paletteNbt = region.getListOrEmpty("BlockStatePalette");
            List<Block> palette = new ArrayList<>();
            for (int i = 0; i < paletteNbt.size(); i++) {
                String id = paletteNbt.getCompoundOrEmpty(i).getString("Name", "minecraft:air");
                palette.add(Registries.BLOCK.get(Identifier.of(id)));
            }
            long[] states = region.getLongArray("BlockStates").orElse(new long[0]);
            if (palette.isEmpty() || states.length == 0) continue;

            int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(palette.size() - 1));
            long mask = (1L << bits) - 1;
            long total = (long) sx * sy * sz;

            for (long i = 0; i < total; i++) {
                long bitIndex = i * bits;
                int start = (int) (bitIndex >>> 6);
                int offset = (int) (bitIndex & 63);
                if (start >= states.length) break;
                long v = states[start] >>> offset;
                if (offset + bits > 64 && start + 1 < states.length) v |= states[start + 1] << (64 - offset);
                int id = (int) (v & mask);
                if (id >= palette.size()) continue;

                Block block = palette.get(id);
                BlockState def = block.getDefaultState();
                if (def.isAir()) continue;

                int x = (int) (i % sx);
                int z = (int) ((i / sx) % sz);
                int y = (int) (i / ((long) sx * sz));
                out.entries.add(new Entry(new BlockPos(minX + x, minY + y, minZ + z), block));
            }
        }

        // Normalise so the lowest corner is (0,0,0), then build bottom-up.
        if (!out.entries.isEmpty()) {
            int mx = Integer.MAX_VALUE, my = Integer.MAX_VALUE, mz = Integer.MAX_VALUE;
            for (Entry e : out.entries) {
                mx = Math.min(mx, e.rel.getX()); my = Math.min(my, e.rel.getY()); mz = Math.min(mz, e.rel.getZ());
            }
            final int fx = mx, fy = my, fz = mz;
            List<Entry> shifted = new ArrayList<>(out.entries.size());
            for (Entry e : out.entries) shifted.add(new Entry(e.rel.add(-fx, -fy, -fz), e.block));
            shifted.sort(Comparator.comparingInt((Entry e) -> e.rel.getY())
                    .thenComparingInt(e -> e.rel.getX()).thenComparingInt(e -> e.rel.getZ()));
            out.entries.clear();
            out.entries.addAll(shifted);
        }
        return out;
    }
}
