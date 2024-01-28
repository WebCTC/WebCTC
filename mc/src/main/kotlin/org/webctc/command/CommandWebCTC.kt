package org.webctc.command

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import org.webctc.WebCTCConfig
import org.webctc.WebCTCCore
import org.webctc.cache.waypoint.WayPointCacheData
import org.webctc.common.types.PosDouble
import org.webctc.common.types.waypoint.WayPoint
import org.webctc.router.PlayerSessionManager

class CommandWebCTC : CommandBase() {

    override fun getCommandName() = "webctc"

    override fun getCommandUsage(sender: ICommandSender) = "/webctc waypoint create <identifier> <displayName>"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (sender is EntityPlayer) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "waypoint" -> {
                        if (args.size >= 2) {
                            if (args[1] == "create" && args.size >= 4) {
                                val pos = PosDouble(sender.posX, sender.posY, sender.posZ)
                                val waypoint = WayPoint(args[2], args.drop(3).joinToString(" "), pos)
                                WayPointCacheData.wayPointCache[args[2]] = waypoint
                                WebCTCCore.INSTANCE.wayPointData.markDirty()
                                sender.addChatMessage(ChatComponentText("Waypoint created."))
                            } else if (args[1] == "delete" && args.size >= 3) {
                                val waypoint = WayPointCacheData.wayPointCache.remove(args[2])
                                if (waypoint != null) {
                                    WebCTCCore.INSTANCE.wayPointData.markDirty()
                                    sender.addChatMessage(ChatComponentText("Waypoint deleted."))
                                } else {
                                    sender.addChatMessage(ChatComponentText("Waypoint not found."))
                                }
                            }
                        }
                    }

                    "auth" -> {
                        val sessionKey = PlayerSessionManager.createSession(sender)
                        val text = ChatComponentText("URL: ")
                        val isHttps = WebCTCConfig.accessUrl.startsWith("https://")
                        val port = WebCTCConfig.accessPort
                        val origin = buildString {
                            append(WebCTCConfig.accessUrl)
                            if (!(port == 80 && !isHttps || port == 443 && isHttps)) append(":$port")
                        }

                        val url = ChatComponentText("$origin/auth/mc-session-login?key=$sessionKey")
                        url.chatStyle.chatClickEvent =
                            ClickEvent(ClickEvent.Action.OPEN_URL, url.chatComponentText_TextValue)
                        text.appendSibling(url)
                        sender.addChatMessage(text)
                    }
                }
            }
        } else {
            sender.addChatMessage(ChatComponentText(("[WebCTC] Sorry! This command can only be executed by player.")))
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>): List<String>? {
        return when (args.size) {
            1 -> listOf("auth", "waypoint")
            2 -> if (args[0] == "waypoint") listOf("create", "delete").filter { it.startsWith(args[1]) } else null
            3 -> if (args[1] == "delete") WayPointCacheData.wayPointCache.keys.filter { it.startsWith(args[2]) } else null
            else -> null
        }
    }
}
