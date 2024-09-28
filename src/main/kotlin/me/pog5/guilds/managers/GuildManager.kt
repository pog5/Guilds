package me.pog5.guilds.managers

import com.mongodb.client.model.Filters.eq
import me.pog5.guilds.Guilds
import me.pog5.guilds.data.Guild
import me.pog5.guilds.data.GuildRole
import me.pog5.guilds.database.GDatabase
import me.pog5.guilds.util.Palette
import net.kyori.adventure.text.Component
import sun.font.TrueTypeFont.trueTag
import java.util.*

class GuildManager(val plugin: Guilds) {
    val guilds: MutableList<Guild> = mutableListOf()
    val guildChatters: MutableSet<UUID> = mutableSetOf()

    init {
        plugin.database.collection.find().forEach {
            guilds.add(it)
        }
    }

    fun createGuild(name: String, tag: String, creator: UUID) {
        if (getGuild(name) != null) {
            plugin.server.getPlayer(creator)?.sendMessage(Component.text("A guild with that name already exists!", Palette.STATUSLINE3))
            return
        }
        if (getGuildByTag(tag) != null) {
            plugin.server.getPlayer(creator)?.sendMessage(Component.text("A guild with that tag already exists!",
                Palette.STATUSLINE3))
            return
        }


        val guild = Guild(plugin, name, tag)
        plugin.database.collection.insertOne(guild)
        guilds.add(guild)
        guild.addMember(creator)
        while (guild.members[creator] != GuildRole.LEADER) {
            guild.promoteMember(creator)
        }
    }

    fun deleteGuild(name: String) {
        guilds.find { it.name == name }?.disband()
        plugin.database.collection.deleteMany(eq(Guild::name.name, name))
        guilds.removeIf { it.name == name }
    }

    fun getGuild(name: String): Guild? {
        return plugin.database.collection.find(eq(Guild::name.name, name)).firstOrNull()
    }

    fun getGuildByTag(tag: String): Guild? {
        return plugin.database.collection.find(eq(Guild::tag.name, tag)).firstOrNull()
    }

    fun getGuilds() : Set<String> {
        return guilds.map { it.name }.toSet()
    }

    fun getTags() : Set<String> {
        return guilds.map { it.tag }.toSet()
    }

    fun getGuildFromPlayer(player: UUID): Guild? {
        return guilds.find { it.members.containsKey(player) }
    }
}