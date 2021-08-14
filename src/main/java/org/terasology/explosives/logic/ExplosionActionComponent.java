// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.actions.ActionTarget;
import org.terasology.engine.logic.health.EngineDamageTypes;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 */
public class ExplosionActionComponent implements Component<ExplosionActionComponent> {
    public ActionTarget relativeTo = ActionTarget.Instigator;
    public int damageAmount = 1000;
    public Prefab damageType = EngineDamageTypes.EXPLOSIVE.get();
    public int maxRange = 64;

    @Override
    public void copyFrom(ExplosionActionComponent other) {
        this.relativeTo = other.relativeTo;
        this.damageAmount = other.damageAmount;
        this.damageType = other.damageType;
        this.maxRange = other.maxRange;
    }
}
