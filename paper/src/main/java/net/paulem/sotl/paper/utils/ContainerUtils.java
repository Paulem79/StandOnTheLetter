package net.paulem.sotl.paper.utils;

import io.papermc.paper.block.TileStateInventoryHolder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public final class ContainerUtils {

    private ContainerUtils() {
    }

    public static ItemStack[] cloneContents(ItemStack[] contents) {
        if (contents == null) {
            return null;
        }
        ItemStack[] copy = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            copy[i] = contents[i] == null ? null : contents[i].clone();
        }
        return copy;
    }

    public static ItemStack[] captureAndClearContainer(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileStateInventoryHolder holder) {
            ItemStack[] contents = cloneContents(holder.getSnapshotInventory().getContents());
            holder.getSnapshotInventory().clear();
            holder.update(true, false);
            if (holder instanceof Chest chest) {
                chest.getBlockInventory().clear();
            } else {
                holder.getInventory().clear();
            }
            return contents;
        }
        return null;
    }

    public static void restoreContainer(Block block, ItemStack[] contents) {
        if (contents == null) {
            return;
        }

        BlockState state = block.getState();
        if (state instanceof TileStateInventoryHolder holder) {
            holder.getSnapshotInventory().setContents(contents);
            holder.update(true, false);
        }
    }
}
