package me.pog5.guilds.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.pog5.guilds.Guilds
import me.pog5.guilds.util.Palette
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatEvent(private val plugin: Guilds) : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player

        val isGuildChatting = plugin.guildManager.guildChatters.contains(player.uniqueId)
        if (!isGuildChatting) return

        event.isCancelled = true

        val guild = plugin.guildManager.getGuildFromPlayer(player.uniqueId) ?: return
        val targets = mutableSetOf<Player>();
        guild.members.keys.forEach { plugin.server.getPlayer(it)?.let { it1 -> targets.add(it1) } }

        val builtMessage = Component.text()
            .append(Guilds().prefix)
            .append(Component.text("${player.name}: ", Palette.GRAY))
            .append(event.message().style(Style.style(Palette.FG)))

        targets.map { it.sendMessage(builtMessage) }
    }
}