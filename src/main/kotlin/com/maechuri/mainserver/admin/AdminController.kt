package com.maechuri.mainserver.admin

import com.maechuri.mainserver.game.entity.Asset
import com.maechuri.mainserver.game.entity.Tag
import com.maechuri.mainserver.game.service.AssetService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@Controller
@RequestMapping("/admin")
class AdminController(private val assetService: AssetService) {

    @GetMapping
    fun index(): String {
        return "admin/index"
    }

    @GetMapping("/assets")
    suspend fun assets(model: Model): String {
        model.addAttribute("assets", assetService.getAllAssets())
        return "admin/assets"
    }

    @GetMapping("/tags")
    suspend fun tags(model: Model): String {
        model.addAttribute("tags", assetService.getAllTags())
        return "admin/tags"
    }

    @GetMapping("/assets/new")
    suspend fun newAsset(model: Model): String {
        model.addAttribute("asset", Asset(name = "", metaFileUrl = ""))
        model.addAttribute("allTags", assetService.getAllTags())
        model.addAttribute("selectedTagIds", emptyList<Long>())
        return "admin/new-asset"
    }

    @PostMapping("/assets")
    suspend fun createAsset(
        @ModelAttribute asset: Asset,
        @RequestParam(name = "tags", required = false) tagIds: List<Long>?,
        model: Model
    ): String {
        val finalTagIds = tagIds ?: emptyList()
        try {
            assetService.createAsset(asset, finalTagIds)
            return "redirect:/admin/assets"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Asset name already exists.")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", finalTagIds)
            return "admin/new-asset"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", finalTagIds)
            return "admin/new-asset"
        }
    }

    @GetMapping("/assets/{id}/edit")
    suspend fun editAsset(@PathVariable id: Long, model: Model): String {
        val asset = assetService.getAssetById(id)
        model.addAttribute("asset", asset)
        model.addAttribute("allTags", assetService.getAllTags())
        model.addAttribute("selectedTagIds", asset?.assetTags?.mapNotNull { it.tagId } ?: emptyList<Long>())
        return "admin/edit-asset"
    }

    @PostMapping("/assets/{id}")
    suspend fun updateAsset(
        @PathVariable id: Long,
        @ModelAttribute asset: Asset,
        exchange: ServerWebExchange,
        model: Model
    ): String {
        val tagIds = exchange.formData.awaitSingle().get("tags")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
        try {
            assetService.updateAsset(id, asset, tagIds)
            return "redirect:/admin/assets"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Asset name already exists.")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", tagIds)
            return "admin/edit-asset"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", tagIds)
            return "admin/edit-asset"
        }
    }

    @GetMapping("/tags/new")
    fun newTag(model: Model): String {
        model.addAttribute("tag", Tag(name = ""))
        return "admin/new-tag"
    }

    @PostMapping("/tags")
    suspend fun createTag(
        @ModelAttribute tag: Tag,
        model: Model
    ): String {
        try {
            assetService.createTag(tag)
            return "redirect:/admin/tags"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Tag name already exists.")
            model.addAttribute("tag", tag)
            return "admin/new-tag"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("tag", tag)
            return "admin/new-tag"
        }
    }

    @GetMapping("/tags/{id}/edit")
    suspend fun editTag(@PathVariable id: Long, model: Model): String {
        model.addAttribute("tag", assetService.getTagById(id))
        return "admin/edit-tag"
    }

    @PostMapping("/tags/{id}")
    suspend fun updateTag(
        @PathVariable id: Long,
        @ModelAttribute tag: Tag,
        model: Model
    ): String {
        try {
            assetService.updateTag(id, tag)
            return "redirect:/admin/tags"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Tag name already exists.")
            model.addAttribute("tag", tag) // Keep the user's input
            return "admin/edit-tag"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("tag", tag)
            return "admin/edit-tag"
        }
    }

    @PostMapping("/assets/{id}/delete")
    suspend fun deleteAsset(@PathVariable id: Long): String {
        assetService.deleteAsset(id)
        return "redirect:/admin/assets"
    }

    @PostMapping("/tags/{id}/delete")
    suspend fun deleteTag(@PathVariable id: Long): String {
        assetService.deleteTag(id)
       return  "redirect:/admin/tags"
    }
}
