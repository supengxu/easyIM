package top.xxpblog.easyChat.api.controller.user;


import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.service.other.QqWebAuthService;
import top.xxpblog.easyChat.api.service.user.UserQqService;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.req.UserLoginPwdReqVO;
import top.xxpblog.easyChat.api.vo.res.QqOpenIdResVO;
import top.xxpblog.easyChat.api.vo.res.QqUserInfoResVO;
import top.xxpblog.easyChat.api.vo.res.UserLoginResVO;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.utils.PasswordUtils;
import top.xxpblog.easyChat.common.entity.user.User;
import top.xxpblog.easyChat.common.entity.user.UserQq;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
    
    @Resource
    private QqWebAuthService qqWebAuthService;
    
    @Resource
    private UserQqService userQqService;
    
   @ApiOperation("账号密码登陆")
    @PostMapping("/byPwd")
    public BaseResVO byPwd(@Valid @RequestBody UserLoginPwdReqVO userLoginPwdReqVO,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }
        
        Long uid = userLoginPwdReqVO.getUid();
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
    @CrossOrigin
    public BaseResVO byTourist(@RequestParam(value = "sex") Integer type) {

        if (type != 1 && type != 2) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "请选择游客性别~");
        }
        char[] chr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String name = "火星人" + RandomStringUtils.random(7, chr);
        String avatar = String.format("http://prbsvykmy.bkt.clouddn.com/static/image/user-%d-default.png", type);
        String remark = "你今生有没有坚定不移地相信过一件事或一个人？是那种至死不渝的相信？";
        // 创建用户
        User user = new User();
        user.setName(name);
        user.setAvatar(avatar);
        user.setRemark(remark);
        boolean b = userService.insertUser(user);
        if (!b) {
            return ResultVOUtils.error();
        }
        
        Long uid = user.getUid();
        String token = UserLoginUtils.createSid(uid);
        
        UserLoginResVO userLoginResVO = new UserLoginResVO();
        userLoginResVO.setUid(uid);
        userLoginResVO.setSid(token);
        return ResultVOUtils.success(userLoginResVO);
    }
    
    @ApiOperation("QQ登陆")
    @PostMapping("/byQq")
    public BaseResVO byQq(@RequestParam(value = "code") String code,
                          @RequestParam(value = "redirect_uri") String redirect_uri) {
    
    
        String accessToken = qqWebAuthService.getAccessToken(code, redirect_uri);
        if (accessToken == null || "".equals(accessToken)) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "accessToken 获取失败~");
        }
    
        QqOpenIdResVO openIdResVO = qqWebAuthService.getOpenID(accessToken);
        if (openIdResVO == null || openIdResVO.getOpenid() == null || "".equals(openIdResVO.getOpenid())) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "openid 获取是吧~");
        }
        QqUserInfoResVO userInfo = qqWebAuthService.getUserInfo(accessToken, openIdResVO.getOpenid());
        
        if (userInfo == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "userInfo 获取失败~");
        }
    
        UserQq userQq = userQqService.findByOpenId(openIdResVO.getOpenid());
        
        // 判断是否有过值
        if (userQq == null || userQq.getUid() == null) {
            // 创建用户
            User user = new User();
            user.setName(userInfo.getNickname());
            user.setAvatar(userInfo.getFigureurl());
            String remark = "你今生有没有坚定不移地相信过一件事或一个人？是那种至死不渝的相信？";
            user.setRemark(remark);
            boolean b = userService.insertUser(user);
            if (!b) {
                return ResultVOUtils.error();
            }
            // 创建QQ用户
            userQq = new UserQq();
            userQq.setUid(user.getUid());
            userQq.setOpenid(openIdResVO.getOpenid());
            boolean b1 = userQqService.insertUserQq(userQq);
            if (!b1) {
                return ResultVOUtils.error();
            }
        }
    
        Long uid = userQq.getUid();
        
        String sid = UserLoginUtils.createSid(uid);
        
        UserLoginResVO userLoginResVO = new UserLoginResVO();
        userLoginResVO.setUid(uid);
        userLoginResVO.setSid(sid);
        return ResultVOUtils.success(userLoginResVO);
    }
    
    
}
