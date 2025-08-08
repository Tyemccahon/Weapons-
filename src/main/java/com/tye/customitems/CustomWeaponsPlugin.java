package com.tye.customitems;

import org.bukkit.plugin.java.JavaPlugin;
import com.tye.customitems.infernoaxe.InfernoAxe;
import com.tye.customitems.witchstaff.WitchStaff;

public class CustomWeaponsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new InfernoAxe(this);
        new WitchStaff(this);
        getLogger().info("CustomWeaponsPlugin enabled — Inferno Axe and Witch Staff registered.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomWeaponsPlugin disabled.");
    }
}
