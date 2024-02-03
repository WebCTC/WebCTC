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
import react.create
import react.dom.events.ChangeEvent
import react.dom.onChange
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
                    updateSwitchSetting(switchSetting.copy(switchRsPos = it))
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
                            updateSwitchSetting(
                                switchSetting.copy(
                                    settingMap = settingMap + SettingEntry()
                                )
                            )
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
                                        switchSetting.settingMap = settingMap - entry
                                        updateSwitchSetting(switchSetting)
                                    }
                                }

                                Box {
                                    sx {
                                        display = Display.flex
                                        padding = 6.px
                                        gap = 8.px
                                    }

                                    TextField {
                                        sx {
                                            flexGrow = number(1.0)
                                        }
                                        size = Size.small
                                        value = entry.key
                                        onChange = { formEvent ->
                                            val event = formEvent.unsafeCast<ChangeEvent<HTMLInputElement>>()
                                            val newKey = event.target.value
                                            updateSwitchSetting(
                                                switchSetting.copy(
                                                    settingMap = settingMap.toMutableList().apply {
                                                        this[index] = entry.copy(key = newKey)
                                                    }.toSet()
                                                )
                                            )
                                        }
                                    }
                                    ArrowRight {}
                                    Button {
                                        +entry.value.toString()
                                        size = Size.small
                                        variant = ButtonVariant.outlined
                                        onClick = { _ ->
                                            updateSwitchSetting(
                                                switchSetting.copy(
                                                    settingMap = settingMap.toMutableList().apply {
                                                        this[index] = entry.copy(value = !entry.value)
                                                    }.toSet()
                                                )
                                            )
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