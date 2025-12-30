package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.AssetTag
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface AssetTagRepository : R2dbcRepository<AssetTag, Long> {
}