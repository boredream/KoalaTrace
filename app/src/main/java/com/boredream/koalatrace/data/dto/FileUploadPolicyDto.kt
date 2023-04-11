package com.boredream.koalatrace.data.dto

data class FileUploadPolicyDTO(
    val token: String,
    val uploadHost: String,
    val downloadHost: String
)