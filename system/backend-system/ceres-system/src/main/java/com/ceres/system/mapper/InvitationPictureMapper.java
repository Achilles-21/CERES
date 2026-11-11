package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.InvitationPicture;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InvitationPictureMapper extends BaseMapper<InvitationPicture> {

    void saveBatchList(@Param("invitationPictureList") List<InvitationPicture> invitationPictureList);

}
