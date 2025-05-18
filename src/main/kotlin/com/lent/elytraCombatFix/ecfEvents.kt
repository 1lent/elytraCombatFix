package com.lent.elytraCombatFix

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.HashMap
import java.util.UUID

class ecfEvents(private val plugin: Main) : Listener {
    private val combatTagged = HashMap<UUID, Long>()

    @EventHandler
    fun onHit(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        val uuid = player.uniqueId
        combatTagged.put(uuid, System.currentTimeMillis())
        if (player.isGliding) player.isGliding = false
        val chest = player.inventory.chestplate
        if (chest != null && chest.type == Material.ELYTRA) {
            player.inventory.chestplate = null
            player.inventory.addItem(chest)
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        if (!isInCombat(player.uniqueId)) return
        val shiftClickItem = e.currentItem
        if (e.click.isShiftClick && shiftClickItem != null && shiftClickItem.type == Material.ELYTRA) {
            val chest = player.inventory.chestplate
            if (chest == null || chest.type == Material.AIR) {
                e.isCancelled = true
                player.sendMessage("[${ChatColor.RED}!${ChatColor.GRAY}] You can't equip Elytra during combat.")
                return
            }
        }
        if (e.slot == 38 && e.cursor.type == Material.ELYTRA) {
            e.isCancelled = true
            player.sendMessage("[${ChatColor.RED}!${ChatColor.GRAY}] You can't equip Elytra during combat.")
            return
        }
        if (e.click.isKeyboardClick && e.hotbarButton >= 0) {
            val hotbarItem = player.inventory.getItem(e.hotbarButton)
            if (e.slot == 38 && hotbarItem != null && hotbarItem.type == Material.ELYTRA) {
                e.isCancelled = true
                player.sendMessage("[${ChatColor.RED}!${ChatColor.GRAY}] You can't equip Elytra during combat.")
            }
        }
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        val player = e.whoClicked as? Player ?: return
        if (!isInCombat(player.uniqueId)) return
        if (e.rawSlots.contains(38) && e.oldCursor?.type == Material.ELYTRA) {
            e.isCancelled = true
            player.sendMessage("[${ChatColor.RED}!${ChatColor.GRAY}] You can't equip Elytra during combat.")
        }
    }

    @EventHandler
    fun onToggleGlide(e: EntityToggleGlideEvent) {
        val player = e.entity as? Player ?: return
        if (isInCombat(player.uniqueId) && e.isGliding) {
            e.isCancelled = true
            player.sendMessage("[${ChatColor.RED}!${ChatColor.GRAY}] You can't fly during combat.")
        }
    }

    private fun isInCombat(uuid: UUID): Boolean {
        val last = combatTagged.get(uuid) ?: return false
        val seconds = plugin.config.getInt("combat-timer")
        return (System.currentTimeMillis() - last < seconds * 1000L)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        combatTagged.remove(e.player.uniqueId)
    }
}