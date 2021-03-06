package top.xxpblog.easyChat.api.controller.group;

import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.annotation.RequiredPermission;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.constant.WSMsgTypeConstant;
import top.xxpblog.easyChat.api.constant.WSResTypeConstant;
import top.xxpblog.easyChat.api.service.group.GroupMsgService;
import top.xxpblog.easyChat.api.service.group.GroupService;
import top.xxpblog.easyChat.api.service.group.GroupUserService;
import top.xxpblog.easyChat.api.utils.GroupUserUtils;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.req.GroupUserSaveReqVO;
import top.xxpblog.easyChat.api.vo.res.GroupUserListResVO;
import top.xxpblog.easyChat.common.entity.group.Group;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 群用户相关
 */
@RequestMapping("/group/user")
@RestController
public class GroupUserController {
    
    @Resource
    private GroupService groupService;
    
    @Resource
    private GroupUserService groupUserService;

    @Resource
    private GroupMsgService groupMsgService;
    
    @ApiOperation("获取该用户的群列表")
    @RequiredPermission
    @GetMapping("/lists")
    public BaseResVO lists(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                           HttpServletRequest request) {
        
        limit = limit > 100 ? 100 : limit;
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        
        Long uid = userLoginDTO.getUid();
        
        List<GroupUserListResVO> groupUserListResVOS = new ArrayList<>();
        
        List<GroupUser> groupUsers = groupUserService.listByUid(uid, page, limit);
        // 没有加入任何群
        if (groupUsers.size() <= 0) {
            return ResultVOUtils.success(groupUserListResVOS);
        }
        
        List<Long> groupIds = groupUsers.stream().map(GroupUser::getGroupId).collect(Collectors.toList());
        // 查询群
        Map<Long, Group> groupMap = groupService.listByGroupIdIn(groupIds);
        
        groupUsers.forEach(v -> {
            GroupUserListResVO groupUserListResVO = new GroupUserListResVO();
            BeanUtils.copyProperties(v, groupUserListResVO);
            groupUserListResVO.setGroup(groupMap.get(v.getGroupId()));
            groupUserListResVOS.add(groupUserListResVO);
        });
        
        return ResultVOUtils.success(groupUserListResVOS);
    }
    
    @ApiOperation("通过二维码邀请入群")
    @RequiredPermission
    @PostMapping("/create")
    public BaseResVO create(@RequestParam(value = "checkCode", required = false, defaultValue = "") String checkCode,
                            @RequestParam(value = "groupId", required = false, defaultValue = "0") Long groupId,
                            HttpServletRequest request) {
        
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        
        Long uid = userLoginDTO.getUid();
        if (groupId == null || groupId <= 0) {
            groupId = GroupUserUtils.checkCode(checkCode);
        }
        // 验证用户是否合法加入
        if (groupId == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "二维码过期~");
        }
        
        Group group = groupService.findByGroupId(groupId);
        if (group == null) {
            return ResultVOUtils.error(ResultEnum.DATA_NOT);
        }
        // 如果是群主
        if (uid.equals(group.getUid())) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "已在当前群聊~");
        }
        
        // 最多500人
        if (group.getMemberNum() >= 500) {
            return ResultVOUtils.error(ResultEnum.AUTH_FAILED, "群用户已达上限~");
        }
        
        // 判断是否在群里面
        GroupUser groupUser1 = groupUserService.findByGroupIdAndUid(groupId, uid);
        if (groupUser1 != null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "已在当前群聊~");
        }
        
        GroupUser groupUser = new GroupUser();
        groupUser.setGroupId(groupId);
        groupUser.setUid(uid);
        groupUser.setRank(0);
        
        // 递增群用户数量
        groupService.incMemberNumByGroupId(groupId, 1);
        
        boolean b = groupUserService.insertGroupUser(groupUser);
        
        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }

        // 追加群消息
        Integer msgType = WSMsgTypeConstant.TEXT;
        String msgContent = "嗨！大家好~";
        groupMsgService.sendGroupMsg(uid, groupId, WSResTypeConstant.JOIN_GROUP, msgType, msgContent);

        GroupUserListResVO groupUserListResVO = new GroupUserListResVO();
        BeanUtils.copyProperties(groupUser, groupUserListResVO);
        groupUserListResVO.setLastMsgContent(msgContent);
        groupUserListResVO.setLastMsgTime(new Date());
        groupUserListResVO.setUnMsgCount(1);
        groupUserListResVO.setGroup(group);
    
        return ResultVOUtils.success(groupUserListResVO);
    }
    
    @ApiOperation("更新群")
    @RequiredPermission
    @PostMapping("/update")
    public BaseResVO update(@Valid @RequestBody GroupUserSaveReqVO groupUserSaveReqVO,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }
        

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        
        Long uid = userLoginDTO.getUid();
        
        GroupUser groupUser = new GroupUser();
        groupUser.setGroupId(groupUserSaveReqVO.getGroupId());
        groupUser.setUid(uid);
        groupUser.setRemark(groupUserSaveReqVO.getRemark());
        
        boolean b = groupUserService.updateGroupUserByGroupIdAndUid(groupUser);
        
        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }
        
        return ResultVOUtils.success();
    }
    
    @ApiOperation("删除 （退出群）")
    @RequiredPermission
    @PostMapping("/delete")
    public BaseResVO delete(@RequestParam(value = "groupId") Long groupId,
                            HttpServletRequest request) {
        
        if (groupId == null
                || groupId <= 0) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "参数错误~");
        }
        
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);
        
        Long uid = userLoginDTO.getUid();
        
        // 判断是不是群主
        Group group = groupService.findByGroupId(groupId);
        if (group == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "没有该群信息~");
        }
        if (uid.equals(group.getUid())) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "群主不能退群~");
        }
        
        // 删除群成员信息
        boolean b = groupUserService.deleteByGroupIdAndUid(groupId, uid);
        if (!b) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "退群失败，请重试~");
        }
        
        // 递减群用户数量
        groupService.decMemberNumByGroupId(groupId);
        
        return ResultVOUtils.success();
    }
    
    @ApiOperation("获取二维码")
    @RequiredPermission
    @GetMapping("/getCheckCode")
    public BaseResVO getCheckCode(@RequestParam(value = "groupId") Long groupId,
                                  HttpServletRequest request) {

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);
        
        Long uid = userLoginDTO.getUid();
        
        GroupUser groupUser = groupUserService.findByGroupIdAndUid(groupId, uid);
        if (groupUser == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "请先加入群~");
        }
        
        String checkCode = GroupUserUtils.createCheckCode(groupId);
        
        return ResultVOUtils.success(checkCode);
        
    }
    
    
    @ApiOperation("清空未读消息数量")
    @RequiredPermission
    @PostMapping("/clearUnMsgCount")
    public BaseResVO clearUnMsgCount(@RequestParam(value = "groupId") Long groupId,
                                     HttpServletRequest request) {
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);
        
        Long uid = userLoginDTO.getUid();
        // 判断是否在群里面
        GroupUser groupUser = groupUserService.findByGroupIdAndUid(groupId, uid);
        if (groupUser == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "不是该群成员~");
        }
        
        GroupUser groupUser1 = new GroupUser();
        groupUser1.setGroupId(groupId);
        groupUser1.setUid(uid);
        groupUserService.clearUnMsgCountByGroupIdAndUid(groupUser1);
        
        return ResultVOUtils.success();
    }
    
}
