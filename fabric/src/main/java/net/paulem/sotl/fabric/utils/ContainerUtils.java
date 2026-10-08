package net.paulem.sotl.fabric.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public final class ContainerUtils {

    private ContainerUtils() {
    }

    public static List<ItemStack> captureAndClearContainer(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            List<ItemStack> contents = new ArrayList<>(container.getContainerSize());
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                contents.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            container.clearContent();
            return contents;
        }
        return null;
    }

    public static void restoreContainer(Level level, BlockPos pos, List<ItemStack> contents) {
        if (contents == null) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            for (int i = 0; i < Math.min(container.getContainerSize(), contents.size()); i++) {
                container.setItem(i, contents.get(i));
            }
            container.setChanged();
        }
    }
}
