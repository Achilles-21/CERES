package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.Invitation;
import com.ceres.system.domain.vo.resp.InvitationResp;
import com.github.pagehelper.Page;

public interface InvitationMapper extends BaseMapper<Invitation> {

    Page<InvitationResp> pageQuery();

}
