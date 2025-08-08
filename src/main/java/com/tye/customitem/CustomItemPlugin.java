package com.tye.customitem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public class CustomItemPlugin extends JavaPlugin implements Listener {

    private NamespacedKey keyIsRubySword;
    private NamespacedKey keyRecipe;

    @Override
    public void onEnable() {
        keyIsRubySword = new NamespacedKey(this, "is_ruby_sword");
        keyRecipe = new NamespacedKey(this, "ruby_sword");
        registerRubySwordRecipe();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CustomItemPlugin enabled — Ruby Sword recipe registered.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomItemPlugin disabled.");
    }

    private ItemStack createRubySword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cRuby Sword");
            meta.setLore(List.of(
                    "§7A blade infused with the power of rubies.",
                    "§6Holding it grants immense strength.",
                    "§8Custom item by plugin"
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setCustomModelData(1001);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(keyIsRubySword, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRubySwordRecipe() {
        ItemStack result = createRubySword();
        ShapedRecipe recipe = new ShapedRecipe(keyRecipe, result);
        recipe.shape("RRR", "R R", "RSR");
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(recipe);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("givecustom")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }
        if (!sender.hasPermission("customitem.give")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }
        player.getInventory().addItem(createRubySword());
        player.sendMessage("§aGiven: §cRuby Sword");
        return true;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> checkSwordAndApplyEffect(player), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkSwordAndApplyEffect(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().removePotionEffect(PotionEffectType.STRENGTH);
    }

    private void checkSwordAndApplyEffect(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isRubySword(mainHand)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
    }

    private boolean isRubySword(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) return false;
        if (!item.hasItemMeta()) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(keyIsRubySword, PersistentDataType.BYTE);
        return val != null && val == (byte)1;
    }
}
