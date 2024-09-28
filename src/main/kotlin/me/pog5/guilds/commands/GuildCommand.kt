package me.pog5.guilds.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import me.pog5.guilds.Guilds
import me.pog5.guilds.data.GuildRole
import me.pog5.guilds.util.Palette
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

@CommandAlias("clan|guild|g")
class GuildCommand : BaseCommand() {
    @Dependency
    private lateinit var plugin: Guilds

    fun permCheck(sender: CommandSender, level: GuildRole): Boolean {
        val uuid = plugin.server.getPlayer(sender.name)?.uniqueId ?: return false
        val guild = plugin.guildManager.getGuildFromPlayer(uuid) ?: run {
            sender.sendMessage(Component.text("You are not in a guild!", Palette.STATUSLINE3))
            return false
        }
        if (guild.members[uuid] != level && guild.members[uuid] != GuildRole.LEADER) {
            sender.sendMessage(Component.text("You need to be a $level or higher to do this!", Palette.STATUSLINE3))
            return false
        }
        return true
    }

    @HelpCommand
    fun GuildHelpCommand(sender: CommandSender, help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("create")
    fun GuildCreateCommand(sender: CommandSender, @Single name: String, @Single tag: String) {
        val uuid = plugin.server.getPlayer(sender.name)?.uniqueId ?: return
        plugin.guildManager.createGuild(name, tag, uuid)
        sender.sendMessage(Component.text("Guild $name created!", Palette.STATUSLINE1))
    }

    @Subcommand("disband")
    fun GuildDisbandCommand(sender: CommandSender, @Optional @Single name: String) {
        if (!permCheck(sender, GuildRole.LEADER)) return
        val guild = plugin.guildManager.getGuild(name) ?: return

        if (name != guild.name) {
            sender.sendMessage(
                Component.text(
                    "Please run the command with the guild name as a argument to confirm this!",
                    Palette.STATUSLINE3,
                    TextDecoration.BOLD
                )
            )
            return
        }

        plugin.guildManager.deleteGuild(name)
    }

    @Subcommand("invite")
    @CommandCompletion("@players")
    fun GuildInviteCommand(sender: CommandSender, @Single name: String, @Single player: String) {
        if (!permCheck(sender, GuildRole.OFFICER)) return
        val guild = plugin.guildManager.getGuild(name) ?: return
        val target = plugin.server.getPlayer(player) ?: run {
            sender.sendMessage(Component.text("Player $player not found!", Palette.STATUSLINE3))
            return
        }

        guild.invitePlayer(target.uniqueId)
        sender.sendMessage(Component.text("Invited $player to $name!", Palette.STATUSLINE1))
    }

    @Subcommand("join")
    fun GuildJoinCommand(sender: CommandSender, @Single name: String) {
        val uuid = plugin.server.getPlayer(sender.name)?.uniqueId ?: return
        val guild = plugin.guildManager.getGuild(name) ?: run {
            sender.sendMessage(Component.text("Guild $name not found!", Palette.STATUSLINE3))
            return
        }

        guild.join(uuid)
    }

    @Subcommand("leave")
    fun GuildLeaveCommand(sender: CommandSender, @Optional @Single name: String?) {
        val uuid = plugin.server.getPlayer(sender.name)?.uniqueId ?: return
        if (!permCheck(sender, GuildRole.MEMBER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(uuid) ?: return

        if (name != guild.name) {
            sender.sendMessage(
                Component.text(
                    "Please run the command with the guild name as a argument to confirm this!", Palette.STATUSLINE3
                )
            )
            return
        }

        guild.removeMember(uuid)
    }

    @Subcommand("kick")
    @CommandCompletion("@guildMembers")
    fun GuildKickCommand(sender: CommandSender, @Single player: String) {
        if (!permCheck(sender, GuildRole.OFFICER)) return
        val guild = plugin.guildManager.getGuild(name) ?: return
        val target = plugin.server.getOfflinePlayer(player)

        guild.kick(target.uniqueId, plugin.server.getPlayer(sender.name)?.uniqueId)
    }

    @Subcommand("list")
    fun GuildListCommand(sender: CommandSender) {
        val guilds = plugin.guildManager.guilds
        val onlineGuilds = guilds.filter { guild ->
            guild.members.keys.any { member ->
                plugin.server.getPlayer(member)?.isOnline == true
            }
        }

        val builtMessage = Component.text("Guilds: ", Palette.PURPLE).appendNewline()

        onlineGuilds.forEach { guild ->
            builtMessage.append(Component.text(guild.name, guild.color))
                .append(Component.text(" - ${guild.onlineMembers.size}/${guild.members.size}", Palette.STATUSLINE2))
                .appendNewline()
        }
    }

    @Subcommand("setname")
    fun GuildSetNameCommand(sender: CommandSender, @Single name: String) {
        if (!permCheck(sender, GuildRole.LEADER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(plugin.server.getPlayer(sender.name)?.uniqueId!!) ?: return

        guild.name = name
        sender.sendMessage(Component.text("Guild name set to $name!", Palette.STATUSLINE1))
    }

    @Subcommand("settag")
    fun GuildSetTagCommand(sender: CommandSender, @Single tag: String) {
        if (!permCheck(sender, GuildRole.LEADER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(plugin.server.getPlayer(sender.name)?.uniqueId!!) ?: return

        guild.tag = tag
        sender.sendMessage(Component.text("Guild tag set to $tag!", Palette.STATUSLINE1))
    }

    @Subcommand("setcolor")
    fun GuildSetColorCommand(sender: CommandSender, @Single color: String) {
        if (!permCheck(sender, GuildRole.LEADER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(plugin.server.getPlayer(sender.name)?.uniqueId!!) ?: return

        guild.color = Palette.readColor(color)
        sender.sendMessage(Component.text("Guild color set to $color!", Palette.STATUSLINE1))
    }

    @Subcommand("chat")
    fun GuildChatCommand(sender: CommandSender, @Optional message: String?) {
        val uuid = plugin.server.getPlayer(sender.name)?.uniqueId ?: return
        if (!permCheck(sender, GuildRole.MEMBER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(uuid) ?: return

        if (message == null) {
            if (plugin.guildManager.guildChatters.contains(uuid)) {
                plugin.guildManager.guildChatters.remove(uuid)
                sender.sendMessage(Component.text("Guild chat disabled!", Palette.STATUSLINE3))
            } else {
                plugin.guildManager.guildChatters.add(uuid)
                sender.sendMessage(Component.text("Guild chat enabled!", Palette.STATUSLINE1))
            }
            return
        }

        guild.chat(uuid, message)
    }

    @Subcommand("promote")
    @CommandCompletion("@guildMembers")
    fun GuildPromoteCommand(sender: CommandSender, @Single player: String) {
        if (!permCheck(sender, GuildRole.LEADER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(plugin.server.getPlayer(sender.name)?.uniqueId!!) ?: return
        val target = plugin.server.getOfflinePlayer(player)

        if (guild.members[target.uniqueId] == GuildRole.OFFICER && guild.pendingTransfer != target.uniqueId) {
            sender.sendMessage(
                Component.text(
                    "That player is already a officer! Run this command again within 10 seconds to transfer ownership.",
                    Palette.STATUSLINE2
                )
            )

            guild.pendingTransfer = target.uniqueId
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                guild.pendingTransfer = null
            }, 10 * 20)

            return
        }

        guild.promoteMember(target.uniqueId)
    }

    @Subcommand("demote")
    @CommandCompletion("@guildMembers")
    fun GuildDemoteCommand(sender: CommandSender, @Single player: String) {
        if (!permCheck(sender, GuildRole.LEADER)) return
        val guild = plugin.guildManager.getGuildFromPlayer(plugin.server.getPlayer(sender.name)?.uniqueId!!) ?: return
        val target = plugin.server.getOfflinePlayer(player)

        if (target.name == sender.name) {
            sender.sendMessage(
                Component.text(
                    "If you want to demote yourself, promote someone else to leader.", Palette.STATUSLINE2
                )
            )
            return
        }

        guild.demoteMember(target.uniqueId)
    }

    @Subcommand("info")
    @CommandCompletion("@guilds")
    fun GuildInfoCommand(sender: CommandSender, @Optional @Single name: String?) {
        val uuid = plugin.server.getPlayer(sender.name)?.uniqueId ?: return
        val guild = plugin.guildManager.getGuildFromPlayer(uuid) ?: return

        val leaderName: Component = guild.members.entries.filter { it.value == GuildRole.LEADER }.map {
                Component.text(
                    plugin.server.getOfflinePlayer(it.key).name ?: "",
                    Palette.onlineCheck(plugin.server.getOfflinePlayer(it.key).name ?: "")
                )
            }.first()

        val officerNames: List<Component?> = guild.members.entries.filter { it.value == GuildRole.OFFICER }.map {
            Component.text(
                plugin.server.getOfflinePlayer(it.key).name ?: "",
                Palette.onlineCheck(plugin.server.getOfflinePlayer(it.key).name ?: "")
            )
        }

        val memberNames: List<Component?> = guild.members.entries.filter { it.value == GuildRole.MEMBER }.map {
            Component.text(
                plugin.server.getOfflinePlayer(it.key).name ?: "",
                Palette.onlineCheck(plugin.server.getOfflinePlayer(it.key).name ?: "")
            )
        }

        sender.sendMessage(
            Component.text("Guild: ", Palette.PURPLE)
                .appendNewline()
                .append(Component.text("  ${guild.name} [", Palette.PURPLE))
                .append(Component.text(guild.tag, guild.color))
                .append(Component.text("] - ${guild.onlineMembers.size}/${guild.members.size}", Palette.STATUSLINE2))
                // ^^^ Guild: <guildName> [<tag>] - <online>/<total>
                .appendNewline()
                .append(Component.text(" Leader: ", Palette.PURPLE))
                .append(leaderName)
                // ^^^ Leader: <leaderName>
                .appendNewline()
                .append(Component.text(" Officers: ", Palette.PURPLE))
                .append(Component.text(officerNames.joinToString(", "), Palette.STATUSLINE2))
                // ^^^ Officers: <officer1>, <officer2>, ...
                .appendNewline()
                .append(Component.text(" Members: ", Palette.PURPLE))
                .append(Component.text(memberNames.joinToString(", "), Palette.STATUSLINE2))
                // ^^^ Members: <member1>, <member2>, ...
                .appendNewline()
        )
    }
}