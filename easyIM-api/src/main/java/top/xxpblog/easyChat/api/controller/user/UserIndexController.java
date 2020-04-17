package top.xxpblog.easyChat.api.controller.user;

import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.annotation.RequiredPermission;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.service.user.UserProfileService;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.utils.UserFriendUtils;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.res.UserInfoResVO;
import top.xxpblog.easyChat.api.vo.res.UserProfileInfoResVO;
import top.xxpblog.easyChat.api.vo.res.UserReadResVO;
import top.xxpblog.easyChat.common.entity.user.User;
import top.xxpblog.easyChat.common.entity.user.UserProfile;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/user")
@RestController
public class UserIndexController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserProfileService userProfileService;
    
    @ApiOperation("查询登陆的用户信息")
    @RequiredPermission
    @GetMapping("/loginInfo")
    public BaseResVO loginInfo(HttpServletRequest request) {

        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

    
        Long uid = userLoginDTO.getUid();
    
        User user = userService.findByUid(uid);
        if (user == null) {
            return ResultVOUtils.error(ResultEnum.LOGIN_VERIFY_FALL);
        }
        
        UserProfile userProfile = userProfileService.findByUid(uid);
    
        UserProfileInfoResVO userProfileInfoResVO = new UserProfileInfoResVO();
        if (userProfile != null) {
            BeanUtils.copyProperties(userProfile, userProfileInfoResVO);
        }

        UserInfoResVO userInfoResVO = new UserInfoResVO();
        BeanUtils.copyProperties(user, userInfoResVO);
        userInfoResVO.setProfile(userProfileInfoResVO);
    
        return ResultVOUtils.success(userInfoResVO);
        
    }
    
    @ApiOperation("查看他人的用户信息")
    @RequiredPermission
    @GetMapping("/read")
    public BaseResVO read(@RequestParam(value = "uid") Long uid) {


    
        User user = userService.findByUid(uid);
    
        UserReadResVO userReadResVO = new UserReadResVO();
        BeanUtils.copyProperties(user, userReadResVO);
    
        return ResultVOUtils.success(userReadResVO);
        
    }
    
    
    
    @ApiOperation("获取用户的二维码--可用户添加好友")
    @RequiredPermission
    @GetMapping("/getQRCheckCode")
    public BaseResVO getQRCheckCode(HttpServletRequest request) {
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);
        
        Long uid = userLoginDTO.getUid();
        
        String checkCode = UserFriendUtils.createCheckCode(uid);
        
        return ResultVOUtils.success(checkCode);
        
    }
    
    
}
