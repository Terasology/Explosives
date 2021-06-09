// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.health.EngineDamageTypes;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 */
public class TunnelActionComponent implements Component<TunnelActionComponent> {
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


    @Override
    public void copy(TunnelActionComponent other) {
        this.maxDestroyedBlocks = other.maxDestroyedBlocks;
        this.maxParticalEffects = other.maxParticalEffects;
        this.maxTunnelDepth = other.maxTunnelDepth;
        this.maxRaysCast = other.maxRaysCast;
        this.damageAmount = other.damageAmount;
        this.damageType = other.damageType;
        this.thoroughness = other.thoroughness;
        this.explosiveForce = other.explosiveForce;
    }
}
