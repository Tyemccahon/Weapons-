
package com.tye.classweapons.archer;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.UUID;

public class WindBow implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private final NamespacedKey keyIs;
    private final NamespacedKey keyRecipe;
    private final NamespacedKey keyMode; // 0 = straight, 1 = homing

    public WindBow(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIs = new NamespacedKey(plugin, "is_wind_bow");
        this.keyRecipe = new NamespacedKey(plugin, "wind_bow");
        this.keyMode = new NamespacedKey(plugin, "wind_bow_mode");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getCommand("givewindbow") != null) plugin.getCommand("givewindbow").setExecutor(this);
    }

    private ItemStack createItem() {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aWind Bow");
            meta.setLore(Arrays.asList("§7Right-click while sneaking: toggle mode", "§eShooting grants Speed II"));
            meta.setCustomModelData(2004);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(keyIs, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        ItemStack result = createItem();
        ShapedRecipe r = new ShapedRecipe(keyRecipe, result);
        r.shape("FSF", " B ", "FSF");
        r.setIngredient('F', Material.FEATHER);
        r.setIngredient('S', Material.STICK);
        r.setIngredient('B', Material.BOW);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(r);
    }

    private boolean isItem(ItemStack s) {
        if (s == null || s.getType() != Material.BOW) return false;
        if (!s.hasItemMeta()) return false;
        return s.getItemMeta().getPersistentDataContainer().has(keyIs, PersistentDataType.BYTE);
    }

    private int getMode(Player p) {
        Integer m = p.getPersistentDataContainer().get(keyMode, PersistentDataType.INTEGER);
        return m == null ? 0 : m;
    }
    private void setMode(Player p, int m) {
        p.getPersistentDataContainer().set(keyMode, PersistentDataType.INTEGER, m % 2);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!e.getAction().toString().contains("RIGHT_CLICK")) return;
        Player p = e.getPlayer();
        if (!isItem(p.getInventory().getItemInMainHand())) return;
        if (!p.isSneaking()) return;

        int newMode = (getMode(p) + 1) % 2;
        setMode(p, newMode);
        p.sendActionBar(newMode == 0 ? "§aWind Bow: §fStraight" : "§aWind Bow: §bHoming");
        e.setCancelled(true);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!isItem(e.getBow())) return;

        // grant speed
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1f, 1.4f);

        if (!(e.getProjectile() instanceof Arrow arrow)) return;
        if (getMode(p) == 0) return; // straight

        // mark and start homing
        UUID shooterId = p.getUniqueId();
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!arrow.isValid() || arrow.isOnGround() || arrow.isInBlock()) { cancel(); return; }
                if (ticks++ > 200) { cancel(); return; } // max 10s
                LivingEntity target = findTarget(arrow, shooterId, 16.0);
                if (target != null) {
                    Vector to = target.getLocation().add(0, target.getHeight()*0.5, 0).toVector().subtract(arrow.getLocation().toVector()).normalize();
                    Vector newVel = arrow.getVelocity().multiply(0.9).add(to.multiply(0.6));
                    if (newVel.length() < 0.5) newVel.normalize().multiply(0.5);
                    arrow.setVelocity(newVel);
                    arrow.getWorld().spawnParticle(Particle.CLOUD, arrow.getLocation(), 2, 0.01,0.01,0.01, 0.0);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private LivingEntity findTarget(Arrow arrow, UUID shooterId, double radius) {
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        Location aloc = arrow.getLocation();
        Vector dir = arrow.getVelocity().clone().normalize();
        for (Entity e : aloc.getWorld().getNearbyEntities(aloc, radius, radius, radius)) {
            if (e instanceof LivingEntity le) {
                if (le.getUniqueId().equals(shooterId)) continue;
                // prefer targets roughly in front of the arrow
                Vector to = le.getLocation().add(0, le.getHeight()*0.5, 0).toVector().subtract(aloc.toVector());
                double angle = dir.angle(to);
                if (angle > Math.toRadians(100)) continue;
                double dist = to.length();
                double score = dist + angle * 10; // cheap heuristic
                if (score < bestScore) {
                    bestScore = score;
                    best = le;
                }
            }
        }
        return best;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        if (!sender.hasPermission("classweapons.givewindbow")) { sender.sendMessage("§cNo permission."); return true; }
        p.getInventory().addItem(createItem());
        sender.sendMessage("§aGiven: §aWind Bow");
        return true;
    }
}
