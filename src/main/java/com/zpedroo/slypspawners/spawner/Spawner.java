package com.zpedroo.slypspawners.spawner;

import com.zpedroo.slypspawners.spawner.drop.Drop;
import com.zpedroo.slypspawners.spawner.head.Head;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Spawner {

    private EntityType entity;
    private String entityName;
    private ItemStack item;
    private Material block;
    private String type;
    private Long delay;
    private BigInteger maxStack;
    private String permission;
    private String permissionMessage;
    private String[] hologram;
    private List<Drop> drops;
    private Head head;

    public Spawner(EntityType entity, String entityName, ItemStack item, Material block, String type, Long delay, BigInteger maxStack, String permission, String permissionMessage, String[] hologram, List<Drop> drops, Head head) {
        this.entity = entity;
        this.entityName = entityName;
        this.item = item;
        this.block = block;
        this.type = type;
        this.delay = delay;
        this.maxStack = maxStack;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.hologram = hologram;
        this.drops = drops;
        this.head = head;
    }

    public EntityType getEntity() {
        return entity;
    }

    public String getEntityName() {
        return entityName;
    }

    public ItemStack getItem() {
        return item;
    }

    public Material getBlock() {
        return block;
    }

    public ItemStack getDisplayItem() {
        return item.clone();
    }

    public String getType() {
        return type;
    }

    public Long getDelay() {
        return delay;
    }

    public BigInteger getMaxStack() {
        return maxStack;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public String[] getHologram() {
        return hologram;
    }

    public List<Drop> getDrops() {
        return drops;
    }

    public Head getHead() {
        return head;
    }

    public ItemStack getItem(BigInteger amount) {
        NBTItem nbt = new NBTItem(getItem().clone());
        nbt.setString("SpawnersAmount", amount.toString());
        nbt.setString("SpawnersType", getType());

        ItemStack item = nbt.getItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;

            if (name != null) meta.setDisplayName(StringUtils.replaceEach(name, new String[] {
                    "{amount}"
            }, new String[] {
                    NumberFormatter.getInstance().format(amount)
            }));

            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());

                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, new String[] {
                            "{amount}"
                    }, new String[] {
                            NumberFormatter.getInstance().format(amount)
                    }));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}