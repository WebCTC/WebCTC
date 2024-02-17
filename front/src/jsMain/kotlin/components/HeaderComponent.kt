package components

import emotion.styled.styled
import kotlinx.browser.window
import mui.icons.material.AccountCircle
import mui.material.*
import mui.material.Size
import mui.system.sx
import react.FC
import react.Props
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.svg
import react.router.useNavigate
import web.cssom.*

val Header = FC<Props> {
    val navigate = useNavigate()

    AppBar {
        position = AppBarPosition.sticky
        sx {
            backgroundColor = Color("#3d3d3d")
        }
        Toolbar {
            Box {
                sx {
                    flexGrow = number(1.0)
                    display = Display.flex
                    alignItems = AlignItems.center
                    height = 100.pct
                }

                IconButton {
                    Logo {
                        xmlns = "http://www.w3.org/2000/svg"
                        viewBox = "0 0 190.058 51"
                        path {
                            d =
                                "M 176.8482360839844 51.00030136108398 C 161.1449890136719 51.00030136108398 151.6050109863281 41.16689682006836 151.6050109863281 26.12430191040039 C 151.6050109863281 9.833400726318359 162.9063415527344 0 178.0227355957031 0 L 190.0575256347656 0 L 190.0575256347656 7.485298156738281 L 178.3890075683594 7.485298156738281 C 168.3360290527344 7.485298156738281 161.0711975097656 13.79610061645508 161.0711975097656 25.68330001831055 C 161.0711975097656 36.54359817504883 167.4558410644531 43.5150032043457 178.3161315917969 43.5150032043457 L 190.0575256347656 43.5150032043457 L 190.0575256347656 51.00030136108398 L 176.8482360839844 51.00030136108398 Z M 128.3435974121094 51.00030136108398 L 128.3435974121094 7.485298156738281 L 114.2550048828125 7.485298156738281 L 114.2550048828125 0 L 151.6059265136719 0 L 151.6059265136719 7.485298156738281 L 137.3697204589844 7.485298156738281 L 137.3697204589844 51.00030136108398 L 128.3435974121094 51.00030136108398 Z M 101.0457153320313 51.00030136108398 C 85.342529296875 51.00030136108398 75.80252075195313 41.16689682006836 75.80252075195313 26.12430191040039 C 75.80252075195313 9.833400726318359 87.10382080078125 0 102.22021484375 0 L 114.2550048828125 0 L 114.2550048828125 7.485298156738281 L 102.5865173339844 7.485298156738281 C 92.53350830078125 7.485298156738281 85.26870727539063 13.79610061645508 85.26870727539063 25.68330001831055 C 85.26870727539063 36.54359817504883 91.6533203125 43.5150032043457 102.5136108398438 43.5150032043457 L 114.2550048828125 43.5150032043457 L 114.2550048828125 51.00030136108398 L 101.0457153320313 51.00030136108398 Z M 30.64410400390625 28.99170303344727 C 29.37692260742188 28.53719711303711 28.30230712890625 27.86580276489258 27.42031860351563 26.97659683227539 C 26.53921508789063 26.08829879760742 25.86422729492188 24.99029922485352 25.39532470703125 23.6807975769043 C 24.92730712890625 22.37219619750977 24.69329833984375 20.85750198364258 24.69329833984375 19.1348991394043 C 24.69329833984375 17.42670059204102 24.92730712890625 15.87329864501953 25.39532470703125 14.47560119628906 C 25.86422729492188 13.07699966430664 26.52572631835938 11.87910079956055 27.37982177734375 10.88010025024414 C 28.23391723632813 9.881099700927734 29.26980590820313 9.109798431396484 30.48931884765625 8.565299987792969 C 31.70880126953125 8.021697998046875 33.0687255859375 7.749000549316406 34.57080078125 7.749000549316406 C 36.05850219726563 7.749000549316406 37.3734130859375 7.980300903320313 38.51730346679688 8.441997528076172 C 39.66030883789063 8.902797698974609 40.617919921875 9.547199249267578 41.38922119140625 10.37339782714844 C 42.16140747070313 11.20050048828125 42.74639892578125 12.18510055541992 43.14602661132813 13.32899856567383 C 43.54562377929688 14.47200012207031 43.74542236328125 15.72570037841797 43.74542236328125 17.08919906616211 C 43.74542236328125 17.43390274047852 43.734619140625 17.88119888305664 43.71392822265625 18.43290328979492 C 43.69320678710938 18.98369979858398 43.66262817382813 19.50030136108398 43.6212158203125 19.98270034790039 L 29.81790161132813 19.98270034790039 C 29.81790161132813 20.90520095825195 29.95919799804688 21.71789932250977 30.2418212890625 22.42080307006836 C 30.52349853515625 23.12369918823242 30.92312622070313 23.71229934692383 31.43972778320313 24.18749618530273 C 31.956298828125 24.66270065307617 32.576416015625 25.02450180053711 33.30001831054688 25.27199935913086 C 34.022705078125 25.5203971862793 34.82550048828125 25.64459609985352 35.70660400390625 25.64459609985352 C 36.726318359375 25.64459609985352 37.81802368164063 25.56540298461914 38.98171997070313 25.40700149536133 C 40.14630126953125 25.24860000610352 41.35501098632813 24.99029922485352 42.60870361328125 24.63209915161133 L 42.60870361328125 28.64069747924805 C 42.0714111328125 28.79189682006836 41.48550415039063 28.92959976196289 40.85189819335938 29.05379867553711 C 40.21832275390625 29.17799758911133 39.57122802734375 29.28779983520508 38.90969848632813 29.38409805297852 C 38.24819946289063 29.48039627075195 37.58041381835938 29.55330276489258 36.90542602539063 29.60100173950195 C 36.23040771484375 29.64960098266602 35.57611083984375 29.67390060424805 34.9425048828125 29.67390060424805 C 33.3441162109375 29.67390060424805 31.91131591796875 29.44620132446289 30.64410400390625 28.99170303344727 Z M 31.34701538085938 12.78089904785156 C 30.5343017578125 13.62779998779297 30.05191040039063 14.822998046875 29.90072631835938 16.36650085449219 L 38.62081909179688 16.36650085449219 C 38.63430786132813 15.53939819335938 38.53082275390625 14.81940078735352 38.310302734375 14.20650100708008 C 38.08981323242188 13.59360122680664 37.791015625 13.08779907226563 37.412109375 12.68819808959961 C 37.033203125 12.28860092163086 36.59222412109375 11.99250030517578 36.089111328125 11.79899978637695 C 35.58599853515625 11.60639953613281 35.0460205078125 11.51009750366211 34.46731567382813 11.51009750366211 C 33.20010375976563 11.51009750366211 32.15969848632813 11.93399810791016 31.34701538085938 12.78089904785156 Z M 52.49612426757813 29.22929763793945 C 51.18032836914063 29.00249862670898 49.92391967773438 28.69559860229492 48.72512817382813 28.30949783325195 L 48.72512817382813 0 L 53.76690673828125 0 L 53.76690673828125 6.860698699951172 L 53.56082153320313 10.99349975585938 C 54.318603515625 10.01519775390625 55.20330810546875 9.230400085449219 56.2158203125 8.638198852539063 C 57.22830200195313 8.045097351074219 58.4442138671875 7.749000549316406 59.86260986328125 7.749000549316406 C 61.10281372070313 7.749000549316406 62.205322265625 7.997398376464844 63.16921997070313 8.493297576904297 C 64.13311767578125 8.989200592041016 64.943115234375 9.694797515869141 65.597412109375 10.61100006103516 C 66.251708984375 11.52719879150391 66.751220703125 12.6359977722168 67.09500122070313 13.93830108642578 C 67.43972778320313 15.23970031738281 67.61160278320313 16.70399856567383 67.61160278320313 18.32940292358398 C 67.61160278320313 20.25809860229492 67.33981323242188 21.92490005493164 66.79531860351563 23.32979965209961 C 66.251708984375 24.73469924926758 65.49392700195313 25.90289688110352 64.5228271484375 26.83259963989258 C 63.55172729492188 27.76230239868164 62.40420532226563 28.45080184936523 61.08212280273438 28.89899826049805 C 59.760009765625 29.3463020324707 58.32000732421875 29.57040023803711 56.76300048828125 29.57040023803711 C 55.23391723632813 29.57040023803711 53.81192016601563 29.45699691772461 52.49612426757813 29.22929763793945 Z M 56.23651123046875 13.10129928588867 C 55.4580078125 13.78979873657227 54.63540649414063 14.72669982910156 53.76690673828125 15.91109848022461 L 53.76690673828125 25.08659744262695 C 54.16650390625 25.23779678344727 54.6588134765625 25.36200332641602 55.24472045898438 25.45830154418945 C 55.8297119140625 25.55459976196289 56.42550659179688 25.60319900512695 57.0321044921875 25.60319900512695 C 57.831298828125 25.60319900512695 58.5576171875 25.43760299682617 59.2119140625 25.10729598999023 C 59.8662109375 24.77609634399414 60.42422485351563 24.30810165405273 60.88592529296875 23.70149612426758 C 61.34762573242188 23.09579849243164 61.70220947265625 22.35509872436523 61.94970703125 21.48029708862305 C 62.1981201171875 20.60550308227539 62.32232666015625 19.6245002746582 62.32232666015625 18.53549575805664 C 62.32232666015625 17.37900161743164 62.239501953125 16.39350128173828 62.07391357421875 15.5807991027832 C 61.9083251953125 14.76810073852539 61.67071533203125 14.10029983520508 61.36111450195313 13.57649993896484 C 61.051513671875 13.05270004272461 60.672607421875 12.67110061645508 60.22442626953125 12.42990112304688 C 59.777099609375 12.18869781494141 59.26409912109375 12.06809997558594 58.6854248046875 12.06809997558594 C 57.831298828125 12.06809997558594 57.0150146484375 12.41279983520508 56.23651123046875 13.10129928588867 Z M 14.42340087890625 29.19869613647461 L 11.75759887695313 20.87099838256836 L 10.7864990234375 17.48160171508789 L 9.8568115234375 20.95380020141602 L 7.232421875 29.19869613647461 L 1.5084228515625 29.19869613647461 L 0 2.190597534179688 L 4.48382568359375 2.190597534179688 L 5.124603271484375 19.5066032409668 L 5.29022216796875 24.23879623413086 L 6.508819580078125 20.02409744262695 L 9.484222412109375 10.41479873657227 L 12.68731689453125 10.41479873657227 L 15.82830810546875 20.60190200805664 L 16.8408203125 24.1973991394043 L 16.92361450195313 19.98270034790039 L 17.60580444335938 2.190597534179688 L 21.92401123046875 2.190597534179688 L 20.3948974609375 29.19869613647461 L 14.42340087890625 29.19869613647461 Z"
                        }
                        fill = "darkslategray"
                    }
                    onClick = {
                        navigate("/")
                    }
                }
            }
            Box {
                IconButton {
                    color = IconButtonColor.inherit
                    size = Size.large
                    AccountCircle {
                    }
                    onClick = {
                        window.location.href = "/p/account"
                    }
                }
            }
        }
    }
}
val Logo = svg.styled {
    height = 50.px
    padding = Padding(5.px, 15.px)
    backgroundColor = Color("white")
    borderRadius = 10.px
}