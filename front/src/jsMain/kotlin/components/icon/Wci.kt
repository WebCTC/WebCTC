package components.icon

import mui.material.SvgIcon
import react.FC
import react.dom.html.ReactHTML.title
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.polygon

val WciSignal = FC {
    SvgIcon {
        viewBox = "0 0 24 24"
        title {
            +"signal"
        }
        path {
            d =
                "M12,13c-2.8,0-5-2.2-5-5s2.2-5,5-5s5,2.2,5,5S14.8,13,12,13z M12,5c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S13.7,5,12,5z"
        }
        polygon {
            points = "17,21 7,21 7,19 11,19 11,12 13,12 13,19 17,19"
        }
    }
}

val WciRouteLever = FC {
    SvgIcon {
        viewBox = "0 0 24 24"
        title {
            +"route lever"
        }
        path {
            d =
                "M17.4,17H4.5C3.7,17,3,16.3,3,15.5v-7C3,7.7,3.7,7,4.5,7h12.9c0.5,0,1,0.3,1.3,0.7l2.1,3.5c0.3,0.5,0.3,1.1,0,1.5l-2.1,3.5 C18.4,16.7,17.9,17,17.4,17z M5,15h12.1l1.8-3l-1.8-3H5V15z"
        }
    }
}

val WciRouteSelection = FC {
    SvgIcon {
        viewBox = "0 0 24 24"
        title {
            +"route selection"
        }
        path {
            d =
                "M16.5,18h-9C6.7,18,6,17.3,6,16.5v-9C6,6.7,6.7,6,7.5,6h9C17.3,6,18,6.7,18,7.5v9C18,17.3,17.3,18,16.5,18z M8,16h8V8H8V16z"
        }
    }
}