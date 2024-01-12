package org.webctc.command

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ChatComponentText
import org.webctc.WebCTCCore
import org.webctc.cache.waypoint.WayPointCacheData
import org.webctc.common.types.Pos
import org.webctc.common.types.WayPoint

class CommandWebCTC : CommandBase() {
    override fun getCommandName() = "webctc"

    override fun getCommandUsage(sender: ICommandSender) = "/webctc waypoint create <identifier> <displayName>"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (sender is EntityPlayerMP) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "waypoint" -> {
                        if (args.size >= 2) {
                            if (args[1] == "create" && args.size >= 4) {
                                val pos = Pos(sender.posX.toInt(), sender.posY.toInt(), sender.posZ.toInt())
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
                }
            }
        } else {
            sender.addChatMessage(ChatComponentText(("[WebCTC] Sorry! This command can only be executed by player.")))
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>): List<String>? {
        return when (args.size) {
            1 -> listOf("waypoint")
            2 -> if (args[0] == "waypoint") listOf("create", "delete").filter { it.startsWith(args[1]) } else null
            3 -> if (args[1] == "delete") WayPointCacheData.wayPointCache.keys.filter { it.startsWith(args[2]) } else null
            else -> null
        }
    }
}
