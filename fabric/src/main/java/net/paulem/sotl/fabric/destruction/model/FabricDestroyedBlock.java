package net.paulem.sotl.fabric.destruction.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.paulem.sotl.common.destruction.model.DestroyedBlock;

import java.util.List;

public record FabricDestroyedBlock(BlockPos pos, BlockState state, List<ItemStack> contents) implements DestroyedBlock {
}
