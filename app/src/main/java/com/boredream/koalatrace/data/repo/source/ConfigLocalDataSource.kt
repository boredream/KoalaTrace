package com.boredream.koalatrace.data.repo.source

import com.boredream.koalatrace.utils.DataStoreUtils
import javax.inject.Inject

class ConfigLocalDataSource @Inject constructor() {

    companion object {
        const val DATA_SYNC_TIMESTAMP_KEY = "data_sync_timestamp_key"
    }

    fun set(key: String, value: Any) {
        DataStoreUtils.putSyncData(key, value)
    }

    fun getLong(key: String) = DataStoreUtils.readLongData(key, 0L)

}