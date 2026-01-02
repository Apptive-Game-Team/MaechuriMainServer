package com.maechuri.mainserver.game.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table

@Table(name = "asset")
class Asset(
    @Id
    var id: Long? = null,
    var name: String,
    var metaFileUrl: String,
) {
    @Transient
    var assetTags: Set<AssetTag> = emptySet()
}