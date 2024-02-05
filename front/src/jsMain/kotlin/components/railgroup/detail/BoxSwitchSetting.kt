package components.railgroup.detail

import mui.icons.material.ArrowRight
import mui.icons.material.Delete
import mui.material.*
import mui.material.Size
import mui.system.sx
import org.webctc.common.types.railgroup.SettingEntry
import org.webctc.common.types.railgroup.SwitchSetting
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.onChange
import utils.setNew
import web.cssom.*
import web.html.HTMLInputElement

external interface BoxSwitchSettingProps : Props {
    var switchSetting: SwitchSetting
    var updateSwitchSetting: (SwitchSetting) -> Unit
}

val BoxSwitchSetting = FC<BoxSwitchSettingProps> { props ->
    val switchSetting = props.switchSetting
    val rsPos = switchSetting.switchRsPos
    val settingMap = switchSetting.settingMap
    val updateSwitchSetting = props.updateSwitchSetting

    Box {
        sx {
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 8.px
        }
        Box {
            +"Switch Setting"
        }
        Box {
            sx {
                paddingLeft = 8.px
                display = Display.flex
                flexDirection = FlexDirection.column
                gap = 8.px
            }
            BoxPosIntList {
                title = "Switch Rs Pos"
                posList = rsPos
                updatePosList = {
                    switchSetting.copy(switchRsPos = it)
                        .also(updateSwitchSetting)
                }
                wsPath = "/api/railgroups/ws/block"
            }
            Box {
                Box {
                    sx {
                        display = Display.flex
                        justifyContent = JustifyContent.spaceBetween
                        paddingBottom = 8.px
                    }
                    +"LockKey Mapping"
                    Button {
                        +"Add"
                        variant = ButtonVariant.outlined
                        onClick = {
                            switchSetting.copy(settingMap = settingMap + SettingEntry())
                                .also(updateSwitchSetting)
                        }
                    }
                }
                Paper {
                    List {
                        disablePadding = true
                        settingMap.forEachIndexed { index, entry ->
                            ListItem {
                                sx { paddingRight = 72.px }
                                disablePadding = true
                                secondaryAction = IconButton.create {
                                    Delete {}
                                    onClick = {
                                        switchSetting.copy(settingMap = settingMap - entry)
                                            .also(updateSwitchSetting)
                                    }
                                }

                                Box {
                                    sx {
                                        display = Display.flex
                                        padding = 6.px
                                        gap = 8.px
                                    }

                                    TextField {
                                        sx { flexGrow = number(1.0) }
                                        label = ReactNode("Key")
                                        size = Size.small
                                        value = entry.key
                                        onChange = { formEvent ->
                                            val event = formEvent.unsafeCast<ChangeEvent<HTMLInputElement>>()
                                            val newKey = event.target.value
                                            switchSetting.copy(
                                                settingMap = settingMap.setNew(index, entry.copy(key = newKey))
                                            ).also(updateSwitchSetting)
                                        }
                                    }
                                    ArrowRight { sx { marginBlock = 8.px } }
                                    Button {
                                        +(if (entry.value) "R" else "N")
                                        size = Size.small
                                        variant = ButtonVariant.outlined
                                        color = if (entry.value) ButtonColor.error else ButtonColor.success
                                        onClick = { _ ->
                                            switchSetting.copy(
                                                settingMap = settingMap.setNew(index, entry.copy(value = !entry.value))
                                            ).also(updateSwitchSetting)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}