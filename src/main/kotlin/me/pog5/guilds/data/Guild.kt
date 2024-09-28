package me.pog5.guilds.data

import me.pog5.guilds.Guilds
import me.pog5.guilds.util.Palette
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Statistic
import java.util.*

enum class GuildRole {
    MEMBER, OFFICER, LEADER,
}

class Guild(
    private val plugin: Guilds,
    var name: String,
    var tag: String = name,
    var color: TextColor = Palette.FG,
    var members: MutableMap<UUID, GuildRole> = mutableMapOf(),
) {
    val onlineMembers: Set<UUID>
        get() = members.keys.filter { plugin.server.getPlayer(it)?.isOnline == true }.toSet()
    val points: Double
        get() {
            val memberCount = members.size
            var totalKillCount = 0
            var totalDeathCount = 0
            var points = 0.00
            members.forEach {
                val playerKills = plugin.server.getOfflinePlayer(it.key).getStatistic(Statistic.PLAYER_KILLS)
                val playerDeaths = plugin.server.getOfflinePlayer(it.key).getStatistic(Statistic.DEATHS)
                totalKillCount += playerKills
                totalDeathCount += playerDeaths
                points += playerDeaths / playerDeaths
            }
            return points
        }
    val invites: MutableSet<UUID> = mutableSetOf()
    var pendingTransfer: UUID? = null

    fun addMember(member: UUID) {
        members[member] = GuildRole.MEMBER
        plugin.server.getPlayer(member)?.sendMessage(Component.text("You have joined $name!", Palette.STATUSLINE1))
    }

    fun promoteMember(member: UUID) {
        when (members[member]) {
            GuildRole.MEMBER -> members[member] = GuildRole.OFFICER
            GuildRole.OFFICER -> {
                members.entries.find { it.value == GuildRole.LEADER }
                    ?.let { leader -> members[leader.key] = GuildRole.OFFICER }
                members[member] = GuildRole.LEADER
            }

            else -> return
        }
        plugin.server.getPlayer(member)
            ?.sendMessage(Component.text("You have been promoted to ${members[member]} in $name!", Palette.STATUSLINE1))
    }

    fun demoteMember(member: UUID) {
        when (members[member]) {
            GuildRole.OFFICER -> members[member] = GuildRole.MEMBER
            GuildRole.LEADER -> members[member] = GuildRole.OFFICER
            else -> return
        }
        plugin.server.getPlayer(member)
            ?.sendMessage(Component.text("You have been demoted to ${members[member]} in $name!", Palette.STATUSLINE1))
    }

    fun removeMember(member: UUID) {
        val name = plugin.server.getPlayer(member)?.name

        members.remove(member)
//        members.let { it -> it.entries.find { it.value == GuildRole.OFFICER || it.value == GuildRole.LEADER } }
//            ?.let { guildStaff -> plugin.server.getPlayer(guildStaff.key)?.sendMessage(Component.text("$name has left $name.", Palette.STATUSLINE3)) }

        plugin.server.getPlayer(member)
            ?.sendMessage(Component.text("You have been removed from $name!", Palette.STATUSLINE3))
    }

    fun disband() {
        plugin.guildManager.deleteGuild(name)
        members.keys.forEach {
            plugin.server.getPlayer(it)
                ?.sendMessage(Component.text("The guild $name has been disbanded.", Palette.STATUSLINE3))
        }
        this.members.clear()
    }

    fun invitePlayer(player: UUID) {
        invites.add(player)
        plugin.server.getPlayer(player)?.sendMessage(
            Component.text(
                "You have been invited to join $name! You have 60 seconds to join.", Palette.STATUSLINE1
            )
        )

        val task = Runnable {
            invites.remove(player)
            plugin.server.getPlayer(player)
                ?.sendMessage(Component.text("The invitation to $name has expired.", Palette.STATUSLINE3))
        }
        plugin.server.scheduler.runTaskLater(plugin, task, 60 * 20)
    }

    fun join(player: UUID) {
        if (invites.contains(player)) {
            addMember(player)
            invites.remove(player)
        } else {
            plugin.server.getPlayer(player)
                ?.sendMessage(Component.text("You have not been invited to $name!", Palette.STATUSLINE3))
        }
    }

    fun kick(player: UUID, kicker: UUID?) {
        val playerName = plugin.server.getPlayer(player)?.name
        if (members.containsKey(player)) {
            removeMember(player)
            kicker?.let {
                plugin.server.getPlayer(it)?.sendMessage(
                    Component.text(
                        "You have kicked $playerName (${members[player]})", Palette.STATUSLINE3
                    )
                )
            }
        } else {
            kicker?.let {
                plugin.server.getPlayer(it)
                    ?.sendMessage(Component.text("Player $playerName not found in $name!", Palette.STATUSLINE3))
            }
        }
    }

    fun chat(player: UUID, message: String) {
        val playerName = plugin.server.getPlayer(player)?.name

        val builtMessage =
            Component.text().append(Guilds().prefix).append(Component.text("$playerName: ", Palette.GRAY))
                .append(Component.text(message, Palette.FG))

        plugin.server.onlinePlayers.forEach {
            if (members.containsKey(it.uniqueId)) {
                it.sendMessage(builtMessage)
            }
        }
    }
}