
package com.tye.classweapons.mage;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
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
import org.bukkit.util.Vector;

import java.util.*;

public class ArcaneStaff implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey keyIs;
    private final NamespacedKey keyRecipe;
    private final NamespacedKey keyMode;
    private final NamespacedKey keyFrostProj;
    private final Map<java.util.UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TICKS = 100; // 5s

    public ArcaneStaff(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keyIs = new NamespacedKey(plugin, "is_arcane_staff");
        this.keyRecipe = new NamespacedKey(plugin, "arcane_staff");
        this.keyMode = new NamespacedKey(plugin, "arcane_mode");
        this.keyFrostProj = new NamespacedKey(plugin, "arcane_frost_proj");
        registerRecipe();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getCommand("givearcane") != null) plugin.getCommand("givearcane").setExecutor(this);
    }

    private ItemStack createItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§dArcane Staff");
            meta.setLore(java.util.Arrays.asList(
                "§7Three spells: Firebolt, Frost Orb, Chain Lightning",
                "§bSneak + Right-Click: switch spell",
                "§dRight-Click: cast (5s CD)"
            ));
            meta.setCustomModelData(2001);
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
        r.shape("EEE", "ESE", " S ");
        r.setIngredient('E', Material.ENDER_PEARL);
        r.setIngredient('S', Material.STICK);
        Bukkit.removeRecipe(keyRecipe);
        Bukkit.addRecipe(r);
    }

    private int getMode(Player p) {
        Integer m = p.getPersistentDataContainer().get(keyMode, PersistentDataType.INTEGER);
        return m == null ? 0 : m;
    }
    private void setMode(Player p, int m) {
        p.getPersistentDataContainer().set(keyMode, PersistentDataType.INTEGER, m % 3);
    }

    private boolean isItem(ItemStack s) {
        if (s == null || s.getType() != Material.BLAZE_ROD) return false;
        if (!s.hasItemMeta()) return false;
        return s.getItemMeta().getPersistentDataContainer().has(keyIs, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!e.getAction().toString().contains("RIGHT_CLICK")) return;
        Player p = e.getPlayer();
        if (!isItem(p.getInventory().getItemInMainHand())) return;

        if (p.isSneaking()) {
            int nm = (getMode(p) + 1) % 3;
            setMode(p, nm);
            String name = (nm==0?"§cFirebolt":nm==1?"§bFrost Orb":"§5Chain Lightning");
            p.sendActionBar("§dSpell: "+name);
            e.setCancelled(true);
            return;
        }

        long now = System.currentTimeMillis();
        long readyAt = cooldowns.getOrDefault(p.getUniqueId(), 0L);
        if (now < readyAt) {
            double secs = (readyAt - now)/1000.0;
            p.sendMessage(String.format("§cRecharging... %.1fs", secs));
            e.setCancelled(true);
            return;
        }

        switch (getMode(p)) {
            case 0 -> castFirebolt(p);
            case 1 -> castFrostOrb(p);
            case 2 -> castChainLightning(p);
        }
        cooldowns.put(p.getUniqueId(), now + COOLDOWN_TICKS * 50L);
        e.setCancelled(true);
    }

    private void castFirebolt(Player p) {
        SmallFireball fb = p.launchProjectile(SmallFireball.class);
        fb.setIsIncendiary(true);
        fb.setYield(0f);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1f, 1.2f);
        p.spawnParticle(Particle.FLAME, p.getEyeLocation(), 20, 0,0,0, 0.05);
    }

    private void castFrostOrb(Player p) {
        Snowball sb = p.launchProjectile(Snowball.class);
        // mark the projectile so we know it's ours
        sb.getPersistentDataContainer().set(keyFrostProj, PersistentDataType.BYTE, (byte)1);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SNOW_PLACE, 1f, 1.1f);
        p.spawnParticle(Particle.SNOWFLAKE, p.getEyeLocation(), 10, 0,0,0, 0.01);
    }

    // Robust chain lightning: ray-march to find first target, then jump to up to 3 nearby
    private void castChainLightning(Player p) {
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        World w = p.getWorld();

        LivingEntity first = findFirstTargetAlongRay(p, eye, dir, 25.0, 0.8);
        if (first == null) {
            w.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
            return;
        }

        java.util.List<LivingEntity> chain = new java.util.ArrayList<>();
        chain.add(first);
        LivingEntity current = first;
        for (int i=0; i<3; i++) {
            LivingEntity next = findNearestTarget(current, p, 5.0, chain);
            if (next == null) break;
            chain.add(next);
            current = next;
        }

        Location prev = eye.clone();
        for (LivingEntity le : chain) {
            drawBeam(prev, le.getLocation().add(0, le.getHeight()*0.5, 0));
            le.damage(4.0, p);
            le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0, true, true));
            prev = le.getLocation();
        }
        w.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.4f);
    }

    private LivingEntity findFirstTargetAlongRay(Player shooter, Location start, Vector dir, double maxDist, double radius) {
        World w = start.getWorld();
        double step = 0.5;
        for (double d=0; d<=maxDist; d += step) {
            Location pos = start.clone().add(dir.clone().multiply(d));
            for (LivingEntity le : pos.getNearbyLivingEntities(radius)) {
                if (le.getUniqueId().equals(shooter.getUniqueId())) continue;
                return le;
            }
            w.spawnParticle(Particle.ELECTRIC_SPARK, pos, 1, 0.02,0.02,0.02, 0);
        }
        return null;
    }

    private LivingEntity findNearestTarget(LivingEntity from, Player shooter, double radius, java.util.List<LivingEntity> exclude) {
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (Entity e : from.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !le.getUniqueId().equals(shooter.getUniqueId()) && !exclude.contains(le)) {
                double d = le.getLocation().distanceSquared(from.getLocation());
                if (d < bestD) { bestD = d; best = le; }
            }
        }
        return best;
    }

    private void drawBeam(Location a, Location b) {
        World w = a.getWorld();
        Vector diff = b.toVector().subtract(a.toVector());
        int steps = Math.max(8, (int)(diff.length()*4));
        Vector step = diff.multiply(1.0/steps);
        Location cur = a.clone();
        for (int i=0; i<steps; i++) {
            cur.add(step);
            w.spawnParticle(Particle.ELECTRIC_SPARK, cur, 3, 0.02,0.02,0.02, 0);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball sb)) return;
        PersistentDataContainer pdc = sb.getPersistentDataContainer();
        if (!pdc.has(keyFrostProj, PersistentDataType.BYTE)) return;

        Location loc = sb.getLocation();
        if (e.getHitEntity() != null) loc = e.getHitEntity().getLocation();
        else if (e.getHitBlock() != null) loc = e.getHitBlock().getLocation().add(0.5, 1, 0.5);

        AreaEffectCloud cloud = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(3f);
        cloud.setDuration(100); // 5s
        cloud.setColor(Color.AQUA);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, true, true), true);
        cloud.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 40, 0.6, 0.4, 0.6, 0.01);
        cloud.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.7f);
        sb.remove();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        if (!sender.hasPermission("classweapons.givearcane")) { sender.sendMessage("§cNo permission."); return true; }
        p.getInventory().addItem(createItem());
        sender.sendMessage("§aGiven: §dArcane Staff");
        return true;
    }
}
