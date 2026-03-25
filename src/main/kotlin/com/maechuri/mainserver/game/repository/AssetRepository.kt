package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.Asset
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AssetRepository : R2dbcRepository<Asset, Long> {
    fun findByName(name: String): Mono<Asset>

    @Query("""
        SELECT * 
        FROM asset a
        JOIN asset_tag at ON a.id = at.asset_id
        JOIN tag t ON at.tag_id = t.id
        WHERE t.name = :tagName
    """)
    fun findAllByTag(tagName: String): Flux<Asset>
}