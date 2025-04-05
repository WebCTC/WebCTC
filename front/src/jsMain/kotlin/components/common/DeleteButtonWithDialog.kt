package components.common

import mui.material.*
import react.FC
import react.Props
import react.useState

external interface DeleteButtonWithDialogProps : Props {
    var onDelete: () -> Unit
    var title: String
    var message: String
}

val DeleteButtonWithDialog = FC<DeleteButtonWithDialogProps> { props ->
    var openDialog by useState(false)

    Button {
        +"削除"
        variant = ButtonVariant.outlined
        color = ButtonColor.error
        onClick = { openDialog = true }
    }

    Dialog {
        open = openDialog
        onClose = { _, _ -> openDialog = false }
        title = props.title

        DialogContent {
            DialogContentText {
                +props.message
            }
        }

        DialogActions {
            Button {
                +"キャンセル"
                variant = ButtonVariant.outlined
                onClick = { openDialog = false }
            }

            Button {
                +"削除"
                color = ButtonColor.error
                variant = ButtonVariant.contained
                onClick = {
                    props.onDelete()
                    openDialog = false
                }
            }
        }
    }
}