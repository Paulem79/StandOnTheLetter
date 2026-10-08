package net.paulem.sotl.paper.destruction.model;

import net.paulem.sotl.common.destruction.model.StoredEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntitySnapshot;

public record PaperStoredEntity(EntitySnapshot snapshot, Location pos) implements StoredEntity {
}
