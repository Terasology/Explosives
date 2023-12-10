// Copyright 2029 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.explosives.logic;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.health.events.DoDamageEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TunnelAction extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, TunnelActionComponent tunnelActionComponent) {

        Vector3f dir = new Vector3f(event.getDirection());
        dir.mul(4.0f);
        Vector3f origin = new Vector3f(event.getOrigin());
        origin.add(dir);
        Vector3i blockPos = new Vector3i();

        int particleEffects = 0;
        int blockCounter = tunnelActionComponent.maxDestroyedBlocks;
        for (int s = 0; s <= tunnelActionComponent.maxTunnelDepth; s++) {
            origin.add(dir);
            if (!worldProvider.isBlockRelevant(origin)) {
                break;
            }

            for (int i = 0; i < tunnelActionComponent.maxRaysCast; i++) {
                Vector3f direction = new Vector3f();
                random.nextVector3f(direction);
                Vector3f impulse = new Vector3f(direction);
                impulse.mul(tunnelActionComponent.explosiveForce);

                for (int j = 0; j < 3; j++) {
                    Vector3f target = new Vector3f(origin);

                    target.x += direction.x * j;
                    target.y += direction.y * j;
                    target.z += direction.z * j;

                    blockPos.set((int) target.x, (int) target.y, (int) target.z);

                    Block currentBlock = worldProvider.getBlock(blockPos);

                    if (currentBlock.isDestructible()) {
                        if (particleEffects < tunnelActionComponent.maxParticalEffects) {
                            EntityBuilder builder = entityManager.newBuilder("CoreAssets:smokeExplosion");
                            builder.getComponent(LocationComponent.class).setWorldPosition(target);
                            builder.build();
                            particleEffects++;
                        }
                        if (random.nextFloat() < tunnelActionComponent.thoroughness) {
                            EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                            blockEntity.send(new DoDamageEvent(tunnelActionComponent.damageAmount, tunnelActionComponent.damageType));
                        }

                        blockCounter--;
                    }

                    if (blockCounter <= 0) {
                        return;
                    }
                }
            }
        }
        // No blocks were destroyed, so cancel the event
        if (blockCounter == tunnelActionComponent.maxDestroyedBlocks) {
            event.consume();
        }
    }
}
