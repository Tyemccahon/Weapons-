
package com.tye.classweapons.berserker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class Bloodaxe implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private final NamespacedKey keyIs;
    private final NamespacedKey keyRecipe;

    public Bloodaxe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIs = new NamespacedKey(plugin, "is_bloodaxe");
        this.keyRecipe = new NamespacedKey(plugin, "bloodaxe");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getCommand("givebloodaxe") != null) plugin.getCommand("givebloodaxe").setExecutor(this);
    }

    private ItemStack createItem() {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§4Bloodaxe");
            meta.setLore(Arrays.asList("§7Thirsty for battle.", "§eHolding: Strength II + Mining Fatigue I"));
            meta.setCustomModelData(2003);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(keyIs, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void apply(Player p) {
        boolean holding = isItem(p.getInventory().getItemInMainHand());
        if (holding) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 0, true, false));
        } else {
            p.removePotionEffect(PotionEffectType.STRENGTH);
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }
    }

    private boolean isItem(ItemStack s) {
        if (s == null || s.getType() != Material.NETHERITE_AXE) return false;
        if (!s.hasItemMeta()) return false;
        return s.getItemMeta().getPersistentDataContainer().has(keyIs, PersistentDataType.BYTE);
    }

    private void registerRecipe() {
        ItemStack result = createItem();
        ShapedRecipe r = new ShapedRecipe(keyRecipe, result);
        r.shape("BBB", "BNB", " S ");
        r.setIngredient('B', Material.REDSTONE_BLOCK);
        r.setIngredient('N', Material.NETHERITE_AXE);
        r.setIngredient('S', Material.STICK);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(r);
    }

    @EventHandler public void onHeld(PlayerItemHeldEvent e) { Bukkit.getScheduler().runTaskLater(plugin, () -> apply(e.getPlayer()), 1L); }
    @EventHandler public void onJoin(PlayerJoinEvent e) { apply(e.getPlayer()); }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        if (!sender.hasPermission("classweapons.givebloodaxe")) { sender.sendMessage("§cNo permission."); return true; }
        p.getInventory().addItem(createItem());
        sender.sendMessage("§aGiven: §4Bloodaxe");
        return true;
    }
}
