package com.boredream.koalatrace.data.constant

import com.boredream.koalatrace.data.User
import com.boredream.koalatrace.utils.DataStoreUtils
import com.google.gson.Gson

object GlobalConstant {

    var token: String? = null
    var curUser: User? = null
    var isForeground: Boolean = true

    fun saveToken(token: String?) {
        DataStoreUtils.putSyncData(DataStoreKey.TOKEN, token ?: "")
        GlobalConstant.token = token
    }

    fun getLocalToken(): String? {
        if (token == null) {
            try {
                token = DataStoreUtils.getSyncData(DataStoreKey.TOKEN, "")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return token
    }

    fun saveUser(user: User?) {
        DataStoreUtils.putSyncData(DataStoreKey.USER, if(user == null) "" else Gson().toJson(user))
        curUser = user
    }

    fun getLocalUser(): User? {
        if (curUser == null) {
            try {
                val userJson = DataStoreUtils.getSyncData(DataStoreKey.USER, "")
                val user = Gson().fromJson(userJson, User::class.java)
                curUser = user
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return curUser
    }

}