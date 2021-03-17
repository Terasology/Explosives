// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.explosives.logic;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;

import java.util.List;
import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ExplosionAuthoritySystem extends BaseComponentSystem {
    public static final String DELAYED_EXPLOSION_ACTION_ID = "Delayed Explosion";

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private DelayManager delayManager;

    private Random random = new FastRandom();
    private List<Optional<StaticSound>> explosionSounds = Lists.newArrayList();

    @Override
    public void initialise() {
        explosionSounds.add(Assets.getSound("CoreAssets:Explode1"));
        explosionSounds.add(Assets.getSound("CoreAssets:Explode2"));
        explosionSounds.add(Assets.getSound("CoreAssets:Explode3"));
        explosionSounds.add(Assets.getSound("CoreAssets:Explode4"));
        explosionSounds.add(Assets.getSound("CoreAssets:Explode5"));
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, ExplosionActionComponent explosionComp) {
        Vector3f origin = null;
        switch (explosionComp.relativeTo) {
            case Self:
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                if (loc != null) {
                    origin = loc.getWorldPosition(new Vector3f());
                    if (!origin.isFinite()) {
                        origin = null;
                    }
                }
                break;
            case Instigator:
                origin = event.getInstigatorLocation();
                break;
            default:
                origin = event.getTargetLocation();
                break;
        }

        if (origin == null) {
            return;
        }

        doExplosion(explosionComp, origin, EntityRef.NULL);
    }

    private StaticSound getRandomExplosionSound() {
        return explosionSounds.get(random.nextInt(0, explosionSounds.size() - 1)).get();
    }

    void doExplosion(ExplosionActionComponent explosionComp, Vector3f origin, EntityRef instigatingBlockEntity) {
        EntityBuilder builder = entityManager.newBuilder("CoreAssets:smokeExplosion");
        builder.getComponent(LocationComponent.class).setWorldPosition(origin);
        EntityRef smokeEntity = builder.build();

        smokeEntity.send(new PlaySoundEvent(getRandomExplosionSound(), 1f));

        Vector3i blockPos = new Vector3i();
        for (int i = 0; i < explosionComp.maxRange; i++) {
            Vector3f direction = new Vector3f();
            random.nextVector3f(1.0f, direction);

            for (int j = 0; j < 4; j++) {
                Vector3f target = new Vector3f(origin);

                target.x += direction.x * j;
                target.y += direction.y * j;
                target.z += direction.z * j;
                blockPos.set((int) target.x, (int) target.y, (int) target.z);
                Block currentBlock = worldProvider.getBlock(blockPos);

                /* PHYSICS */
                if (currentBlock.isDestructible()) {
                    EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                    // allow explosions to chain together,  but do not chain on the instigating block
                    if (!blockEntity.equals(instigatingBlockEntity) && blockEntity.hasComponent(ExplosionActionComponent.class)) {
                        doExplosion(blockEntity.getComponent(ExplosionActionComponent.class), new Vector3f(blockPos), blockEntity);
                    } else {
                        blockEntity.send(new DoDamageEvent(explosionComp.damageAmount, explosionComp.damageType));
                    }
                }
            }
        }
    }

    @ReceiveEvent(components = ItemComponent.class)
    public void onActivateFuseOnBlock(ActivateEvent event, EntityRef entityRef, TimedExplosionComponent timedExplosionComponent) {
        if (event.getTarget().hasComponent(BlockComponent.class) && event.getTarget().hasComponent(ExplosionActionComponent.class)
            && !event.getTarget().hasComponent(TimedExplosionComponent.class)) {
            Optional<StaticSound> fuseBurningSound = Assets.getSound("CoreAssets:FuseBurning");
            if (fuseBurningSound.isPresent()) {
                event.getTarget().send(new PlaySoundEvent(fuseBurningSound.get(), 1f));
            }
            // add a timed explosion to the block so that it stays active
            event.getTarget().addComponent(new TimedExplosionComponent());

            delayManager.addDelayedAction(event.getTarget(), DELAYED_EXPLOSION_ACTION_ID, timedExplosionComponent.fuseTimeMs);
        }
    }

    @ReceiveEvent
    public void onDelayedExplosion(DelayedActionTriggeredEvent event, EntityRef entityRef,
                                    ExplosionActionComponent explosionActionComponent) {
        if (event.getActionId().equals(DELAYED_EXPLOSION_ACTION_ID)) {
            //check if the exploding entity is a block or not
            if (entityRef.hasComponent(BlockComponent.class)) {
                BlockComponent blockComponent = entityRef.getComponent(BlockComponent.class);
                // always destroy the block that caused the explosion
                worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
                // create the explosion from the block's location
                doExplosion(explosionActionComponent, new Vector3f(blockComponent.getPosition()), entityRef);
            } else if (entityRef.hasComponent(LocationComponent.class)) {
                // get the position of the non-block entity to make it explode from there
                Vector3f position = new Vector3f();
                entityRef.getComponent(LocationComponent.class).getWorldPosition(position);
                // destroy the non-block entity
                entityRef.destroy();
                // create the explosion from the non-block entity location
                doExplosion(explosionActionComponent, position, EntityRef.NULL);
            }
        }
    }
}
