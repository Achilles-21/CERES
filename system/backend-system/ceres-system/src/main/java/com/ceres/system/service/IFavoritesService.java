package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;

public interface IFavoritesService {

    void addFavorites(Long queId);

    void deleteFavorites(Long queId);

    PageResult getFavoritesList(Integer pageNum, Integer pageSize);

}
