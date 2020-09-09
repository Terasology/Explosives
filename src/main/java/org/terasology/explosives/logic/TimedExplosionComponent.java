// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.ForceBlockActive;

// force block active so that we can put this on a block while waiting for the explosion
@ForceBlockActive
public class TimedExplosionComponent implements Component {
    public long fuseTimeMs;
}
