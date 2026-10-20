package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Builds a .litematic from the "schematics" folder in your game folder.
 * It starts 2 blocks in front of you when you switch it on and builds toward +X / +Z.
 * Blocks must be in your HOTBAR. The camera turns with eased, mouse-like movement.
 */
public class SchematicBuilderModule extends Module {
    private final Setting<String> file = add(Setting.mode("Schematic", "(none)", "(none)"));
    private final Setting<Double> turnSpeed = add(Setting.number("Turn speed", 12.0, 3.0, 40.0, 1.0));
    private final Setting<Double> delay = add(Setting.number("Delay (ticks)", 4.0, 1.0, 20.0, 1.0));
    private final Setting<Double> reach = add(Setting.number("Reach", 4.5, 2.0, 5.0, 0.25));

    private Schematic schematic;
    private BlockPos origin;
    private int cursor, cooldown, msgTimer;

    public SchematicBuilderModule() {
        super("Schematic Builder", "Builds a .litematic for you.", Category.MISC);
        refreshFiles();
    }

    private Path dir() { return FabricLoader.getInstance().getGameDir().resolve("schematics"); }

    @Override
    public void onGuiOpen() { refreshFiles(); }

    private void refreshFiles() {
        List<String> names = new ArrayList<>();
        try {
            Path d = dir();
            if (Files.isDirectory(d)) {
                try (Stream<Path> s = Files.list(d)) {
                    s.filter(p -> p.getFileName().toString().endsWith(".litematic"))
                            .forEach(p -> names.add(p.getFileName().toString()));
                }
            }
        } catch (IOException ignored) {}
        if (names.isEmpty()) names.add("(none)");
        file.modes = names;
        if (!names.contains(file.get())) file.set(names.get(0));
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        schematic = null;
        if (p == null || file.get().equals("(none)")) {
            if (p != null) p.sendMessage(Text.literal("Builder: put a .litematic in the schematics folder"), true);
            setEnabled(false);
            return;
        }
        try {
            schematic = Schematic.load(dir().resolve(file.get()));
        } catch (Exception e) {
            p.sendMessage(Text.literal("Builder: could not read " + file.get()), true);
            setEnabled(false);
            return;
        }
        origin = p.getBlockPos().offset(p.getHorizontalFacing(), 2);
        cursor = 0;
        cooldown = 0;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (schematic == null || p == null || mc.interactionManager == null || mc.currentScreen != null) return;

        // Skip everything already built.
        while (cursor < schematic.entries.size() && isBuilt(mc, schematic.entries.get(cursor))) cursor++;
        if (cursor >= schematic.entries.size()) {
            p.sendMessage(Text.literal("Builder: done!"), true);
            setEnabled(false);
            return;
        }

        if (--msgTimer <= 0) {
            msgTimer = 20;
            p.sendMessage(Text.literal("Builder: " + cursor + "/" + schematic.entries.size()), true);
        }

        Vec3d eye = p.getEyePos();
        double maxReach = reach.get();
        int end = Math.min(schematic.entries.size(), cursor + 800);

        for (int i = cursor; i < end; i++) {
            Schematic.Entry e = schematic.entries.get(i);
            BlockPos target = origin.add(e.rel());
            if (isBuilt(mc, e)) continue;
            BlockState here = mc.world.getBlockState(target);
            if (!here.isAir() && !here.isReplaceable()) continue;

            Item item = e.block().asItem();
            int slot = hotbarSlot(p, item);
            if (slot < 0) continue; // not in hotbar

            // Find a solid neighbour to click against.
            for (Direction d : Direction.values()) {
                BlockPos nb = target.offset(d);
                BlockState ns = mc.world.getBlockState(nb);
                if (ns.isAir() || ns.isReplaceable() || !ns.getFluidState().isEmpty()) continue;

                Direction face = d.getOpposite();
                Vec3d hit = Vec3d.ofCenter(nb).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
                if (eye.distanceTo(hit) > maxReach) continue;

                // Turn like a hand easing onto the spot, then click.
                float[] rot = RotationUtil.rotationTo(eye, hit);
                double left = RotationUtil.turnToward(p, rot[0], rot[1], turnSpeed.get(), 0.35);

                if (cooldown > 0) { cooldown--; return; }
                if (left > 3.0) return; // still turning

                p.getInventory().setSelectedSlot(slot);
                mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, new BlockHitResult(hit, face, nb, false));
                p.swingHand(Hand.MAIN_HAND);
                cooldown = delay.get().intValue();
                return;
            }
        }

        if (msgTimer == 19) p.sendMessage(Text.literal("Builder: move closer / check your hotbar"), true);
    }

    private boolean isBuilt(MinecraftClient mc, Schematic.Entry e) {
        return mc.world.getBlockState(origin.add(e.rel())).getBlock() == e.block();
    }

    private int hotbarSlot(ClientPlayerEntity p, Item item) {
        for (int i = 0; i < 9; i++) if (p.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }
}
