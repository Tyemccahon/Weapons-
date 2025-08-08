package com.tye.customitems.infernoaxe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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

public class InfernoAxe implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey keyIsInfernoAxe;
    private final NamespacedKey keyRecipe;

    public InfernoAxe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIsInfernoAxe = new NamespacedKey(plugin, "is_inferno_axe");
        this.keyRecipe = new NamespacedKey(plugin, "inferno_axe");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private ItemStack createInfernoAxe() {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cInferno Axe");
            meta.setLore(List.of(
                    "§7Forged in the heart of a volcano.",
                    "§6Holding it grants immense strength.",
                    "§8Custom weapon by plugin"
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(keyIsInfernoAxe, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        ItemStack result = createInfernoAxe();
        ShapedRecipe recipe = new ShapedRecipe(keyRecipe, result);
        recipe.shape("LLL", "LNL", " S ");
        recipe.setIngredient('L', Material.LAVA_BUCKET);
        recipe.setIngredient('N', Material.NETHERITE_AXE);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkAndApply(player), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkAndApply(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().removePotionEffect(PotionEffectType.STRENGTH);
    }

    private void checkAndApply(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isInfernoAxe(mainHand)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
    }

    private boolean isInfernoAxe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE) return false;
        if (!item.hasItemMeta()) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(keyIsInfernoAxe, PersistentDataType.BYTE);
        return val != null && val == (byte)1;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("giveinfernoaxe")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }
        if (!sender.hasPermission("customweapons.giveaxe")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }
        player.getInventory().addItem(createInfernoAxe());
        player.sendMessage("§aGiven: §cInferno Axe");
        return true;
    }
}
