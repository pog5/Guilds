package me.pog5.guilds

import co.aikar.commands.PaperCommandManager
import me.pog5.guilds.commands.GuildCommand
import me.pog5.guilds.database.GDatabase
import me.pog5.guilds.managers.GuildManager
import me.pog5.guilds.util.Palette
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class Guilds : JavaPlugin() {
    val prefix: Component = Component.text("[GUILD] ", Palette.PURPLE)
    lateinit var database: GDatabase
    lateinit var guildManager: GuildManager
    private lateinit var commandManager: PaperCommandManager

    override fun onEnable() {
        database = GDatabase(this)
        guildManager = GuildManager(this)
        registerCommands()
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }

    private fun registerCommands() {
        commandManager = PaperCommandManager(this)
        commandManager.registerDependency(Guilds::class.java, this)
        commandManager.enableUnstableAPI("brigadier")
        commandManager.enableUnstableAPI("help")
        commandManager.commandCompletions.registerCompletion("players") { server.onlinePlayers.map { it.name } }
        commandManager.commandCompletions.registerCompletion("guilds") { guildManager.getGuilds() }
        commandManager.commandCompletions.registerCompletion("guildMembers") {
            val uuid = server.getPlayer(it.sender.name)?.uniqueId ?: return@registerCompletion emptyList()
            val guild = guildManager.getGuildFromPlayer(uuid) ?: return@registerCompletion emptyList()
            guild.members.keys.map { member -> server.getOfflinePlayer(member).name }
        }
        listOf(GuildCommand())
            .forEach(commandManager::registerCommand)
    }
}
