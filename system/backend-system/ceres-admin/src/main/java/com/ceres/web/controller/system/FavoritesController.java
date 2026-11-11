package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.IFavoritesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("favorites")
public class FavoritesController {

    @Resource
    private IFavoritesService favoritesService;

    @Log(title = "收藏题目")
    @GetMapping("addFavorites")
    public AjaxResult addFavorites(@RequestParam("queId") Long queId) {
        favoritesService.addFavorites(queId);
        return AjaxResult.success();
    }

    @Log(title = "获取收藏题目")
    @GetMapping("getFavoritesList")
    public AjaxResult getFavoritesList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        return AjaxResult.success(favoritesService.getFavoritesList(pageNum, pageSize));
    }

    @Log(title = "取消收藏题目")
    @GetMapping("deleteFavorites")
    public AjaxResult deleteFavorites(@RequestParam("queId") Long queId) {
        favoritesService.deleteFavorites(queId);
        return AjaxResult.success();
    }

}
