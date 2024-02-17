package pages

import components.Header
import mui.material.Container
import mui.material.CssBaseline
import react.FC
import react.dom.html.ReactHTML.h1

val FallBack = FC {
    CssBaseline {}
    Header {}
    Container {
        h1 {
            +"404 Page Not Found"
        }
    }
}