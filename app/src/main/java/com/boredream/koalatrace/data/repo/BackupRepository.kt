package com.boredream.koalatrace.data.repo

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.Logger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val logger: Logger,
    private val appDatabase: AppDatabase,
) : BaseRepository() {

    private val appDbFile = File(PathUtils.getInternalAppDbPath(CommonConstant.DB_NAME))
    var backupDbFile = File(PathUtils.getExternalStoragePath(), "KoalaTrace/backup/" + appDbFile.name)
        private set

    suspend fun backup(): ResponseEntity<String> {
        val success = FileUtils.copy(appDbFile, backupDbFile)
        logger.i("backup success : $success")
        if(!success) {
            return ResponseEntity(null, 500, "备份失败")
        }
        return ResponseEntity.success("备份成功 " + backupDbFile.absolutePath)
    }

    fun restore(): ResponseEntity<String> {
        val success = FileUtils.copy(backupDbFile, appDbFile)
        logger.i("restore success : $success")
        if(!success) {
            return ResponseEntity(null, 500, "恢复备份失败")
        }
        // 恢复成功后需要重启下
        appDatabase.close()
        appDatabase.openHelper.writableDatabase
        return ResponseEntity.success("恢复备份成功 " + backupDbFile.absolutePath)
    }

}