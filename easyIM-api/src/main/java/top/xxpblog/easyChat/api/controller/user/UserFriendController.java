package top.xxpblog.easyChat.api.controller.user;

import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.annotation.RequiredPermission;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.service.user.UserFriendService;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.req.UserFriendDeleteReqVO;
import top.xxpblog.easyChat.api.vo.res.UserFriendListInfoResVO;
import top.xxpblog.easyChat.api.vo.res.UserInfoListResVO;
import top.xxpblog.easyChat.common.entity.user.UserFriend;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 朋友相关
 */
@RequestMapping("/user/friend")
@RestController
public class UserFriendController {

    @Resource
    private UserFriendService userFriendService;

    @Resource
    private UserService userService;

    @ApiOperation("获取好友列表")
    @RequiredPermission
    @GetMapping("/lists")
    public BaseResVO lists(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                           HttpServletRequest request) {

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        limit = limit > 50 ? 50 : limit;

        Long uid = userLoginDTO.getUid();


        List<UserFriend> userFriends = userFriendService.listByUid(uid, page, limit);

        List<Long> uids = userFriends.stream().map(UserFriend::getFriendUid).collect(Collectors.toList());

        Map<Long, UserInfoListResVO> userInfoListResVOMap = userService.listByUidIn(uids);

        List<UserFriendListInfoResVO> userFriendListInfoResVOS = new ArrayList<>();
        userFriends.forEach(v -> {
            UserFriendListInfoResVO userFriendListInfoResVO = new UserFriendListInfoResVO();
            BeanUtils.copyProperties(v, userFriendListInfoResVO);
            userFriendListInfoResVO.setUser(userInfoListResVOMap.get(v.getFriendUid()));
            userFriendListInfoResVOS.add(userFriendListInfoResVO);
        });

        return ResultVOUtils.success(userFriendListInfoResVOS);

    }


    @ApiOperation("删除好友关系")
    @RequiredPermission
    @PostMapping("/delete")
    public BaseResVO delete(@Valid @RequestBody UserFriendDeleteReqVO userFriendDeleteReqVO,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }

        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);


        Long uid = userLoginDTO.getUid();
        Long friendUid = userFriendDeleteReqVO.getFriendUid();

        boolean b = userFriendService.deleteByUidAndFriendUid(uid, friendUid);
        boolean b1 = userFriendService.deleteByUidAndFriendUid(friendUid, uid);
        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }

        return ResultVOUtils.success();
    }
}
