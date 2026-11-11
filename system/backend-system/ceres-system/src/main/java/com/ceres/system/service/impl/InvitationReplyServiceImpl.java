package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ceres.common.constant.HttpStatus;
import com.ceres.common.core.domain.entity.SysUser;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.system.domain.Invitation;
import com.ceres.system.domain.InvitationReply;
import com.ceres.system.domain.InvitationReplyPicture;
import com.ceres.system.domain.vo.req.InvitationReplyReq;
import com.ceres.system.domain.vo.resp.InvitationReplyCommentResp;
import com.ceres.system.domain.vo.resp.InvitationReplyResp;
import com.ceres.system.mapper.InvitationMapper;
import com.ceres.system.mapper.InvitationReplyMapper;
import com.ceres.system.mapper.InvitationReplyPictureMapper;
import com.ceres.system.mapper.SysUserMapper;
import com.ceres.system.service.IInvitationReplyService;
import com.ceres.system.service.support.InvitationReplyDeletePolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvitationReplyServiceImpl extends ServiceImpl<InvitationReplyMapper, InvitationReply> implements IInvitationReplyService {

    @Resource
    private InvitationReplyPictureMapper replyPictureMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private InvitationMapper invitationMapper;

    @Override
    @Transactional
    public InvitationReplyResp add(InvitationReplyReq replyReq) {
        // Return the assembled reply so the frontend can update without reloading.
        InvitationReplyResp replyResp = BeanUtil.toBean(replyReq, InvitationReplyResp.class);
        InvitationReply reply = BeanUtil.toBean(replyReq, InvitationReply.class);
        reply.setUserId(SecurityUtils.getUserId());
        reply.setCreateTime(LocalDateTime.now());
        save(reply);
        replyResp.setId(reply.getId());
        SysUser user = userMapper.getUserById(SecurityUtils.getUserId());
        replyResp.setUserId(user.getUserId());
        replyResp.setNickName(user.getNickName());
        if(StrUtil.isNotBlank(user.getAvatar())){
            replyResp.setAvatar(user.getAvatar());
        }
        if(replyReq.getInvitationReplyId()==null){
            InvitationReply invitationReply = getById(reply.getId());
            if(invitationReply.getNumberComments()!=null){
                replyResp.setNumberComments(invitationReply.getNumberComments());
            }
        }else {
            replyResp.setNumberComments(0);
            SysUser sysUser = userMapper.getUserById(replyReq.getTargetUserId());
            replyResp.setTargetUserName(sysUser.getNickName());
        }
        List<String> replyPictureReqList = replyReq.getReplyPictureList();
        if (CollUtil.isNotEmpty(replyPictureReqList)) {
            List<InvitationReplyPicture> replyPictureList = new ArrayList<>();
            for (String picUrl : replyPictureReqList) {
                InvitationReplyPicture replyPicture = new InvitationReplyPicture();
                replyPicture.setInvitationReplyId(reply.getId());
                replyPicture.setReplyPictureUrl(picUrl);
                replyPicture.setCreateTime(LocalDateTime.now());
                replyPictureList.add(replyPicture);
            }
            replyPictureMapper.saveBatchList(replyPictureList);
        }
        LambdaUpdateWrapper<Invitation> updateWrapper = new LambdaUpdateWrapper<Invitation>().eq(Invitation::getId, replyReq.getInvitationId())
                .eq(Invitation::getIsDeleted, 0).setSql("comments = comments + 1");
        invitationMapper.update(null, updateWrapper);
        if (replyReq.getInvitationReplyId() != null) {
            lambdaUpdate().eq(InvitationReply::getId, replyReq.getInvitationReplyId())
                    .eq(InvitationReply::getIsDeleted, 0).setSql("number_comments = number_comments + 1").update();
        }
        return replyResp;
    }


    @Override
    @Transactional
    public void delete(Long id) {
        InvitationReply reply = getById(id);
        Long currentUserId = SecurityUtils.getUserId();
        if (reply == null || Objects.equals(reply.getIsDeleted(), 1) || !InvitationReplyDeletePolicy.canDelete(currentUserId, reply.getUserId())) {
            throw new ServiceException(MessageUtils.message("user.no.delete.permission"), HttpStatus.BAD_REQUEST);
        }

        List<Long> deletedReplyIds = new ArrayList<>();
        deletedReplyIds.add(id);
        if (reply.getInvitationReplyId() == null) {
            List<InvitationReply> childReplies = list(new LambdaQueryWrapper<InvitationReply>()
                    .eq(InvitationReply::getInvitationReplyId, id)
                    .eq(InvitationReply::getIsDeleted, 0));
            if (CollUtil.isNotEmpty(childReplies)) {
                deletedReplyIds.addAll(childReplies.stream().map(InvitationReply::getId).collect(Collectors.toList()));
            }
        }

        boolean updated = lambdaUpdate()
                .in(InvitationReply::getId, deletedReplyIds)
                .eq(InvitationReply::getIsDeleted, 0)
                .set(InvitationReply::getIsDeleted, 1)
                .update();
        if (!updated) {
            throw new ServiceException(MessageUtils.message("user.no.delete.permission"), HttpStatus.BAD_REQUEST);
        }

        LambdaUpdateWrapper<InvitationReplyPicture> pictureUpdateWrapper = new LambdaUpdateWrapper<InvitationReplyPicture>()
                .in(InvitationReplyPicture::getInvitationReplyId, deletedReplyIds)
                .eq(InvitationReplyPicture::getIsDeleted, 0)
                .set(InvitationReplyPicture::getIsDeleted, 1);
        replyPictureMapper.update(null, pictureUpdateWrapper);

        int deletedCount = deletedReplyIds.size();
        LambdaUpdateWrapper<Invitation> invitationUpdateWrapper = new LambdaUpdateWrapper<Invitation>()
                .eq(Invitation::getId, reply.getInvitationId())
                .eq(Invitation::getIsDeleted, 0)
                .setSql("comments = CASE WHEN comments >= " + deletedCount + " THEN comments - " + deletedCount + " ELSE 0 END");
        invitationMapper.update(null, invitationUpdateWrapper);

        if (reply.getInvitationReplyId() != null) {
            lambdaUpdate()
                    .eq(InvitationReply::getId, reply.getInvitationReplyId())
                    .eq(InvitationReply::getIsDeleted, 0)
                    .setSql("number_comments = CASE WHEN number_comments >= 1 THEN number_comments - 1 ELSE 0 END")
                    .update();
        }
    }

    @Override
    public PageResult pageQuery(Integer pageNum, Integer pageSize, Long invitationId) {
        Page<InvitationReply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InvitationReply> queryWrapper = new LambdaQueryWrapper<InvitationReply>().eq(InvitationReply::getInvitationId, invitationId)
                .isNull(InvitationReply::getInvitationReplyId).eq(InvitationReply::getIsDeleted, 0).orderByDesc(InvitationReply::getCreateTime);
        Page<InvitationReply> replyPage = page(page, queryWrapper);
        List<InvitationReply> replyList = replyPage.getRecords();
        if (CollUtil.isEmpty(replyList)) {
            return new PageResult(ListUtil.empty(), replyPage.getTotal(), (int) replyPage.getCurrent(), (int) replyPage.getSize());
        }
        List<Long> replyIds = replyList.stream().map(InvitationReply::getId).collect(Collectors.toList());
        LambdaQueryWrapper<InvitationReply> lambdaQueryWrapper = new LambdaQueryWrapper<InvitationReply>()
                .in(InvitationReply::getInvitationReplyId, replyIds).eq(InvitationReply::getIsDeleted, 0);
        List<InvitationReply> replies = list(lambdaQueryWrapper);
        Set<Long> userIds = replyList.stream().map(InvitationReply::getUserId).collect(Collectors.toSet());
        Set<Long> uIds = replies.stream().map(InvitationReply::getUserId).collect(Collectors.toSet());
        userIds.addAll(uIds);
        List<SysUser> userList = userMapper.getUserByIds(userIds);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
        List<Long> replyIdList = replies.stream().map(InvitationReply::getId).collect(Collectors.toList());
        replyIds.addAll(replyIdList);
        LambdaQueryWrapper<InvitationReplyPicture> wrapper = new LambdaQueryWrapper<InvitationReplyPicture>()
                .in(InvitationReplyPicture::getInvitationReplyId, replyIds)
                .eq(InvitationReplyPicture::getIsDeleted, 0);
        List<InvitationReplyPicture> replyPictureList = replyPictureMapper.selectList(wrapper);
        Map<Long, List<InvitationReplyPicture>> replyPictureMap = null;
        if (CollUtil.isNotEmpty(replyPictureList)) {
            replyPictureMap = replyPictureList.stream().collect(Collectors.groupingBy(InvitationReplyPicture::getInvitationReplyId));
        }
        List<InvitationReplyResp> replyRespList = new ArrayList<>();

        for (InvitationReply reply : replyList) {
            InvitationReplyResp replyResp = BeanUtil.toBean(reply, InvitationReplyResp.class);
            SysUser user = userMap.get(replyResp.getUserId());
            replyResp.setNickName(user.getNickName());
            replyResp.setAvatar(user.getAvatar());
            if (CollUtil.isNotEmpty(replyPictureList)) {
                List<InvitationReplyPicture> pictures = replyPictureMap.get(replyResp.getId());
                if(CollUtil.isNotEmpty(pictures)){
                    List<String> picUrlList = pictures.stream().map(InvitationReplyPicture::getReplyPictureUrl).collect(Collectors.toList());
                    replyResp.setReplyPictureList(picUrlList);
                }
            }
            replyRespList.add(replyResp);
        }
        HashMap<Long, Integer> map = new HashMap<>();
        List<InvitationReplyResp> replyResps = new ArrayList<>();
        for (InvitationReply reply : replies) {
            Long id = reply.getInvitationReplyId();
            map.merge(id, 1, Integer::sum);
            if(map.get(id)>=3){
                continue;
            }
            InvitationReplyResp replyResp = BeanUtil.toBean(reply, InvitationReplyResp.class);
            SysUser user = userMap.get(replyResp.getUserId());
            replyResp.setNickName(user.getNickName());
            if (CollUtil.isNotEmpty(replyPictureList)) {
                List<InvitationReplyPicture> pictures = replyPictureMap.get(replyResp.getId());
                if(CollUtil.isNotEmpty(pictures)) {
                    List<String> picUrlList = pictures.stream().map(InvitationReplyPicture::getReplyPictureUrl).collect(Collectors.toList());
                    replyResp.setReplyPictureList(picUrlList);
                }
            }
            SysUser targetUser = userMap.get(replyResp.getTargetUserId());
            if(ObjUtil.isNotEmpty(targetUser)){
                replyResp.setTargetUserName(targetUser.getNickName());
            }
            replyResps.add(replyResp);
        }
        Map<Long, List<InvitationReplyResp>> listMap = replyResps.stream().collect(Collectors.groupingBy(InvitationReplyResp::getInvitationReplyId));
        replyRespList.forEach(r->{
            if(CollUtil.isNotEmpty(listMap.get(r.getId()))){
                r.setReplyRespList(listMap.get(r.getId()));
            }
        });



        return new PageResult(replyRespList, replyPage.getTotal(), (int) replyPage.getCurrent(), (int) replyPage.getSize());
    }

    @Override
    public PageResult listComment(Integer pageNum, Integer pageSize, Long invitationReplyId) {
        InvitationReply reply = getById(invitationReplyId);
        Page<InvitationReply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InvitationReply> wrapper = new LambdaQueryWrapper<InvitationReply>()
                .eq(InvitationReply::getInvitationReplyId, invitationReplyId).eq(InvitationReply::getIsDeleted, 0);
        Page<InvitationReply> replyCommentPage = page(page, wrapper);
        List<InvitationReply> replyCommentList = replyCommentPage.getRecords();
        if(CollUtil.isEmpty(replyCommentList)){
            return new PageResult(ListUtil.empty(), replyCommentPage.getTotal(), (int) replyCommentPage.getCurrent(), (int) replyCommentPage.getSize());
        }
        Set<Long> userIds = replyCommentList.stream().map(InvitationReply::getUserId).collect(Collectors.toSet());
        Set<Long> uIds = replyCommentList.stream().map(InvitationReply::getTargetUserId).collect(Collectors.toSet());
        userIds.addAll(uIds);
        List<SysUser> userList = userMapper.getUserByIds(userIds);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
        List<Long> commentIds = replyCommentList.stream().map(InvitationReply::getId).collect(Collectors.toList());
        commentIds.add(invitationReplyId);
        LambdaQueryWrapper<InvitationReplyPicture> queryWrapper = new LambdaQueryWrapper<InvitationReplyPicture>()
                .in(InvitationReplyPicture::getInvitationReplyId, commentIds)
                .eq(InvitationReplyPicture::getIsDeleted, 0);
        List<InvitationReplyPicture> replyPictureList = replyPictureMapper.selectList(queryWrapper);
        Map<Long, List<InvitationReplyPicture>> picMap = replyPictureList.stream().collect(Collectors.groupingBy(InvitationReplyPicture::getInvitationReplyId));
        InvitationReplyCommentResp replyCommentResp = BeanUtil.toBean(reply, InvitationReplyCommentResp.class);
        SysUser user = userMap.get(reply.getUserId());
        replyCommentResp.setNickName(user.getNickName());
        replyCommentResp.setAvatar(user.getAvatar());
        List<InvitationReplyPicture> pictures = picMap.get(invitationReplyId);
        if(CollUtil.isNotEmpty(pictures)){
            List<String> urlList = pictures.stream().map(InvitationReplyPicture::getReplyPictureUrl).collect(Collectors.toList());
            replyCommentResp.setReplyPictureList(urlList);
        }
        List<InvitationReplyCommentResp> commentRespList = new ArrayList<>();
        for (InvitationReply invitationReply : replyCommentList) {
            InvitationReplyCommentResp commentResp = BeanUtil.toBean(invitationReply, InvitationReplyCommentResp.class);
            SysUser sysUser = userMap.get(invitationReply.getUserId());
            commentResp.setNickName(sysUser.getNickName());
            SysUser u = userMap.get(invitationReply.getTargetUserId());
            commentResp.setTargetUserName(u.getNickName());
            List<InvitationReplyPicture> pictureList = picMap.get(invitationReply.getId());
            if(CollUtil.isNotEmpty(pictureList)){
                List<String> urls = pictureList.stream().map(InvitationReplyPicture::getReplyPictureUrl).collect(Collectors.toList());
                commentResp.setReplyPictureList(urls);
            }
            commentRespList.add(commentResp);
        }
        replyCommentResp.setReplyCommentRespList(commentRespList);
        return new PageResult(ListUtil.of(replyCommentResp), replyCommentPage.getTotal(), (int) replyCommentPage.getCurrent(), (int) replyCommentPage.getSize());
    }

}
