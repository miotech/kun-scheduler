package com.miotech.kun.workflow.core.model.entity;

import javax.annotation.Nullable;

/**
 * @deprecated replaced by {@link com.miotech.kun.workflow.core.model.lineage.DataStore}
 */
@Deprecated
public abstract class Entity {
    @Nullable
    private final Entity parent;
    private final EntityType type;

    public Entity(EntityType type) {
        this(null, type);
    }

    public Entity(Entity parent, EntityType type) {
        this.parent = parent;
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }

    public Entity getParent() {
        return parent;
    }

    public String getIdentifier() {
        String part = identifierPart();
        if (part.indexOf(':') != -1) {
            throw new IllegalStateException("Identifier should not contains semicolon. entity=" + serialize());
        }
        return parent != null ? parent.getIdentifier() : "" + ":" + part;
    }

    protected abstract String identifierPart();

    public String serialize() {
        // TODO
        return null;
    }
}
