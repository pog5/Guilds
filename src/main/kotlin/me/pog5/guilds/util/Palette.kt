package me.pog5.guilds.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import java.util.*

/**
 * Palette.kt
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * @author FlutterMC (https://github.com/FlutterMC/)
 * @contributor Aubrey @ aubrey.rs
 * @since 2024-09-12
 * @version 1.0
 */
object Palette {
    val BG_DIM = TextColor.color(0x1E2326)
    val BG0 = TextColor.color(0x272E33)
    val BG1 = TextColor.color(0x2E383C)
    val BG2 = TextColor.color(0x374145)
    val BG3 = TextColor.color(0x414B50)
    val BG4 = TextColor.color(0x495156)
    val BG5 = TextColor.color(0x4F5B58)

    val BG_RED = TextColor.color(0x4C3743)
    val BG_VISUAL = TextColor.color(0x493B40)
    val BG_YELLOW = TextColor.color(0x45443C)
    val BG_GREEN = TextColor.color(0x3C4841)
    val BG_BLUE = TextColor.color(0x384B55)

    val RED = TextColor.color(0xE67E80)
    val ORANGE = TextColor.color(0xE69875)
    val YELLOW = TextColor.color(0xDBBC7F)
    val GREEN = TextColor.color(0xA7C080)
    val BLUE = TextColor.color(0x7FBBB3)
    val AQUA = TextColor.color(0x83C092)
    val PURPLE = TextColor.color(0xD699B6)

    val FG = TextColor.color(0xD3C6AA)
    val GRAY = TextColor.color(0x859289)

    val STATUSLINE1 = TextColor.color(0xA7C080)
    val STATUSLINE2 = TextColor.color(0xD3C6AA)
    val STATUSLINE3 = TextColor.color(0xE67E80)

    val GRAY0 = TextColor.color(0x7A8478)
    val GRAY1 = TextColor.color(0x859289)
    val GRAY2 = TextColor.color(0x9DA9A0)

    fun readColor(hex: String): TextColor {
        val matcher = Regex("#?(?i)([a-f0-9]{6})").find(hex) ?: return FG
        return TextColor.color(Integer.parseInt(matcher.groupValues[1], 16))
    }

    fun componentToString(component: Component): String {
        return MiniMessage.miniMessage().serialize(component)
    }

    fun onlineCheck(name: String, colorIfOnline: TextColor = GREEN, colorIfOffline: TextColor = GRAY): TextColor {
        return if (Bukkit.getPlayer(name) != null) colorIfOnline else colorIfOffline
    }
}