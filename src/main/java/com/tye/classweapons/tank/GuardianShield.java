package com.tye.classweapons.tank;

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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class GuardianShield implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private final NamespacedKey keyIs;
    private final NamespacedKey keyRecipe;

    public GuardianShield(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIs = new NamespacedKey(plugin, "is_guardian_shield");
        this.keyRecipe = new NamespacedKey(plugin, "guardian_shield");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getCommand("giveguardian") != null) plugin.getCommand("giveguardian").setExecutor(this);
    }

    private ItemStack createItem() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§9Guardian Shield");
            meta.setLore(Arrays.asList("§7Bulwark of stone.", "§eGrants Resistance II while held."));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(keyIs, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void apply(Player p) {
        ItemStack main = p.getInventory().getItemInMainHand();
        boolean holding = isItem(main);
        if (holding) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
        } else {
            p.removePotionEffect(PotionEffectType.RESISTANCE);
            p.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    private boolean isItem(ItemStack s) {
        if (s == null || s.getType() != Material.SHIELD) return false;
        if (!s.hasItemMeta()) return false;
        return s.getItemMeta().getPersistentDataContainer().has(keyIs, PersistentDataType.BYTE);
        }

    private void registerRecipe() {
        ItemStack result = createItem();
        ShapedRecipe r = new ShapedRecipe(keyRecipe, result);
        r.shape("OIO", "OSO", " OO");
        r.setIngredient('O', Material.OBSIDIAN);
        r.setIngredient('I', Material.IRON_BLOCK);
        r.setIngredient('S', Material.SHIELD);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(r);
    }

    @EventHandler public void onHeld(PlayerItemHeldEvent e) { Bukkit.getScheduler().runTaskLater(plugin, () -> apply(e.getPlayer()), 1L); }
    @EventHandler public void onJoin(PlayerJoinEvent e) { apply(e.getPlayer()); }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        if (!sender.hasPermission("classweapons.giveguardian")) { sender.sendMessage("§cNo permission."); return true; }
        p.getInventory().addItem(createItem());
        sender.sendMessage("§aGiven: §9Guardian Shield");
        return true;
    }
}
