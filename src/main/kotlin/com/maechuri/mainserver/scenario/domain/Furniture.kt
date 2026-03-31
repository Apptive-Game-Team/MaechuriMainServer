package com.maechuri.mainserver.scenario.domain

data class Furniture(
    val id: Long,
    val locationId: Long,
    val name: String,
    val description: String?,
    val originX: Int,
    val originY: Int,
    val width: Int,
    val height: Int,
    val assetsId: Int?
)
