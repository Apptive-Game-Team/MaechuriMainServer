package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.entity.Asset
import com.maechuri.mainserver.game.entity.AssetTag
import com.maechuri.mainserver.game.entity.Tag
import com.maechuri.mainserver.game.repository.AssetRepository
import com.maechuri.mainserver.game.repository.AssetTagRepository
import com.maechuri.mainserver.game.repository.TagRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssetService(
    private val assetRepository: AssetRepository,
    private val tagRepository: TagRepository,
    private val assetTagRepository: AssetTagRepository,
) {
    @Transactional(readOnly = true)
    suspend fun getAllAssets(): List<Asset> {
        val assets = assetRepository.findAll().collectList().awaitFirst()
        if (assets.isEmpty()) return emptyList()

        val assetIds = assets.mapNotNull { it.id }
        val assetTags = assetTagRepository.findByAssetIdIn(assetIds).toList()
        val tagIds = assetTags.mapNotNull { it.tagId }.distinct()
        val tags = tagRepository.findAllById(tagIds).collectList().awaitFirst().associateBy { it.id }

        assetTags.forEach { assetTag ->
            assetTag.tag = tags[assetTag.tagId]
        }

        val assetTagsByAssetId = assetTags.groupBy { it.assetId }
        assets.forEach { asset ->
            asset.assetTags = assetTagsByAssetId[asset.id]?.toSet() ?: emptySet()
        }

        return assets
    }

    @Transactional(readOnly = true)
    suspend fun getAssetById(id: Long): Asset? {
        val asset = assetRepository.findById(id).awaitSingleOrNull() ?: return null
        val assetTags = assetTagRepository.findByAssetIdIn(listOf(id)).toList()
        val tagIds = assetTags.mapNotNull { it.tagId }.distinct()
        val tags = tagRepository.findAllById(tagIds).collectList().awaitFirst().associateBy { it.id }

        assetTags.forEach { assetTag ->
            assetTag.tag = tags[assetTag.tagId]
        }
        asset.assetTags = assetTags.toSet()
        return asset
    }

    @Transactional(readOnly = true)
    suspend fun getAllTags(): List<Tag> = tagRepository.findAll().collectList().awaitFirst()

    @Transactional(readOnly = true)
    suspend fun getTagById(id: Long): Tag? = tagRepository.findById(id).awaitSingleOrNull()

    @Transactional
    suspend fun createAsset(asset: Asset, tagIds: List<Long>): Asset {
        val savedAsset = assetRepository.save(asset).awaitFirst()
        if (tagIds.isNotEmpty()) {
            val assetTags = tagIds.map { tagId -> AssetTag(assetId = savedAsset.id!!, tagId = tagId) }
            assetTagRepository.saveAll(assetTags).collectList().awaitFirst()
        }
        return savedAsset
    }

    @Transactional
    suspend fun updateAsset(id: Long, assetDetails: Asset, tagIds: List<Long>): Asset? {
        val existingAsset = assetRepository.findById(id).awaitSingleOrNull() ?: return null
        existingAsset.name = assetDetails.name
        existingAsset.metaFileUrl = assetDetails.metaFileUrl
        val savedAsset = assetRepository.save(existingAsset).awaitFirst()

        assetTagRepository.deleteByAssetId(id)
        if (tagIds.isNotEmpty()) {
            val assetTags = tagIds.map { tagId -> AssetTag(assetId = savedAsset.id!!, tagId = tagId) }
            assetTagRepository.saveAll(assetTags).collectList().awaitFirst()
        }

        return savedAsset
    }

    @Transactional
    suspend fun createTag(tag: Tag): Tag = tagRepository.save(tag).awaitFirst()

    @Transactional
    suspend fun updateTag(id: Long, tagDetails: Tag): Tag? {
        val existingTag = tagRepository.findById(id).awaitSingleOrNull() ?: return null
        existingTag.name = tagDetails.name
        return tagRepository.save(existingTag).awaitFirst()
    }

    @Transactional
    suspend fun deleteAsset(id: Long) {
        assetTagRepository.deleteByAssetId(id)
        assetRepository.deleteById(id).awaitSingleOrNull()
    }

    @Transactional
    suspend fun deleteTag(id: Long) {
        assetTagRepository.deleteByTagId(id)
        tagRepository.deleteById(id).awaitSingleOrNull()
    }
}