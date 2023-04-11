package com.boredream.koalatrace.data.repo

import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.User
import com.boredream.koalatrace.data.dto.LoginDto
import com.boredream.koalatrace.data.repo.source.UserLocalDataSource
import com.boredream.koalatrace.net.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val service: ApiService,
    private val localDataSource: UserLocalDataSource
) : BaseRepository() {

    suspend fun autoLogin(): ResponseEntity<User> {
        localDataSource.getToken() ?: return ResponseEntity.notExistError()

        try {
            return service.getUserInfo()
        } catch (e: Exception) {
            // 获取失败，则尝试从本地获取
            localDataSource.getUser()?.let { return ResponseEntity.success(it) }
        }
        return ResponseEntity.notExistError()
    }

    suspend fun login(username: String, password: String): ResponseEntity<User> {
        val response = service.login(LoginDto(username, password))
        return if (response.isSuccess()) {
            localDataSource.saveToken(response.getSuccessData())
            getUserInfo()
        } else {
            ResponseEntity(null, response.code, response.msg)
        }
    }

    fun logout() {
        localDataSource.saveToken(null)
        localDataSource.saveUser(null)
    }

    suspend fun getUserInfo(): ResponseEntity<User> {
        return try {
            val response = service.getUserInfo()
            if (response.isSuccess()) {
                localDataSource.saveUser(response.getSuccessData())
            }
            response
        } catch (e: Exception) {
            ResponseEntity.httpError(e)
        }
    }

    fun getLocalUser(): User? {
        return localDataSource.getUser()
    }

    suspend fun updateTogetherDay(togetherDay: String): ResponseEntity<Boolean> {
        val curUser = getLocalUser() ?: return ResponseEntity.notExistError()
        curUser.cpTogetherDate = togetherDay
        val response = service.updateUserInfo(curUser.id!!, curUser)
        if (response.isSuccess()) {
            localDataSource.saveUser(curUser)
        }
        return response
    }

}