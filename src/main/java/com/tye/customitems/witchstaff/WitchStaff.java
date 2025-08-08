package com.tye.customitems.witchstaff;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class WitchStaff implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey keyIsWitchStaff;
    private final NamespacedKey keyRecipe;

    public WitchStaff(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIsWitchStaff = new NamespacedKey(plugin, "is_witch_staff");
        this.keyRecipe = new NamespacedKey(plugin, "witch_staff");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private ItemStack createWitchStaff() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Witch Staff");
            meta.setLore(List.of(
                    "§7A magical staff imbued with dark power.",
                    "§dRight-click to shoot a fireball.",
                    "§8Custom weapon by plugin"
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(keyIsWitchStaff, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        ItemStack result = createWitchStaff();
        ShapedRecipe recipe = new ShapedRecipe(keyRecipe, result);
        recipe.shape("BBB", "BSB", " S ");
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isWitchStaff(mainHand) && event.getAction().toString().contains("RIGHT_CLICK")) {
            player.launchProjectile(Fireball.class);
        }
    }

    private boolean isWitchStaff(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        if (!item.hasItemMeta()) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(keyIsWitchStaff, PersistentDataType.BYTE);
        return val != null && val == (byte)1;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("givewitchstaff")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }
        if (!sender.hasPermission("customweapons.givestaff")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }
        player.getInventory().addItem(createWitchStaff());
        player.sendMessage("§aGiven: §5Witch Staff");
        return true;
    }
}
