package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.InvitationReplyPicture;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InvitationReplyPictureMapper extends BaseMapper<InvitationReplyPicture> {

    void saveBatchList(@Param("replyPictureList") List<InvitationReplyPicture> replyPictureList);

}
