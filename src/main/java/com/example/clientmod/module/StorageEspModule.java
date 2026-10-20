package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.block.*;

/** Highlights chests, barrels, shulker boxes etc. through walls. */
public class StorageEspModule extends BlockScanModule {
    private final Setting<Boolean> chests = add(Setting.bool("Chests", true));
    private final Setting<Boolean> barrels = add(Setting.bool("Barrels", true));
    private final Setting<Boolean> shulkers = add(Setting.bool("Shulker boxes", true));
    private final Setting<Boolean> ender = add(Setting.bool("Ender chests", true));
    private final Setting<Boolean> other = add(Setting.bool("Furnaces / hoppers", false));

    public StorageEspModule() { super("Storage ESP", "Shows storage blocks through walls."); }

    @Override
    protected int colorFor(BlockState st) {
        Block b = st.getBlock();
        if (b instanceof TrappedChestBlock) return chests.get() ? 0xEF5350 : -1;
        if (b instanceof ChestBlock) return chests.get() ? 0xFFA726 : -1;
        if (b instanceof BarrelBlock) return barrels.get() ? 0xB07A4A : -1;
        if (b instanceof ShulkerBoxBlock) return shulkers.get() ? 0xFF7EB6 : -1;
        if (b instanceof EnderChestBlock) return ender.get() ? 0xB388FF : -1;
        if (other.get() && (b instanceof AbstractFurnaceBlock || b instanceof HopperBlock || b instanceof DispenserBlock))
            return 0x90A4AE;
        return -1;
    }
}
