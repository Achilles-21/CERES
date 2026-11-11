package com.ceres.system.domain.vo.resp.openApi;

import lombok.Data;

import java.util.List;

@Data
public class ChatHistoryResp {

    private Long user_id;

    private List<ConversationsResp> conversations;

}
