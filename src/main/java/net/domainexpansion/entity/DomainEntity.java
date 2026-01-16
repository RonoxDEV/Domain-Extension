package net.domainexpansion.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.sounds.SoundSource;
import net.domainexpansion.init.DomainExpansionModSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.domainexpansion.init.DomainExpansionModBlocks;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DomainEntity extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_CLOSING = SynchedEntityData.defineId(DomainEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(DomainEntity.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(DomainEntity.class,
            EntityDataSerializers.INT);

    public static final int STATE_INCANTATION = 0;
    public static final int STATE_OPENING = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_CLOSING = 3;

    private static final int PHASE_WAIT = 140; // 0 to 140
    private static final int PHASE_GROW = 280; // 140 to 280 (140 ticks duration)
    private static final int PHASE_ACTIVE = 900; // 280 to 900 (620 ticks active)
    private static final int PHASE_SHRINK = 960; // 900 to 960 (60 ticks shrink)
    private static final int DEATH_TIME = 965; // Final discard

    private UUID ownerUUID;
    private UUID targetUUID;
    private int age = 0;
    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    private final List<UUID> trappedEntities = new ArrayList<>();
    private boolean terrainTransformed = false;
    private boolean ambientStopped = false;
    private int closingTimer = 0; // Local timer (server authoritative)
    private float radius = 0.0f; // Client side display variable
    private final int CLOSING_DURATION = 160; // 8 seconds

    public DomainEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(STATE, STATE_OPENING);
        this.entityData.define(DATA_CLOSING, false);
        this.entityData.define(DATA_RADIUS, 0.0f);
    }

    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public void setTarget(UUID uuid) {
        this.targetUUID = uuid;
    }

    public float getRadius() {
        return this.radius;
    }

    public float getRadius(float partialTicks) {
        return this.radius;
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public UUID getTargetUUID() {
        return this.targetUUID;
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;

        // --- PARTIE 1 : LOGIQUE SERVEUR (Le Cerveau) ---
        if (!this.level().isClientSide) {

            // A. Vérification de la Mort (Déclencheur de fermeture anticipée)
            boolean isClosing = this.entityData.get(DATA_CLOSING);

            // Si on est en phase active (après l'ouverture) et qu'on ne ferme pas encore
            if (!isClosing && this.age > 280) {
                Entity owner = (ownerUUID != null)
                        ? ((net.minecraft.server.level.ServerLevel) level()).getEntity(ownerUUID)
                        : null;
                boolean targetDead = false;
                if (targetUUID != null) {
                    Entity target = ((net.minecraft.server.level.ServerLevel) level()).getEntity(targetUUID);
                    if (target == null || !target.isAlive())
                        targetDead = true;
                }

                // Si quelqu'un est mort OU que le temps est fini (900 ticks)
                boolean ownerDead = (owner != null && !owner.isAlive());
                boolean timeExpired = (this.age >= 900);

                if (ownerDead || targetDead || timeExpired) {
                    System.out.println("DOMAIN CLOSING DEBUG: Age=" + this.age +
                            " OwnerDead=" + ownerDead + " (Owner=" + owner + ")" +
                            " TargetDead=" + targetDead + " (TargetUUID=" + targetUUID + ")" +
                            " TimeExpired=" + timeExpired);

                    this.entityData.set(DATA_CLOSING, true); // On prévient tout le monde
                    this.closingTimer = 0; // Reset du timer interne
                    this.entityData.set(STATE, STATE_CLOSING);

                    // JOUER LE SON
                    this.level().playSound(null, this.blockPosition(), DomainExpansionModSounds.DOMAIN_OUT.get(),
                            SoundSource.MASTER, 1.0F, 1.0F);

                    // SCREEN SHAKE - Fermeture (Camera Shake via Network)
                    int shakeCount = 0;
                    for (Player player : level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(30))) {
                        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                            net.domainexpansion.DomainExpansionMod.PACKET_HANDLER.send(
                                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                                    new net.domainexpansion.network.ShakePacket(5.0f, 30)); // 5.0 intensity, 1 second
                            shakeCount++;
                        }
                    }
                    System.out.println("[DOMAIN] CLOSING SHAKE sent to " + shakeCount + " players!");
                    // Particules de rupture
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.SONIC_BOOM,
                            this.getX(), this.getY() + 1, this.getZ(), 3, 0.5, 0.5, 0.5, 0);
                }
            }

            // B. Calcul de la Taille (Le serveur décide de la taille)
            float currentRadius = 0.0f;

            if (this.entityData.get(DATA_CLOSING)) {
                // MODE FERMETURE (Prioritaire)
                this.entityData.set(STATE, STATE_CLOSING);
                this.closingTimer++;
                // De 22 vers 0 en 160 ticks (8 secondes)
                float progress = (float) this.closingTimer / 160.0f;
                currentRadius = Mth.lerp(progress, 22.0f, 0.0f);

                // Suppression à la fin
                if (this.closingTimer >= 160) {
                    this.restoreTerrain();
                    this.discard();
                }
            } else {
                // MODE NORMAL
                if (this.age < 140) {
                    this.entityData.set(STATE, STATE_INCANTATION);
                    currentRadius = 0.0f; // Attente
                } else if (this.age < 280) {
                    this.entityData.set(STATE, STATE_OPENING);
                    // Ouverture (0 -> 22)
                    float progress = (this.age - 140) / 140.0f;
                    currentRadius = Mth.lerp(progress, 0.0f, 22.0f);

                    // SCREEN SHAKE - Apparition de la sphère (Camera Shake via Network)
                    if (this.age == 140) {
                        int shakeCount = 0;
                        for (Player player : level().getEntitiesOfClass(Player.class,
                                this.getBoundingBox().inflate(30))) {
                            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                                net.domainexpansion.DomainExpansionMod.PACKET_HANDLER.send(
                                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                                        new net.domainexpansion.network.ShakePacket(8.0f, 30)); // 8.0 intensity, 1.5
                                                                                                // seconds
                                shakeCount++;
                            }
                        }
                        System.out.println(
                                "[DOMAIN] OPENING SHAKE sent to " + shakeCount + " players at tick " + this.age);

                        // Particules d'impact MASSIVES (10 au lieu de 5)
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.SONIC_BOOM,
                                this.getX(), this.getY() + 1, this.getZ(), 10, 0.5, 0.5, 0.5, 0);

                        // Anneau de particules de dissipation autour de la sphère
                        for (int i = 0; i < 360; i += 15) {
                            double angle = Math.toRadians(i);
                            double offsetX = Math.cos(angle) * 22.0;
                            double offsetZ = Math.sin(angle) * 22.0;
                            ((ServerLevel) this.level()).sendParticles(ParticleTypes.PORTAL,
                                    this.getX() + offsetX, this.getY(), this.getZ() + offsetZ,
                                    5, 0.2, 0.5, 0.2, 0.1);
                        }
                    }
                } else {
                    this.entityData.set(STATE, STATE_ACTIVE);
                    // Phase Active stable
                    currentRadius = 22.0f;

                    // Logique du Tick 280 (Capture, Terrain, Son Ambiance)
                    if (this.age == 280) {
                        this.level().playSound(null, this.blockPosition(),
                                DomainExpansionModSounds.DOMAIN_AMBIANCE.get(), SoundSource.RECORDS, 1.0F, 1.0F);
                        this.transformTerrain();
                        this.captureTrappedEntities();
                    }
                    if (this.age > 280) {
                        applyEffectsToTarget();
                        enforceBarrier();
                        enforceBindingVow();
                    }
                }
            }

            // C. ENVOI AU CLIENT (Mise à jour de la variable partagée)
            this.entityData.set(DATA_RADIUS, currentRadius);
        }

        // --- PARTIE 2 : LOGIQUE CLIENT (Les Yeux) ---
        // Le client ne calcule RIEN. Il lit juste la valeur décidée par le serveur.
        // Cela garantit une synchro parfaite à 100%.
        this.radius = this.entityData.get(DATA_RADIUS);

        if (this.level().isClientSide && this.entityData.get(DATA_CLOSING) && !ambientStopped) {
            stopAmbientSound();
            ambientStopped = true;
        }
    }

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private void stopAmbientSound() {
        net.minecraft.client.Minecraft.getInstance().getSoundManager()
                .stop(DomainExpansionModSounds.DOMAIN_AMBIANCE.get().getLocation(), null);
    }

    private void transformTerrain() {
        if (terrainTransformed)
            return;

        BlockPos center = this.blockPosition();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        // Max radius for iteration (Air clearance is wider: 24)
        int maxRadius = 24;

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int z = -maxRadius; z <= maxRadius; z++) {
                double distSq = (double) x * x + (double) z * z;

                // 1. The Floor (Platform) - Radius 22
                if (distSq <= 22.0 * 22.0) {
                    BlockPos floorPos = new BlockPos(centerX + x, centerY - 1, centerZ + z);
                    originalBlocks.put(floorPos, level().getBlockState(floorPos));
                    level().setBlock(floorPos, DomainExpansionModBlocks.DOMAIN_OCCULTE.get().defaultBlockState(), 3);
                }

                // 2. Air Clearance (20 blocks high) - Radius 24 (Buffer zone)
                if (distSq <= 24.0 * 24.0) {
                    for (int y = 0; y <= 20; y++) {
                        BlockPos airPos = new BlockPos(centerX + x, centerY + y, centerZ + z);
                        BlockState currentState = level().getBlockState(airPos);

                        // Safety: don't track or replace if already air, and don't replace bedrock
                        if (!currentState.isAir() && currentState.getDestroySpeed(level(), airPos) >= 0) {
                            originalBlocks.put(airPos, currentState);
                            level().setBlock(airPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        terrainTransformed = true;
    }

    private void restoreTerrain() {
        if (!terrainTransformed)
            return;

        for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
            level().setBlock(entry.getKey(), entry.getValue(), 3);
        }
        originalBlocks.clear();
        terrainTransformed = false;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level().isClientSide) {
            restoreTerrain();
        }
    }

    private void updateTrappedEntities() {
        // Optional: Remove dead entities from the list to save memory
        trappedEntities.removeIf(uuid -> {
            Entity e = ((net.minecraft.server.level.ServerLevel) level()).getEntity(uuid);
            return e == null || !e.isAlive();
        });
    }

    private void captureTrappedEntities() {
        trappedEntities.clear();

        // 1. Always add Owner
        if (ownerUUID != null) {
            trappedEntities.add(ownerUUID);
        }

        // 2. Find closest Target and Expel others
        AABB scanBox = this.getBoundingBox().inflate(22.0);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, scanBox);

        // Sort by distance to center
        nearby.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(this)));

        boolean targetFound = false;

        for (LivingEntity entity : nearby) {
            // Skip Owner
            if (entity.getUUID().equals(ownerUUID))
                continue;

            if (!targetFound) {
                // FIRST non-owner entity is the TARGET
                this.targetUUID = entity.getUUID();
                this.trappedEntities.add(this.targetUUID);
                targetFound = true;

                // Ensure target is properly synced/saved if needed
                // (Already handled by onSyncedData/saveData)
            } else {
                // All OTHER entities are INTRUDERS -> Expel
                Vec3 center = this.position();
                Vec3 pos = entity.position();
                Vec3 dir = pos.subtract(center).normalize();

                // Safety: if exactly at center, choose random dir
                if (dir.lengthSqr() < 0.0001) {
                    dir = new Vec3(1, 0, 0);
                }

                Vec3 safeSpot = center.add(dir.scale(25.0));

                // Teleport out
                entity.teleportTo(safeSpot.x, entity.getY(), safeSpot.z);
            }
        }
    }

    private void enforceBarrier() {
        double barrierRadius = 20.0;
        double interactionRadius = 25.0; // Check entities within this range

        AABB checkBox = this.getBoundingBox().inflate(interactionRadius);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, checkBox);

        for (LivingEntity entity : entities) {
            double dist = entity.distanceTo(this);
            boolean isTrapped = trappedEntities.contains(entity.getUUID());

            if (isTrapped) {
                // CASE A: Prisoner trying to escape (dist > 19.0)
                // We keep them inside radius 19
                if (dist > 19.0) {
                    Vec3 center = this.position();
                    Vec3 pos = entity.position();
                    Vec3 direction = center.subtract(pos).normalize();
                    // Push back inward
                    entity.setDeltaMovement(direction.scale(0.5));
                    entity.hurtMarked = true;
                }
            } else {
                // CASE B: Intruder trying to enter (dist < 21.0)
                // We keep them outside radius 21
                if (dist < 21.0) {
                    Vec3 center = this.position();
                    Vec3 pos = entity.position();
                    Vec3 direction = pos.subtract(center).normalize();
                    // Push back outward stronger
                    entity.setDeltaMovement(direction.scale(1.0));
                    entity.hurtMarked = true;
                }
            }
        }
    }

    private void enforceBindingVow() {
        if (level().isClientSide)
            return;

        // Copy list to avoid ConcurrentModificationException if we remove elements
        List<UUID> toRemove = new ArrayList<>();

        for (UUID uuid : trappedEntities) {
            Entity victim = ((net.minecraft.server.level.ServerLevel) level()).getEntity(uuid);

            // If entity is unloaded or dead, remove from tracking
            if (victim == null || !victim.isAlive()) {
                toRemove.add(uuid);
                continue;
            }

            double dist = victim.distanceTo(this);

            // Binding Vow: Escape > 30 blocks = Death
            if (dist > 30.0) {
                DamageSource vowBreak = new DamageSource(level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,
                                new ResourceLocation("domain_expansion", "binding_vow"))));

                // 1. Hurt with custom source for chat message
                victim.hurt(vowBreak, Float.MAX_VALUE);

                // 2. Force death if still alive (Creative/Totem protections)
                if (victim.isAlive()) {
                    if (victim instanceof LivingEntity living) {
                        living.setHealth(0);
                        living.die(vowBreak);
                    } else {
                        victim.kill();
                    }
                }

                toRemove.add(uuid);
            }
        }
        trappedEntities.removeAll(toRemove);
    }

    private void applyEffectsToTarget() {
        if (targetUUID == null)
            return;
        Entity target = ((net.minecraft.server.level.ServerLevel) level()).getEntity(targetUUID);
        if (target instanceof LivingEntity living) {
            if (living.distanceTo(this) <= getRadius()) {
                // Freeze Target: Slowness 255 (No movement), Jump 250 (No jump), Mining Fatigue
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 255, false, false));
                living.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 250, false, false));
                living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 5, false, false));
            }
        }
    }

    private boolean isTargetDeadOrMissing() {
        if (targetUUID == null)
            return true;
        Entity target = ((net.minecraft.server.level.ServerLevel) level()).getEntity(targetUUID);
        return target == null || !target.isAlive();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner"))
            ownerUUID = tag.getUUID("Owner");
        if (tag.hasUUID("Target"))
            targetUUID = tag.getUUID("Target");
        this.entityData.set(DATA_RADIUS, tag.getFloat("Radius"));
        this.entityData.set(STATE, tag.getInt("State"));
        this.age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null)
            tag.putUUID("Owner", ownerUUID);
        if (targetUUID != null)
            tag.putUUID("Target", targetUUID);
        tag.putFloat("Radius", getRadius());
        tag.putInt("State", getState());
        tag.putInt("Age", age);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
