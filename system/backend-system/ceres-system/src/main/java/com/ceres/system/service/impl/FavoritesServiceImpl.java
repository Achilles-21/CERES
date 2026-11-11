package com.ceres.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.StringUtils;
import com.ceres.system.domain.FavoritesQue;
import com.ceres.system.domain.vo.resp.FavoriteQuestionCardResp;
import com.ceres.system.mapper.FavoritesQueMapper;
import com.ceres.system.service.IFavoritesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class FavoritesServiceImpl implements IFavoritesService {

    @Resource
    private FavoritesQueMapper favoritesQueMapper;

    @Override
    public void addFavorites(Long queId) {
        LambdaQueryWrapper<FavoritesQue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoritesQue::getUserId, SecurityUtils.getUserId())
               .eq(FavoritesQue::getQueId, queId)
               .last("limit 1");
        FavoritesQue favoritesQue = favoritesQueMapper.selectOne(wrapper);
        if (StringUtils.isNotNull(favoritesQue)) {
            throw new ServiceException(MessageUtils.message("user.favorites.question"));
        }
        favoritesQueMapper.insert(new FavoritesQue(SecurityUtils.getUserId(), queId));
    }

    @Override
    public void deleteFavorites(Long queId) {
        LambdaQueryWrapper<FavoritesQue> wrapper = new LambdaQueryWrapper<>();
        favoritesQueMapper.delete(wrapper.eq(FavoritesQue::getUserId, SecurityUtils.getUserId())
                                         .eq(FavoritesQue::getQueId, queId));
    }

    @Override
    public PageResult getFavoritesList(Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        Page<FavoriteQuestionCardResp> page = new Page<>(safePageNum, safePageSize);
        IPage<FavoriteQuestionCardResp> result = favoritesQueMapper.getFavoritesList(page, SecurityUtils.getUserId());
        result.getRecords().forEach(FavoriteQuestionCardResp::refreshOptions);
        return new PageResult(result.getRecords(), result.getTotal(), safePageNum, safePageSize);
    }
}
