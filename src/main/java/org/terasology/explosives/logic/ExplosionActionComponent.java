// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.actions.ActionTarget;
import org.terasology.engine.logic.destruction.EngineDamageTypes;

/**
 *
 */
public class ExplosionActionComponent implements Component {
    public ActionTarget relativeTo = ActionTarget.Instigator;
    public int damageAmount = 1000;
    public Prefab damageType = EngineDamageTypes.EXPLOSIVE.get();
    public int maxRange = 64;
}
