package org.webctc.router.api

import cpw.mods.fml.common.FMLCommonHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import kotlinx.uuid.UUID
import kotlinx.uuid.toKotlinUUID
import kotlinx.uuid.toUUID
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.GOLD
import net.minecraft.util.EnumChatFormatting.WHITE
import org.webctc.WebCTCCore
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.railgroup.*
import org.webctc.router.WebCTCRouter

class RailGroupRouter : WebCTCRouter() {
    companion object {
        val blockPosConnection = mutableMapOf<UUID, Connection?>()
        val signalPosConnection = mutableMapOf<UUID, Connection?>()
    }

    override fun install(application: Route): Route.() -> Unit = {
        get {
            call.respond(RailGroupData.railGroupList)
        }
        route("/{RailGroup}") {
            get {
                call.getRailGroup()?.let { call.respond(it) }
            }

            get("/state") {
                val railGroup = call.getRailGroup() ?: return@get
                val railGroupState = railGroup.getState()

                call.respond(railGroupState)
            }

            webSocket("/state/ws") {
                val railGroup = call.getRailGroup() ?: return@webSocket
                val railGroupStateWS = RailGroupStateWS(railGroup, this)
                for (frame in incoming) {
                }
                railGroupStateWS.close()
            }
        }

        authenticate("auth-session") {
            post {
                val railGroup = RailGroup.create()
                call.respond(railGroup)

                WebCTCCore.INSTANCE.railGroupData.markDirty()
            }

            route("/{RailGroup}") {
                delete {
                    val railGroup = call.getRailGroup() ?: return@delete

                    railGroup.delete()

                    call.respond(HttpStatusCode.OK)

                    WebCTCCore.INSTANCE.railGroupData.markDirty()
                }
                put {
                    val oldRailGroup = call.getRailGroup() ?: return@put
                    val railGroup: RailGroup = call.receive()

                    oldRailGroup.updateBy(railGroup)

                    call.respond(railGroup)

                    WebCTCCore.INSTANCE.railGroupData.markDirty()
                }
            }
            route("ws") {
                webSocket("/block") {
                    val uuid = call.sessions.get<WebCTCCore.UserSession>()?.uuid ?: return@webSocket
                    this.initPosSetter("BlockPosSetter", uuid, blockPosConnection, Items.stick)
                }
                webSocket("/signal") {
                    val uuid = call.sessions.get<WebCTCCore.UserSession>()?.uuid ?: return@webSocket
                    this.initPosSetter("SignalPosSetter", uuid, signalPosConnection, Items.blaze_rod)
                }

            }
        }
    }
}

private suspend fun ApplicationCall.getRailGroup(): RailGroup? {
    val uuid = parameters["RailGroup"]?.toUUID()
    val railGroup = RailGroupData.railGroupList.find { it.uuid == uuid }
    if (railGroup == null) {
        respond(HttpStatusCode.NotFound)
    }
    return railGroup
}

private suspend fun ApplicationCall.getPosInt(): PosInt? {
    val x = request.queryParameters["x"]?.toIntOrNull()
    val y = request.queryParameters["y"]?.toIntOrNull()
    val z = request.queryParameters["z"]?.toIntOrNull()
    if (x == null || y == null || z == null) {
        respond(HttpStatusCode.BadRequest)
        return null
    }
    return PosInt(x, y, z)
}

suspend fun WebSocketServerSession.initPosSetter(
    itemName: String,
    playerUUID: UUID,
    connectionList: MutableMap<UUID, Connection?>,
    item: Item
) {
    val thisConnection = Connection(this)
    try {
        val itemStack = ItemStack(item).apply {
            tagCompound = NBTTagCompound().apply {
                setTag("ench", NBTTagList().apply {
                    appendTag(NBTTagCompound().apply {
                        setShort("id", 255)
                        setShort("lvl", 0)
                    })
                })
            }
            setStackDisplayName(itemName)
        }
        MinecraftServer.getServer().entityWorld.playerEntities
            .filterIsInstance<EntityPlayer>()
            .find { it.uniqueID.toKotlinUUID() == playerUUID }
            ?.let { player ->
                if (!player.inventory.mainInventory.all {
                        ItemStack.areItemStacksEqual(it, itemStack)
                                && ItemStack.areItemStackTagsEqual(it, itemStack)
                    }) {
                    player.inventory.addItemStackToInventory(itemStack)
                }
                player.entityWorld.playSoundAtEntity(player, "random.levelup", 1.0f, 1.0f)
                player.addChatComponentMessage(
                    ChatComponentText(
                        "${WebCTCCore.IN_CHAT_LOGO} ${WHITE}Click block with $GOLD$itemName ${WHITE}to send pos to the web client."
                    )
                )
                connectionList[playerUUID] = thisConnection

                for (frame in incoming) {
                }
            }
    } catch (e: Exception) {
        FMLCommonHandler.instance().fmlLogger.error(e.stackTrace.toString())
    }
    connectionList.remove(playerUUID, thisConnection)
}