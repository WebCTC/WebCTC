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
import org.webctc.common.types.railgroup.RailGroupFolder
import org.webctc.openapi.OpenApiRoute
import org.webctc.railgroup.RailGroupData
import org.webctc.railgroup.RailGroupStateWS
import org.webctc.railgroup.create
import org.webctc.railgroup.delete
import org.webctc.router.WebCTCRouter
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class RailGroupRouter : WebCTCRouter() {
    companion object {
        val blockPosConnection = mutableMapOf<Uuid, Connection?>()
        val signalPosConnection = mutableMapOf<Uuid, Connection?>()
    }

    override fun install(application: Route): Route.() -> Unit = {
        @OpenApiRoute(summary = "List rail groups", response = RailGroup::class, responseList = true)
        get {
            call.respond(RailGroupData.railGroupList)
        }

        route("/folders") {
            @OpenApiRoute(
                docPath = "/folders",
                summary = "List rail group folders",
                response = RailGroupFolder::class,
                responseList = true
            )
            get {
                call.respond(RailGroupData.folderList)
            }
            authenticate("auth-session") {
                @OpenApiRoute(
                    docPath = "/folders",
                    summary = "Create a rail group folder",
                    response = RailGroupFolder::class,
                    authenticated = true
                )
                post {
                    val folder = RailGroupFolder.create()
                    call.respond(folder)
                    WebCTCCore.INSTANCE.railGroupData.markDirty()
                }
                route("/{Folder}") {
                    @OpenApiRoute(
                        docPath = "/folders/{Folder}",
                        summary = "Update a rail group folder",
                        request = RailGroupFolder::class,
                        response = RailGroupFolder::class,
                        authenticated = true
                    )
                    put {
                        val folder = call.getFolder() ?: return@put
                        val updated: RailGroupFolder = call.receive()
                        folder.updateBy(updated)
                        call.respond(folder)
                        WebCTCCore.INSTANCE.railGroupData.markDirty()
                    }
                    @OpenApiRoute(
                        docPath = "/folders/{Folder}",
                        summary = "Delete a rail group folder",
                        authenticated = true
                    )
                    delete {
                        val folder = call.getFolder() ?: return@delete
                        val parentUuid = folder.parentUuid
                        RailGroupData.folderList
                            .filter { it.parentUuid == folder.uuid }
                            .forEach { it.parentUuid = parentUuid }
                        RailGroupData.railGroupList
                            .filter { it.folderUuid == folder.uuid }
                            .forEach { it.folderUuid = parentUuid }
                        folder.delete()
                        call.respond(HttpStatusCode.OK)
                        WebCTCCore.INSTANCE.railGroupData.markDirty()
                    }
                }
            }
        }

        route("/{RailGroup}") {
            @OpenApiRoute(docPath = "/{RailGroup}", summary = "Get a rail group", response = RailGroup::class)
            get {
                call.getRailGroup()?.let { call.respond(it) }
            }
        }

        route("/state") {
            @OpenApiRoute(docPath = "/state/ws", summary = "Subscribe to rail group state")
            webSocket("/ws") {
                val uuids = receiveDeserialized<Set<Uuid>>()
                val railGroupStateWSSet = uuids.mapNotNull { uuid ->
                    val railGroup = RailGroupData.railGroupList.find { it.uuid == uuid }
                    if (railGroup != null) RailGroupStateWS(railGroup, this) else null
                }.toSet()
                for (frame in incoming) {
                }
                railGroupStateWSSet.forEach { it.close() }
            }
        }

        authenticate("auth-session") {
            @OpenApiRoute(summary = "Create a rail group", response = RailGroup::class, authenticated = true)
            post {
                val railGroup = RailGroup.create()
                call.respond(railGroup)

                WebCTCCore.INSTANCE.railGroupData.markDirty()
            }

            route("/{RailGroup}") {
                @OpenApiRoute(docPath = "/{RailGroup}", summary = "Delete a rail group", authenticated = true)
                delete {
                    val railGroup = call.getRailGroup() ?: return@delete

                    railGroup.delete()

                    call.respond(HttpStatusCode.OK)

                    WebCTCCore.INSTANCE.railGroupData.markDirty()
                }
                @OpenApiRoute(
                    docPath = "/{RailGroup}",
                    summary = "Update a rail group",
                    request = RailGroup::class,
                    response = RailGroup::class,
                    authenticated = true
                )
                put {
                    val oldRailGroup = call.getRailGroup() ?: return@put
                    val railGroup: RailGroup = call.receive()

                    oldRailGroup.updateBy(railGroup)

                    call.respond(railGroup)

                    WebCTCCore.INSTANCE.railGroupData.markDirty()
                }
            }
            route("ws") {
                @OpenApiRoute(docPath = "/ws/block", summary = "Start block position selection", authenticated = true)
                webSocket("/block") {
                    val uuid = call.sessions.get<WebCTCCore.UserSession>()?.uuid ?: return@webSocket
                    this.initPosSetter("BlockPosSetter", uuid, blockPosConnection, Items.stick)
                }
                @OpenApiRoute(docPath = "/ws/signal", summary = "Start signal position selection", authenticated = true)
                webSocket("/signal") {
                    val uuid = call.sessions.get<WebCTCCore.UserSession>()?.uuid ?: return@webSocket
                    this.initPosSetter("SignalPosSetter", uuid, signalPosConnection, Items.blaze_rod)
                }

            }
        }
    }
}

private suspend fun ApplicationCall.getFolder(): RailGroupFolder? {
    val uuid = parameters["Folder"]?.let { Uuid.parse(it) }
    val folder = RailGroupData.folderList.find { it.uuid == uuid }
    if (folder == null) respond(HttpStatusCode.NotFound)
    return folder
}

private suspend fun ApplicationCall.getRailGroup(): RailGroup? {
    val uuid = parameters["RailGroup"]?.let { Uuid.parse(it) }
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
    playerUUID: Uuid,
    connectionList: MutableMap<Uuid, Connection?>,
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
            .find { it.uniqueID == playerUUID.toJavaUuid() }
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
