package com.zpedroo.slypspawners.managers;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.config.Settings;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

import java.math.BigInteger;
import java.util.Random;

public class EntityManager {

    public static void removeStack(Entity entity, BigInteger amount, PlayerSpawner playerSpawner) {
        String spawner = entity.getMetadata("Spawner").get(0).asString();
        BigInteger stack = new BigInteger(entity.getMetadata("MobAmount").get(0).asString());
        BigInteger newStack = stack.subtract(amount);
        if (newStack.signum() <= 0) return;

        Entity toRespawn = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        toRespawn.setMetadata("MobAmount", new FixedMetadataValue(SlypSpawners.get(), newStack.toString()));
        toRespawn.setMetadata("Spawner", new FixedMetadataValue(SlypSpawners.get(), spawner));
        toRespawn.setCustomName(StringUtils.replaceEach(playerSpawner.getSpawner().getEntityName(), new String[]{
                "{stack}"
        }, new String[]{
                NumberFormatter.getInstance().format(newStack)
        }));

        removeAI(toRespawn);
    }

    public static void spawn(PlayerSpawner spawner) {
        int r = Settings.STACK_RADIUS * 2;

        for (Entity near : spawner.getLocation().getWorld().getNearbyEntities(spawner.getLocation(), r, r, r)) {
            if (near == null || !near.getType().equals(spawner.getSpawner().getEntity())) continue;
            if (!near.hasMetadata("Spawner")) continue;

            final BigInteger stack = new BigInteger(near.getMetadata("MobAmount").get(0).asString());
            BigInteger newStack = stack.add(spawner.getStack());
            String serialized = SpawnerManager.getInstance().serializeLocation(spawner.getLocation());

            near.setMetadata("MobAmount", new FixedMetadataValue(SlypSpawners.get(), newStack.toString()));
            near.setMetadata("Spawner", new FixedMetadataValue(SlypSpawners.get(), serialized));

            near.setCustomName(StringUtils.replaceEach(spawner.getSpawner().getEntityName(), new String[]{
                    "{stack}"
            }, new String[]{
                    NumberFormatter.getInstance().format(newStack)
            }));
            spawner.addEntity(near);
            return;
        }

        int tryLimit = 20;

        int minRange = 1;
        int maxRange = r / 2;

        Random random = new Random();
        double x = spawner.getLocation().getX() + random.nextDouble() * (maxRange - minRange) + 0.5D;
        double y = spawner.getLocation().getY() + 5D; // fix spawn bugs
        double z = spawner.getLocation().getZ() + random.nextDouble() * (maxRange - minRange) + 0.5D;

        Location location = new Location(spawner.getLocation().getWorld(), x, y, z);

        while (!canSpawn(location)) {
            if (--tryLimit <= 0) return;

            location.setY(location.getY() - 1);
        }

        Entity entity = spawner.getLocation().getWorld().spawnEntity(location, spawner.getSpawner().getEntity());
        entity.setMetadata("MobAmount", new FixedMetadataValue(SlypSpawners.get(), spawner.getStack().toString()));
        entity.setMetadata("Spawner", new FixedMetadataValue(SlypSpawners.get(), SpawnerManager.getInstance().serializeLocation(spawner.getLocation())));
        entity.setCustomName(StringUtils.replaceEach(spawner.getSpawner().getEntityName(), new String[]{
                "{stack}"
        }, new String[]{
                NumberFormatter.getInstance().format(spawner.getStack())
        }));

        removeAI(entity);
        spawner.addEntity(entity);
    }

    private static void removeAI(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null) tag = new NBTTagCompound();

        nmsEntity.c(tag);
        tag.setInt("NoAI", 1);
        nmsEntity.f(tag);
    }

    private static Boolean canSpawn(Location location) {
        Block block = location.getBlock().getRelative(BlockFace.DOWN);

        return !block.getType().equals(Material.AIR) && !block.getType().toString().contains("SLAB");
    }
}