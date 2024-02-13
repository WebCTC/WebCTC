package components.icon

import mui.material.SvgIcon
import react.FC
import react.dom.html.ReactHTML.title
import react.dom.svg.ReactSVG.path

val mdiEraser = FC {
    SvgIcon {
        viewBox = "0 0 24 24"
        title {
            +"eraser"
        }
        path {
            d =
                "M16.24,3.56L21.19,8.5C21.97,9.29 21.97,10.55 21.19,11.34L12,20.53C10.44,22.09 7.91,22.09 6.34,20.53L2.81,17C2.03,16.21 2.03,14.95 2.81,14.16L13.41,3.56C14.2,2.78 15.46,2.78 16.24,3.56M4.22,15.58L7.76,19.11C8.54,19.9 9.8,19.9 10.59,19.11L14.12,15.58L9.17,10.63L4.22,15.58Z"
        }
    }
}

val mdiFence = FC {
    SvgIcon {
        viewBox = "0 0 24 24"
        title {
            +"fence"
        }
        path {
            d =
                "M9,6v2H7V6H5v2H3V6H1v12h2v-2h2v2h2v-2h2v2h2v-2h2v2h2v-2h2v2h2v-2h2v2h2V6h-2v2h-2V6h-2v2h-2V6h-2v2h-2V6H9 M3,10h2v4H3V10 M7,10h2v4H7V10 M11,10h2v4h-2V10 M15,10h2v4h-2V10 M19,10h2v4h-2V10z"
        }
    }
}

val mdiCursorDefaultOutline = FC {
    SvgIcon {
        viewBox = "0 0 24 24"
        title {
            +"cursor-default-outline"
        }
        path {
            d =
                "M10.07,14.27C10.57,14.03 11.16,14.25 11.4,14.75L13.7,19.74L15.5,18.89L13.19,13.91C12.95,13.41 13.17,12.81 13.67,12.58L13.95,12.5L16.25,12.05L8,5.12V15.9L9.82,14.43L10.07,14.27M13.64,21.97C13.14,22.21 12.54,22 12.31,21.5L10.13,16.76L7.62,18.78C7.45,18.92 7.24,19 7,19A1,1 0 0,1 6,18V3A1,1 0 0,1 7,2C7.24,2 7.47,2.09 7.64,2.23L7.65,2.22L19.14,11.86C19.57,12.22 19.62,12.85 19.27,13.27C19.12,13.45 18.91,13.57 18.7,13.61L15.54,14.23L17.74,18.96C18,19.46 17.76,20.05 17.26,20.28L13.64,21.97Z"
        }
    }
}