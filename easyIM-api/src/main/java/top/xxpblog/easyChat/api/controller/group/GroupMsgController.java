package top.xxpblog.easyChat.api.controller.group;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.constant.WSResTypeConstant;
import top.xxpblog.easyChat.api.service.group.GroupMsgService;
import top.xxpblog.easyChat.api.service.group.GroupUserService;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.ws.WSServer;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.req.GroupMsgCreateReqVO;
import top.xxpblog.easyChat.api.vo.res.GroupMsgListResVO;
import top.xxpblog.easyChat.api.vo.res.UserInfoListResVO;
import top.xxpblog.easyChat.common.entity.group.GroupMsg;
import top.xxpblog.easyChat.common.entity.group.GroupUser;
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

@Api("群消息发布相关API")
@RequestMapping("/group/msg")
@RestController
public class GroupMsgController {

    @Resource
    private GroupUserService groupUserService;

    @Resource
    private GroupMsgService groupMsgService;

    @Resource
    private UserService userService;
    
    @Resource
    private WSServer wsServer;

    @ApiOperation("查询列表")
    @GetMapping("/lists")
    public BaseResVO lists(@RequestParam("groupId") Long groupId,
                           @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                           HttpServletRequest request) {

        limit = limit > 25 ? 25 : limit;
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.check(request);
        if (userLoginDTO == null) {
            return ResultVOUtils.error(ResultEnum.LOGIN_VERIFY_FALL);
        }

        Long uid = userLoginDTO.getUid();

        // 判断是不是群里的人
        GroupUser groupUser = groupUserService.findByGroupIdAndUid(groupId, uid);
        if (groupUser == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "不是该群成员~");
        }

        List<GroupMsg> groupMsgs = groupMsgService.listByGroupIdAndCreateTime(groupId, groupUser.getCreateTime(), page, limit);

        List<GroupMsgListResVO> groupMsgListResVOS = new ArrayList<>();
        if (groupMsgs.size() == 0) {
            return ResultVOUtils.success(groupMsgListResVOS);
        }

        List<Long> uids = groupMsgs.stream().map(GroupMsg::getSenderUid).collect(Collectors.toList());
        Map<Long, UserInfoListResVO> userInfoListResVOMap = userService.listByUidIn(uids);

        groupMsgs.forEach(v -> {
            GroupMsgListResVO groupMsgListResVO = new GroupMsgListResVO();
            BeanUtils.copyProperties(v, groupMsgListResVO);
            groupMsgListResVO.setUser(userInfoListResVOMap.get(v.getSenderUid()));
            groupMsgListResVOS.add(groupMsgListResVO);
        });

        return ResultVOUtils.success(groupMsgListResVOS);
    }

    @ApiOperation("发布群消息")
    @PostMapping("/create")
    public BaseResVO create(@Valid @RequestBody GroupMsgCreateReqVO groupMsgCreateReqVO,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }

        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.check(request);
        if (userLoginDTO == null) {
            return ResultVOUtils.error(ResultEnum.LOGIN_VERIFY_FALL);
        }

        Long uid = userLoginDTO.getUid();

        Long groupId = groupMsgCreateReqVO.getGroupId();
        // 判断是否在群里面
        GroupUser groupUser1 = groupUserService.findByGroupIdAndUid(groupId, uid);
        if (groupUser1 == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "不是该群成员~");
        }

        Integer msgType = groupMsgCreateReqVO.getMsgType();
        String msgContent = groupMsgCreateReqVO.getMsgContent();
        boolean b = groupMsgService.sendGroupMsg(uid, groupId, WSResTypeConstant.GROUP, msgType, msgContent);
        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }


        return ResultVOUtils.success();
    }

}
