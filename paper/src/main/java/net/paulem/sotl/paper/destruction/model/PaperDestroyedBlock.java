package net.paulem.sotl.paper.destruction.model;

import net.paulem.sotl.common.destruction.model.DestroyedBlock;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public record PaperDestroyedBlock(Location pos, BlockData state, ItemStack[] contents) implements DestroyedBlock {
}
