package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class InvitationResp {

    private Long id;

    private Long userId;

    private String nickName;

    private String avatar;

    private String content;

    private List<String> pictureList;

    private Integer likes = 0;

    private Integer comments;

    private String roleName;

    private Boolean isLiked;

}
