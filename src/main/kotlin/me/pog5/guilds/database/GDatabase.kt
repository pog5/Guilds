package me.pog5.guilds.database

import com.mongodb.kotlin.client.MongoClient
import me.pog5.guilds.Guilds
import me.pog5.guilds.data.Guild

class GDatabase(
    plugin: Guilds,
    connectinString: String = plugin.config.getString("mongo-uri") ?: "mongodb://localhost"
) {
    val mongoClient = MongoClient.create(connectinString)
    private val database = mongoClient.getDatabase("nightly")
    val collection = database.getCollection<Guild>("guilds")
}