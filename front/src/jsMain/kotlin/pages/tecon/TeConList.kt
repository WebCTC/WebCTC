package pages.tecon

import client
import components.Header
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.Edit
import mui.material.*
import mui.system.Breakpoint
import mui.system.sx
import org.webctc.common.types.tecon.TeCon
import react.FC
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.h1
import tanstack.react.router.useNavigate
import tanstack.router.core.RoutePath
import utils.useListData
import web.cssom.px

val TeConList = FC {
    val teconList by useListData<TeCon>("/api/tecons")
    val navigate = useNavigate()

    CssBaseline {}
    Header {}
    Container {
        maxWidth = Breakpoint.md
        Card {
            sx {
                padding = 16.px
            }
            h1 {
                +"TeCon List"
            }

            Box {
                Button {
                    +"Add"
                    variant = ButtonVariant.outlined
                    onClick = {
                        MainScope().launch {
                            val new = client.post("/api/tecons").body<TeCon>()
                            navigate { to = RoutePath("edit/${new.uuid}") }
                        }
                    }
                }
            }

            Paper {
                List {
                    teconList.sortedBy { it.name }.forEach { tecon ->
                        ListItem {
                            disablePadding = true
                            secondaryAction = IconButton.create {
                                Edit {}
                                onClick = { navigate { to = RoutePath("edit/${tecon.uuid}") } }
                            }
                            ListItemButton {
                                ListItemText { primary = ReactNode(tecon.name) }
                                onClick = { navigate { to = RoutePath("view/${tecon.uuid}") } }
                            }
                        }
                    }
                }
            }
        }
    }
}