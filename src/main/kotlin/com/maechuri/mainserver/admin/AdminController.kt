package com.maechuri.mainserver.admin

import com.maechuri.mainserver.game.entity.Asset
import com.maechuri.mainserver.game.entity.Tag
import com.maechuri.mainserver.game.service.AssetService
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
class AdminController(private val assetService: AssetService) {

    @GetMapping
    fun index(): String {
        return "admin/index"
    }

    @GetMapping("/assets")
    fun assets(model: Model): String = runBlocking {
        model.addAttribute("assets", assetService.getAllAssets())
        "admin/assets"
    }

    @GetMapping("/tags")
    fun tags(model: Model): String = runBlocking {
        model.addAttribute("tags", assetService.getAllTags())
        "admin/tags"
    }

    @GetMapping("/assets/new")
    fun newAsset(model: Model): String = runBlocking {
        model.addAttribute("asset", Asset(name = "", metaFileUrl = ""))
        model.addAttribute("allTags", assetService.getAllTags())
        model.addAttribute("selectedTagIds", emptyList<Long>())
        "admin/new-asset"
    }

    @PostMapping("/assets")
    fun createAsset(
        @ModelAttribute asset: Asset,
        @RequestParam(name = "tags", required = false) tagIds: List<Long>?,
        model: Model
    ): String = runBlocking {
        val finalTagIds = tagIds ?: emptyList()
        try {
            assetService.createAsset(asset, finalTagIds)
            "redirect:/admin/assets"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Asset name already exists.")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", finalTagIds)
            "admin/new-asset"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", finalTagIds)
            "admin/new-asset"
        }
    }

    @GetMapping("/assets/{id}/edit")
    fun editAsset(@PathVariable id: Long, model: Model): String = runBlocking {
        val asset = assetService.getAssetById(id)
        model.addAttribute("asset", asset)
        model.addAttribute("allTags", assetService.getAllTags())
        model.addAttribute("selectedTagIds", asset?.assetTags?.mapNotNull { it.tagId } ?: emptyList<Long>())
        "admin/edit-asset"
    }

    @PostMapping("/assets/{id}")
    fun updateAsset(
        @PathVariable id: Long,
        @ModelAttribute asset: Asset,
        request: HttpServletRequest,
        model: Model
    ): String = runBlocking {
        val tagIds: List<Long> = request.getParameterValues("tags")
            ?.mapNotNull { it.toLongOrNull() }
            ?: emptyList()
        try {
            assetService.updateAsset(id, asset, tagIds)
            "redirect:/admin/assets"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Asset name already exists.")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", tagIds)
            "admin/edit-asset"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("asset", asset)
            model.addAttribute("allTags", assetService.getAllTags())
            model.addAttribute("selectedTagIds", tagIds)
            "admin/edit-asset"
        }
    }

    @GetMapping("/tags/new")
    fun newTag(model: Model): String {
        model.addAttribute("tag", Tag(name = ""))
        return "admin/new-tag"
    }

    @PostMapping("/tags")
    fun createTag(
        @ModelAttribute tag: Tag,
        model: Model
    ): String = runBlocking {
        try {
            assetService.createTag(tag)
            "redirect:/admin/tags"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Tag name already exists.")
            model.addAttribute("tag", tag)
            "admin/new-tag"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("tag", tag)
            "admin/new-tag"
        }
    }

    @GetMapping("/tags/{id}/edit")
    fun editTag(@PathVariable id: Long, model: Model): String = runBlocking {
        model.addAttribute("tag", assetService.getTagById(id))
        "admin/edit-tag"
    }

    @PostMapping("/tags/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @ModelAttribute tag: Tag,
        model: Model
    ): String = runBlocking {
        try {
            assetService.updateTag(id, tag)
            "redirect:/admin/tags"
        } catch (e: DataIntegrityViolationException) {
            model.addAttribute("error", "Tag name already exists.")
            model.addAttribute("tag", tag) // Keep the user's input
            "admin/edit-tag"
        } catch (e: Exception) {
            model.addAttribute("error", "An unexpected error occurred: ${e.message}")
            model.addAttribute("tag", tag)
            "admin/edit-tag"
        }
    }

    @PostMapping("/assets/{id}/delete")
    fun deleteAsset(@PathVariable id: Long): String = runBlocking {
        assetService.deleteAsset(id)
        "redirect:/admin/assets"
    }

    @PostMapping("/tags/{id}/delete")
    fun deleteTag(@PathVariable id: Long): String = runBlocking {
        assetService.deleteTag(id)
        "redirect:/admin/tags"
    }
}
