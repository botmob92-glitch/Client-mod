package com.example.clientmod.module;

import com.example.clientmod.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;

/** X-ray style: highlights ores through walls with bright boxes. */
public class OreEspModule extends BlockScanModule {
    private final Setting<Boolean> diamond = add(Setting.bool("Diamond", true));
    private final Setting<Boolean> emerald = add(Setting.bool("Emerald", true));
    private final Setting<Boolean> debris = add(Setting.bool("Ancient debris", true));
    private final Setting<Boolean> gold = add(Setting.bool("Gold", true));
    private final Setting<Boolean> iron = add(Setting.bool("Iron", false));
    private final Setting<Boolean> coal = add(Setting.bool("Coal", false));
    private final Setting<Boolean> redstone = add(Setting.bool("Redstone", false));
    private final Setting<Boolean> lapis = add(Setting.bool("Lapis", false));
    private final Setting<Boolean> copper = add(Setting.bool("Copper", false));

    public OreEspModule() { super("Ore ESP", "Shows ores through walls (X-ray style)."); }

    @Override
    protected int colorFor(BlockState st) {
        if (st.isIn(BlockTags.DIAMOND_ORES)) return diamond.get() ? 0x40E0FF : -1;
        if (st.isIn(BlockTags.EMERALD_ORES)) return emerald.get() ? 0x2EE66B : -1;
        Block b = st.getBlock();
        if (b == Blocks.ANCIENT_DEBRIS) return debris.get() ? 0xB5651D : -1;
        if (st.isIn(BlockTags.GOLD_ORES) || b == Blocks.NETHER_GOLD_ORE) return gold.get() ? 0xFFD54F : -1;
        if (st.isIn(BlockTags.IRON_ORES)) return iron.get() ? 0xE0B89A : -1;
        if (st.isIn(BlockTags.COAL_ORES)) return coal.get() ? 0x9E9E9E : -1;
        if (st.isIn(BlockTags.REDSTONE_ORES)) return redstone.get() ? 0xFF3B3B : -1;
        if (st.isIn(BlockTags.LAPIS_ORES)) return lapis.get() ? 0x3F6BFF : -1;
        if (st.isIn(BlockTags.COPPER_ORES)) return copper.get() ? 0xFF8A50 : -1;
        return -1;
    }
}
