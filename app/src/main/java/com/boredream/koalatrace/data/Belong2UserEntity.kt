package com.boredream.koalatrace.data

import androidx.room.Ignore
import com.boredream.koalatrace.base.BaseEntity

open class Belong2UserEntity : BaseEntity() {

    @Ignore
    open lateinit var user: User

}