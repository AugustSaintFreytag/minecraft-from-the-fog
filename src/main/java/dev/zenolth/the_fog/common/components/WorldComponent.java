package dev.zenolth.the_fog.common.components;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorldComponent implements AutoSyncedComponent {

    // Configuration

    public static final String ENTITY_ID_NBT_KEY = "EntityId";
    public static final String ENTITY_HEALTH_NBT_KEY = "EntityHealth";
    public static final String TICK_LAST_KILLED_NBT_KEY = "TickLastKilled";
    public static final String NUMBER_OF_KILLS_NBT_KEY = "NumberOfKills";
    public static final String NUMBER_OF_SIGHTINGS_BY_PLAYER_NBT_KEY = "NumberOfSightingsByPlayer";

    // Properties

    private final World world;

    private long tickLastKilled = 0;
    
    private int numberOfKills = 0;
    
    @Nullable private Integer entityId;
    
    private float entityHealth = (float) TheManEntity.createAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH);
    
    private final Map<UUID, Integer> numberOfSightingsByPlayer = new HashMap<>();

    public WorldComponent(World world) {
        this.world = world;
    }

    public static WorldComponent get(World world) {
        return ModComponents.WORLD_COMPONENT.get(world);
    }

    public static void sync(World world) {
        if (world instanceof ServerWorld serverWorld) {
            ModComponents.WORLD_COMPONENT.sync(serverWorld);
        }
    }

    public static void syncWith(ServerPlayerEntity player) {
        ModComponents.WORLD_COMPONENT.syncWith(player,player.getServerWorld().asComponentProvider());
    }

    public long getTickLastKilled() { return this.tickLastKilled; }

    public void setTickLastKilled(long value) {
        this.tickLastKilled = value;
        sync(this.getWorld());
    }

    public int getNumberOfKills() { return this.numberOfKills; }

    public void setNumberOfKills(int value) {
        this.numberOfKills = value;
        sync(this.getWorld());
    }

    public Optional<Integer> getEntityId() {
        if (this.entityId == null) {
            return Optional.empty();
        }

        return Optional.of(this.entityId);
    }

    public void setEntityId(@Nullable Integer value) {
        this.entityId = value;
        sync(this.getWorld());
    }

    public float getEntityHealth() {
        return this.entityHealth;
    }

    public void setEntityHealth(float value) {
        this.entityHealth = value;
        sync(this.getWorld());
    }

    public Map<UUID, Integer> getNumberOfSightingsByPlayer() {
        return this.numberOfSightingsByPlayer;
    }

    public int getNumberOfSightingsForPlayer(UUID playerId) {
        return this.numberOfSightingsByPlayer.getOrDefault(playerId, 0);
    }

    public void NumberOfSightingsForPlayer(UUID playerId, int sightings) {
        if (sightings <= 0) {
            this.numberOfSightingsByPlayer.remove(playerId);
        } else {
            this.numberOfSightingsByPlayer.put(playerId, sightings);
        }

        sync(this.getWorld());
    }

    public void incrementSightingsForPlayer(UUID playerId) {
        this.NumberOfSightingsForPlayer(playerId, this.getNumberOfSightingsForPlayer(playerId) + 1);
    }

    public World getWorld() { 
        return this.world;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.tickLastKilled = tag.getLong(TICK_LAST_KILLED_NBT_KEY);
        this.numberOfKills = tag.getInt(NUMBER_OF_KILLS_NBT_KEY);

        if (tag.contains(ENTITY_ID_NBT_KEY, NbtElement.INT_TYPE)) {
            this.entityId = tag.getInt(ENTITY_ID_NBT_KEY);
        }
        
        this.entityHealth = tag.getFloat(ENTITY_HEALTH_NBT_KEY);
        this.numberOfSightingsByPlayer.clear();
        
        if (tag.contains(NUMBER_OF_SIGHTINGS_BY_PLAYER_NBT_KEY, NbtElement.LIST_TYPE)) {
            var list = tag.getList(NUMBER_OF_SIGHTINGS_BY_PLAYER_NBT_KEY, NbtElement.COMPOUND_TYPE);
            
            for (var i = 0; i < list.size(); i++) {
                var entry = list.getCompound(i);
                
                if (!entry.contains("Player", NbtElement.INT_ARRAY_TYPE)) {
                    continue;
                }

                var playerId = entry.getUuid("Player");
                var sightings = entry.getInt("Sightings");
                
                if (sightings > 0) {
                    this.numberOfSightingsByPlayer.put(playerId, sightings);
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong(TICK_LAST_KILLED_NBT_KEY,this.tickLastKilled);
        tag.putInt(NUMBER_OF_KILLS_NBT_KEY,this.numberOfKills);
        
        if (this.entityId != null) {
            tag.putInt(ENTITY_ID_NBT_KEY,this.entityId);
        } else {
            if (tag.contains(ENTITY_ID_NBT_KEY,NbtElement.INT_TYPE)) {
                tag.remove(ENTITY_ID_NBT_KEY);
            }
        }
        
        tag.putFloat(ENTITY_HEALTH_NBT_KEY,this.entityHealth);
        
        var list = new NbtList();
        
        for (var entry : this.numberOfSightingsByPlayer.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }

            var sighting = new NbtCompound();
            
            sighting.putUuid("Player", entry.getKey());
            sighting.putInt("Sightings", entry.getValue());
            
            list.add(sighting);
        }
        
        if (!list.isEmpty()) {
            tag.put(NUMBER_OF_SIGHTINGS_BY_PLAYER_NBT_KEY, list);
        } else if (tag.contains(NUMBER_OF_SIGHTINGS_BY_PLAYER_NBT_KEY, NbtElement.LIST_TYPE)) {
            tag.remove(NUMBER_OF_SIGHTINGS_BY_PLAYER_NBT_KEY);
        }
    }
}
