// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.destruction.EngineDamageTypes;

/**
 *
 */
public class TunnelActionComponent implements Component {
    /**
     * The most blocks that can be destroyed before the action ends (counts duplicates, so actually way lower)
     */
    public int maxDestroyedBlocks = 5000;

    /**
     * How many effects to display at the most
     */
    public int maxParticalEffects = 4;

    /**
     * The max number of "steps" we'll take along the direction of the tunnel to pick explosive points
     */
    public int maxTunnelDepth = 64;

    /**
     * The max number of rays to cast at each chosen spot in the path of the tunnel to hit target blocks
     */
    public int maxRaysCast = 512;

    public int damageAmount = 1000;

    public Prefab damageType = EngineDamageTypes.EXPLOSIVE.get();

    /**
     * The amount of block positions that should be skipped from selection
     */
    public float thoroughness = 0.25f;

    public float explosiveForce = 200f;


}
