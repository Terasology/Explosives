// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

// force block active so that we can put this on a block while waiting for the explosion
@ForceBlockActive
public class TimedExplosionComponent implements Component<TimedExplosionComponent> {
    public long fuseTimeMs;

    @Override
    public void copy(TimedExplosionComponent other) {
        this.fuseTimeMs = other.fuseTimeMs;
    }
}
