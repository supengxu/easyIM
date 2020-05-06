package top.xxpblog.easyChat.api.controller.user;


import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.req.UserLoginPwdReqVO;
import top.xxpblog.easyChat.api.vo.req.UserRegisterReqVO;
import top.xxpblog.easyChat.api.vo.res.UserLoginResVO;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.utils.PasswordUtils;
import top.xxpblog.easyChat.common.entity.user.User;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 登录控制器
 */
@RequestMapping("/user/login")
@RestController
public class UserLoginController {

    @Resource
    private UserService userService;


    @ApiOperation("账号密码登陆")
    @PostMapping("/loginByPassword")
    public BaseResVO byPwd(@Valid @RequestBody UserLoginPwdReqVO userLoginPwdReqVO,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }

        Long uid = userLoginPwdReqVO.getUid();
        String userName = userLoginPwdReqVO.getPwd();
        User user = userService.findPwdByUid(uid);
        String md5Pwd = PasswordUtils.md52md5(userLoginPwdReqVO.getPwd());
        if (user == null || !md5Pwd.equals(user.getPwd())) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "密码或用户名错误~");
        }
        String token = UserLoginUtils.createSid(uid);

        UserLoginResVO userLoginResVO = new UserLoginResVO();
        userLoginResVO.setUid(uid);
        userLoginResVO.setSid(token);
        return ResultVOUtils.success(userLoginResVO);
    }

    @ApiOperation("游客登陆")
    @PostMapping("/byTourist")
    public BaseResVO byTourist(@RequestParam(value = "uid") Long uid) {
        //TODO 检验ID 是否存在该用户
        //TODO 游客登陆存在漏洞---不使用账号密码即可登陆，这样是绝对不可以的。
        User user = userService.findByUid(uid);
        if (user==null){
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "该用户不存在");
        }

        uid = uid==null?13L:uid;
        String token = UserLoginUtils.createSid(uid);

        UserLoginResVO userLoginResVO = new UserLoginResVO();
        userLoginResVO.setUid(uid);
        userLoginResVO.setSid(token);
        return ResultVOUtils.success(userLoginResVO);
    }
    @ApiOperation("注册账号")
    @PostMapping("/register")
    public BaseResVO RegisterUserByPasswordAndUserName(@Valid @RequestBody UserRegisterReqVO userRegister,
                                                       BindingResult bindingResult){
        // 检测账号密码
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }
        User user = new User();
        user.setName(userRegister.getUserName());
        user.setPwd(PasswordUtils.md52md5(userRegister.getPwd()));
        user.setRemark("用户很懒，啥都没有写");
        user.setAvatar("");
         userService.insertUserAndReturnId(user);
        long uid = user.getUid();
        String token = UserLoginUtils.createSid(uid);

        UserLoginResVO userLoginResVO = new UserLoginResVO();
        userLoginResVO.setUid(uid);
        userLoginResVO.setSid(token);
        return ResultVOUtils.success(userLoginResVO);
    }

}
