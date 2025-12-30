package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.Asset
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface AssetRepository : R2dbcRepository<Asset, Long> {

    @Query("""
        SELECT * 
        FROM asset a
        JOIN asset_tag at ON a.id = at.asset_id
        JOIN tag t ON at.tag_id = t.id
        WHERE t.name = :tagName
    """)
    fun findAllByTag(tagName: String): Flux<Asset>
}