package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ceres.system.domain.FavoritesQue;
import com.ceres.system.domain.vo.resp.FavoriteQuestionCardResp;
import org.apache.ibatis.annotations.Param;

public interface FavoritesQueMapper extends BaseMapper<FavoritesQue> {

    IPage<FavoriteQuestionCardResp> getFavoritesList(@Param("page") IPage<FavoriteQuestionCardResp> page, @Param("userId") Long userId);

}
