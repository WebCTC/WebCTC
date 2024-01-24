package org.webctc

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import io.ktor.server.websocket.*
import jp.ngt.rtm.RTMBlock
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.uuid.UUID
import kotlinx.uuid.toKotlinUUID
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraft.util.EnumChatFormatting.WHITE
import net.minecraft.world.WorldServer
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.railgroup.RailGroupData
import org.webctc.railgroup.update
import org.webctc.router.api.Connection
import org.webctc.router.api.RailGroupRouter
import java.util.*

class WebCTCEventHandler {
    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.entityPlayer
        val itemStack = player.heldItem
        if (event.world is WorldServer &&
            event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
            itemStack?.isItemEnchanted == true
        ) {
            val item = itemStack.item
            val uuid = player.uniqueID.toKotlinUUID()
            val pos = PosInt(event.x, event.y, event.z)
            if (item == Items.stick && itemStack.displayName == "BlockPosSetter") {
                RailGroupRouter.blockPosConnection[uuid]?.trySendBlockPos(player, pos)
            } else if (item == Items.blaze_rod && itemStack.displayName == "SignalPosSetter" && event.targetBlock() == RTMBlock.signal) {
                RailGroupRouter.signalPosConnection[uuid]?.trySendBlockPos(player, pos)
            }
        }
    }

    @SubscribeEvent
    fun onBreakBlock(event: BlockEvent.BreakEvent) {
        val tile = event.world.getTileEntity(event.x, event.y, event.z)
        if (tile is TileEntityLargeRailBase) {
            val core = tile.railCore
            val pos = PosInt(core.xCoord, core.yCoord, core.zCoord)
            val usedByWebCTC = RailGroupData.railGroupList.map { it.railPosList }.flatten().any { it == pos }
            if (usedByWebCTC) {
                event.player.addChatComponentMessage(
                    ChatComponentText("${RED}This rail is managed by WebCTC(RailGroup).")
                )

                val uuids = RailGroupData.railGroupList
                    .filter { it.railPosList.contains(pos) }
                    .map(RailGroup::uuid)
                    .joinToString(transform = UUID::toString)

                event.player.addChatComponentMessage(
                    ChatComponentText("${RED}If you want to break this rail, first remove it from $uuids.")
                )
                event.isCanceled = true
            }
        }
    }


    private var tickCount = 0

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        tickCount++
        if (event.phase.equals(TickEvent.Phase.END)) {
            if (tickCount == 20) {
                WebCTCCore.INSTANCE.railData.update()
                WebCTCCore.INSTANCE.signalData.update()
                tickCount = 0
            }
            RailGroupData.railGroupList.forEach { it.update() }
        }

    }
}

fun PlayerInteractEvent.targetBlock(): Block {
    return this.world.getBlock(this.x, this.y, this.z)
}

fun Connection.trySendBlockPos(player: EntityPlayer, pos: PosInt) {
    MainScope().launch(Dispatchers.IO) {
        session.sendSerialized(pos)
        player.addChatComponentMessage(
            ChatComponentText("${WebCTCCore.IN_CHAT_LOGO} $WHITE$pos was sent to web client.")
        )
        player.playSoundAtEntity("random.bow", 0.5f, 0.4f / (Random().nextFloat() * 0.4f + 0.8f))
    }
}

private fun EntityPlayer.playSoundAtEntity(name: String, volute: Float, pitch: Float) {
    this.worldObj.playSoundAtEntity(this, name, volute, pitch)
}
