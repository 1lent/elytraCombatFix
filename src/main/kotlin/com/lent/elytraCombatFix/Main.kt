package com.lent.elytraCombatFix

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        Bukkit.getPluginManager().registerEvents(EcfEvents(this), this)
    }

}
