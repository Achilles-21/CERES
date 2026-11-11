package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ceres.common.constant.CacheConstants;
import com.ceres.common.constant.HttpStatus;
import com.ceres.common.core.domain.entity.SysRole;
import com.ceres.common.core.domain.entity.SysUser;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.system.domain.Invitation;
import com.ceres.system.domain.InvitationPicture;
import com.ceres.system.domain.SysUserRole;
import com.ceres.system.domain.vo.req.InvitationLikeReq;
import com.ceres.system.domain.vo.req.InvitationReq;
import com.ceres.system.domain.vo.resp.InvitationResp;
import com.ceres.system.mapper.*;
import com.ceres.system.service.IInvitationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvitationServiceImpl extends ServiceImpl<InvitationMapper, Invitation> implements IInvitationService {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private InvitationPictureMapper invitationPictureMapper;
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;
    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private InvitationMapper invitationMapper;

    @Override
    public PageResult pageQuery(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        Page<InvitationResp> respPage = invitationMapper.pageQuery();
        List<InvitationResp> invitationRespList = respPage.getResult();
        if(CollUtil.isEmpty(invitationRespList)){
            return new PageResult(ListUtil.empty(), respPage.getTotal(), respPage.getPageNum(), respPage.getPageSize());
        }
        List<Long> invitationIds = invitationRespList.stream().map(InvitationResp::getId).collect(Collectors.toList());
        Set<Long> likedIdSet = likedList(invitationIds);
        LambdaQueryWrapper<InvitationPicture> wrapper = new LambdaQueryWrapper<InvitationPicture>()
                .in(InvitationPicture::getInvitationId, invitationIds)
                .eq(InvitationPicture::getIsDeleted, 0);
        List<InvitationPicture> invitationPictureList = invitationPictureMapper.selectList(wrapper);
        Map<Long, List<InvitationPicture>> InvitationPictureMap = invitationPictureList.stream().collect(Collectors.groupingBy(InvitationPicture::getInvitationId));
        for (InvitationResp invitationResp : invitationRespList) {
            Double score = stringRedisTemplate.opsForZSet().score(CacheConstants.INVITATION_LIKES_KEY, invitationResp.getId().toString());
            if(score!=null){
                invitationResp.setLikes(score.intValue());
            }
            invitationResp.setIsLiked(likedIdSet.contains(invitationResp.getId()));
            List<InvitationPicture> invitationPictures = InvitationPictureMap.get(invitationResp.getId());
            if (CollUtil.isNotEmpty(invitationPictures)) {
                List<String> pictureUrlS = invitationPictures.stream().map(InvitationPicture::getPictureUrl).collect(Collectors.toList());
                invitationResp.setPictureList(pictureUrlS);
            }
        }
        return new PageResult(invitationRespList, respPage.getTotal(), respPage.getPageNum(), respPage.getPageSize());
    }

    @Override
    @Transactional
    public void add(InvitationReq invitationReq) {
        LocalDateTime now = LocalDateTime.now();
        Invitation invitation = BeanUtil.toBean(invitationReq, Invitation.class);
        invitation.setUserId(SecurityUtils.getUserId());
        invitation.setCreateTime(now);
        save(invitation);
        List<InvitationPicture> invitationPictureList = new ArrayList<>();
        List<String> pictureList = invitationReq.getPictureList();
        if (CollUtil.isNotEmpty(pictureList)) {
            for (String pictureUrl : pictureList) {
                InvitationPicture invitationPicture = new InvitationPicture();
                invitationPicture.setInvitationId(invitation.getId());
                invitationPicture.setPictureUrl(pictureUrl);
                invitationPicture.setCreateTime(now);
                invitationPictureList.add(invitationPicture);
            }
            invitationPictureMapper.saveBatchList(invitationPictureList);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LambdaUpdateWrapper<Invitation> lambdaUpdateWrapper = new LambdaUpdateWrapper<Invitation>().eq(Invitation::getId, id)
                .eq(Invitation::getIsDeleted, 0).eq(SecurityUtils.getUserId() != 1, Invitation::getUserId, SecurityUtils.getUserId())
                .set(Invitation::getIsDeleted, 1);
        boolean update = update(lambdaUpdateWrapper);
        if (update) {
            LambdaUpdateWrapper<InvitationPicture> updateWrapper = new LambdaUpdateWrapper<InvitationPicture>()
                    .eq(InvitationPicture::getInvitationId, id).eq(InvitationPicture::getIsDeleted, 0).set(InvitationPicture::getIsDeleted, 1);
            invitationPictureMapper.update(null, updateWrapper);
        } else {
            throw new ServiceException(MessageUtils.message("user.no.delete.permission"), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public InvitationResp selectById(Long id) {
        Invitation invitation = getById(id);
        InvitationResp invitationResp = BeanUtil.toBean(invitation, InvitationResp.class);
        SysUser sysUser = sysUserMapper.selectUserById(invitation.getUserId());
        invitationResp.setNickName(sysUser.getNickName());
        invitationResp.setAvatar(sysUser.getAvatar());
        Double score = stringRedisTemplate.opsForZSet().score(CacheConstants.INVITATION_LIKES_KEY, invitation.getId().toString());
        if(score!=null){
            invitationResp.setLikes(score.intValue());
        }
        Set<Long> likedIdSet = likedList(ListUtil.of(id));
        invitationResp.setIsLiked(likedIdSet.contains(invitation.getId()));
        SysUserRole role = sysUserRoleMapper.selectUserRoleById(invitation.getUserId());
        SysRole sysRole = roleMapper.selectById(role.getRoleId());
        if(ObjUtil.isNotEmpty(sysRole)){
            invitationResp.setRoleName(sysRole.getRoleName());
        }
        LambdaQueryWrapper<InvitationPicture> lambdaQueryWrapper = new LambdaQueryWrapper<InvitationPicture>().eq(InvitationPicture::getInvitationId, id)
                .eq(InvitationPicture::getIsDeleted, 0);
        List<InvitationPicture> invitationPictureList = invitationPictureMapper.selectList(lambdaQueryWrapper);
        if (CollUtil.isNotEmpty(invitationPictureList)) {
            List<String> pictureList = new ArrayList<>();
            for (InvitationPicture invitationPicture : invitationPictureList) {
                pictureList.add(invitationPicture.getPictureUrl());
            }
            invitationResp.setPictureList(pictureList);
        }
        return invitationResp;
    }

    @Override
    public void likes(InvitationLikeReq likeReq) {
        boolean result;
        if(likeReq.getIsLiked()){
            result = liked(likeReq);
        }else {
            result = unliked(likeReq);
        }
        if(!result){
            return;
        }
        Long size = stringRedisTemplate.opsForSet().size(CacheConstants.LIKED_RECORD_KEY + likeReq.getId());
        if(size==null){
            return;
        }
        stringRedisTemplate.opsForZSet().add(CacheConstants.INVITATION_LIKES_KEY, likeReq.getId().toString(), size.doubleValue());
    }

    private boolean unliked(InvitationLikeReq likeReq) {
        Long count = stringRedisTemplate.opsForSet().remove(CacheConstants.LIKED_RECORD_KEY + likeReq.getId(), SecurityUtils.getUserId().toString());
        return count!=null && count>0;
    }

    private boolean liked(InvitationLikeReq likeReq) {
        Long count = stringRedisTemplate.opsForSet().add(CacheConstants.LIKED_RECORD_KEY + likeReq.getId(), SecurityUtils.getUserId().toString());
        return count!=null && count>0;
    }

    @Override
    public Set<Long> likedList(List<Long> invitationIds) {
        String userId = SecurityUtils.getUserId().toString();
        List<Object> resultList = stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            StringRedisConnection src = (StringRedisConnection) connection;
            for (Long invitationId : invitationIds) {
                String key = CacheConstants.LIKED_RECORD_KEY + invitationId;
                src.sIsMember(key, userId);
            }
            return null;
        });
        Set<Long> set = new HashSet<>();
        for (int i = 0; i < resultList.size(); i++) {
            Boolean result = (Boolean) resultList.get(i);
            if (BooleanUtils.isTrue(result)) {
                set.add(invitationIds.get(i));
            }
        }
        return set;
    }

}
