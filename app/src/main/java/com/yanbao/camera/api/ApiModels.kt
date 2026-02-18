package com.yanbao.camera.api

/**
 * 通用API响应包装
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T
)

/**
 * 分页响应
 */
data class PagedResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int
)
