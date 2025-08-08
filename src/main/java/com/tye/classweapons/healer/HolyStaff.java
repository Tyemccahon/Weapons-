
package com.tye.classweapons.healer;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class HolyStaff implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private final NamespacedKey keyIs;
    private final NamespacedKey keyRecipe;
    private final Map<java.util.UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TICKS = 200; // 10s

    public HolyStaff(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIs = new NamespacedKey(plugin, "is_holy_staff");
        this.keyRecipe = new NamespacedKey(plugin, "holy_staff");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getCommand("giveholystaff") != null) plugin.getCommand("giveholystaff").setExecutor(this);
    }

    private ItemStack createItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eHoly Staff");
            meta.setLore(java.util.Arrays.asList("§7Beacon of life.", "§eRight-click: AoE heal (10s CD)"));
            meta.setCustomModelData(2005);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(keyIs, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isItem(ItemStack s) {
        if (s == null || s.getType() != Material.BLAZE_ROD) return false;
        if (!s.hasItemMeta()) return false;
        return s.getItemMeta().getPersistentDataContainer().has(keyIs, PersistentDataType.BYTE);
    }

    private void registerRecipe() {
        ItemStack result = createItem();
        ShapedRecipe r = new ShapedRecipe(keyRecipe, result);
        r.shape("GGG", "GSG", " S ");
        r.setIngredient('G', Material.GOLD_INGOT);
        r.setIngredient('S', Material.STICK);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(r);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!e.getAction().toString().contains("RIGHT_CLICK")) return;
        Player p = e.getPlayer();
        if (!isItem(p.getInventory().getItemInMainHand())) return;

        long now = System.currentTimeMillis();
        long readyAt = cooldowns.getOrDefault(p.getUniqueId(), 0L);
        if (now < readyAt) {
            p.sendMessage("§cHoly Staff recharging...");
            e.setCancelled(true);
            return;
        }

        double radius = 6.0;
        for (Entity ent : p.getNearbyEntities(radius, radius, radius)) {
            if (ent instanceof LivingEntity le) {
                if (le instanceof Player) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, true));
                }
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, true));
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0,1,0), 30, 1, 0.5, 1, 0.1);

        cooldowns.put(p.getUniqueId(), now + COOLDOWN_TICKS * 50L);
        e.setCancelled(true);
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        if (!sender.hasPermission("classweapons.giveholystaff")) { sender.sendMessage("§cNo permission."); return true; }
        p.getInventory().addItem(createItem());
        sender.sendMessage("§aGiven: §eHoly Staff");
        return true;
    }
}
