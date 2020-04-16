package top.xxpblog.easyChat.api.vo.req;

import top.xxpblog.easyChat.common.vo.req.BaseReqVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 删除好友
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFriendMsgClearMsgCountReqVO extends BaseReqVO {
    
    /**
     * 消息接收方
     */
    @NotNull(message = "参数错误~")
    @Min(value = 1, message = "参数错误~")
    private Long receiverUid;

}
