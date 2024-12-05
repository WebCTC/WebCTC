package org.webctc.utils

import java.util.*

class TimerFunc {
    companion object {
        @JvmStatic
        fun scheduleRun(jsFunction: () -> Unit, delay: Long) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    jsFunction()
                }
            }, delay)
        }
    }
}
