package dev.zenolth.the_fog.common.util;

import org.joml.Vector3f;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.config.ModConfig;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TheManUtils {

    /**
     * @param serverWorld The World to check
     * @return If any TheManEntityHallucination exist in serverWorld
     */
    public static boolean hallucinationsExists(ServerWorld serverWorld) {
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN_HALLUCINATION, TheManPredicates.THE_MAN_HALLUCINATION_ENTITY_PREDICATE).isEmpty();
    }

    /**
     * @param world The World to check
     * @return If any TheManEntity exist in serverWorld
     */
    public static boolean manExists(ServerWorld world) {
        if (FogMod.getTheMan(world) != null) return true;
        return !world.getEntitiesByType(Util.LIVING_ENTITY_TYPE_FILTER, TheManPredicates.THE_MAN_ENTITY_PREDICATE).isEmpty();
    }

    public static void spawnLightning(ServerWorld world, double x, double y, double z) {
        if (!FogMod.CONFIG.miscellaneous.summonCosmeticLightning) return;

        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightningEntity.setCosmetic(true);
        lightningEntity.setInvulnerable(true);
        lightningEntity.setOnFire(false);
        lightningEntity.setPosition(x, y, z);
        world.spawnEntity(lightningEntity);
    }

    public static void spawnLightning(ServerWorld world, Vec3d position) {
        spawnLightning(world,position.getX(),position.getY(),position.getZ());
    }

    public static void spawnLightning(ServerWorld world, Entity entity) {
        spawnLightning(world,entity.getX(),entity.getY(),entity.getZ());
    }

    public static void spawnDustCloud(ServerWorld world, Vec3d pos) {
        var random = world.getRandom();
		var cloudCount = random.nextBetween(12, 18);

		var offsetX = (random.nextDouble() - 0.5) * 1.5;
        var offsetY = (random.nextDouble() - 0.5) * 1.0;
        var offsetZ = (random.nextDouble() - 0.5) * 1.5;

        world.spawnParticles(ParticleTypes.ASH, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, cloudCount, offsetX, offsetY, offsetZ, 1.0f);
    }
}
