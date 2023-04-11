package com.boredream.koalatrace.ui.trace

import com.boredream.koalatrace.data.TraceLocation

data class TraceMapLocationState(
    val myLocation: TraceLocation? = null,
    val isFollowing: Boolean = true,
)