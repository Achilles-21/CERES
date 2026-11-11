package com.ceres.system.mapper;

import com.ceres.system.domain.SysNotice;

import java.util.List;

public interface SysNoticeMapper
{
    public SysNotice selectNoticeById(Long noticeId);

    public List<SysNotice> selectNoticeList(SysNotice notice);

    public int insertNotice(SysNotice notice);

    public int updateNotice(SysNotice notice);

    public int deleteNoticeById(Long noticeId);

    public int deleteNoticeByIds(Long[] noticeIds);
}
