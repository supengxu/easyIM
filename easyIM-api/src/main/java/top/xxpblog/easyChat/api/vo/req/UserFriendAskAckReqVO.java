package top.xxpblog.easyChat.api.vo.req;

import top.xxpblog.easyChat.common.vo.req.BaseReqVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 发起好友请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFriendAskAckReqVO extends BaseReqVO {

    /**
     * 朋友的ID
     */
    @NotNull(message = "参数错误！")
    private Long id;

    /**
     * 状态
     */
    private Integer status;

}
