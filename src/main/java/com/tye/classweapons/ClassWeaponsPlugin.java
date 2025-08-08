package com.tye.classweapons;

import org.bukkit.plugin.java.JavaPlugin;
import com.tye.classweapons.mage.ArcaneStaff;
import com.tye.classweapons.tank.AegisMace;
import com.tye.classweapons.berserker.Bloodaxe;
import com.tye.classweapons.archer.WindBow;
import com.tye.classweapons.healer.HolyStaff;

public class ClassWeaponsPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new ArcaneStaff(this);
        new AegisMace(this);
        new Bloodaxe(this);
        new WindBow(this);
        new HolyStaff(this);
        getLogger().info("ClassWeaponsPlugin v1.2 enabled.");
    }
}
