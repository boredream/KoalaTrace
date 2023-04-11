package com.boredream.koalatrace.utils

import com.boredream.koalatrace.data.event.SyncStatusEvent
import org.greenrobot.eventbus.EventBus

object SyncUtils {

    var isSyncing = false
        set(value) {
            field = value
            EventBus.getDefault().post(SyncStatusEvent(value))
        }

}