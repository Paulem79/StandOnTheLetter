package net.paulem.sotl.fabric.destruction.state;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.common.destruction.state.RestoringState;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;
import net.paulem.sotl.fabric.destruction.model.FabricDestroyedBlock;
import net.paulem.sotl.fabric.destruction.model.FabricStoredEntity;
import net.paulem.sotl.fabric.utils.ContainerUtils;

import java.util.List;

public class FabricRestoringState extends RestoringState<FabricDestructionContext> {

    @Override
    public DestructionState<FabricDestructionContext> tick(FabricDestructionContext context, long startTime) {
        initIndices(context);

        ServerLevel world = context.getWorld();
        List<FabricDestroyedBlock> destroyedBlocks = context.getDestroyedBlocks();
        List<FabricStoredEntity> storedEntities = context.getStoredEntities();

        while (blockIndex >= 0) {
            FabricDestroyedBlock block = destroyedBlocks.get(blockIndex--);
            world.setBlock(block.pos(), block.state(), Block.UPDATE_ALL);
            ContainerUtils.restoreContainer(world, block.pos(), block.contents());

            if (System.nanoTime() - startTime >= context.getMaxNanosPerTick()) {
                return this;
            }
        }

        while (entityIndex >= 0) {
            FabricStoredEntity stored = storedEntities.get(entityIndex--);
            if (stored.snapshot() != null) {
                ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), stored.snapshot());
                Entity loaded = EntityType.loadEntityRecursive(input, world, EntitySpawnReason.LOAD, entity -> {
                    entity.setPos(stored.pos().x, stored.pos().y, stored.pos().z);
                    return entity;
                });
                if (loaded != null) {
                    world.addFreshEntity(loaded);
                }
            }

            if (System.nanoTime() - startTime >= context.getMaxNanosPerTick()) {
                return this;
            }
        }

        destroyedBlocks.clear();
        storedEntities.clear();
        return null;
    }
}
