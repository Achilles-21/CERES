package com.ceres.system.domain.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class InvitationReq {

    private String content;

    private List<String> pictureList;

}
