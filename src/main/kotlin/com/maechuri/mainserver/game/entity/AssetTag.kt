package com.maechuri.mainserver.game.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "asset_tag")
class AssetTag {

    @Id
    var id: Long? = null
    var assetId: Long? = null
    var tagId: String? = null
}