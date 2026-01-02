package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.AssetTag
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface AssetTagRepository : R2dbcRepository<AssetTag, Long> {
    fun findByAssetIdIn(assetIds: List<Long>): Flow<AssetTag>
    suspend fun deleteByAssetId(assetId: Long): Int
    suspend fun deleteByTagId(tagId: Long): Int
}