package com.maechuri.mainserver.game.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table

@Table(name = "asset_tag")
class AssetTag(
    @Id
    var id: Long? = null,
    var assetId: Long? = null,
    var tagId: Long? = null,
) {
    @Transient
    var tag: Tag? = null

    constructor(assetId: Long, tagId: Long) : this(id = null, assetId = assetId, tagId = tagId)
}