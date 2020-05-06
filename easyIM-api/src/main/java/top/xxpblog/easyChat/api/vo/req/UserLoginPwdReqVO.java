package top.xxpblog.easyChat.api.vo.req;

import top.xxpblog.easyChat.common.vo.req.BaseReqVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 密码登录
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserLoginPwdReqVO extends BaseReqVO {
    
    @NotNull(message = "用户ID不能为空")

    private Long uid;


    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    private String pwd;
    
}
