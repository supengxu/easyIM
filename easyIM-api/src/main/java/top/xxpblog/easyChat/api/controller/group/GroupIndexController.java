package top.xxpblog.easyChat.api.controller.group;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import top.xxpblog.easyChat.api.annotation.RequiredPermission;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.service.group.GroupService;
import top.xxpblog.easyChat.api.service.group.GroupUserService;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.vo.req.GroupSaveReqVO;
import top.xxpblog.easyChat.api.vo.res.GroupIndexListResVO;
import top.xxpblog.easyChat.api.vo.res.UserInfoListResVO;
import top.xxpblog.easyChat.common.entity.group.Group;
import top.xxpblog.easyChat.common.entity.group.GroupUser;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 群相关
 */
@Api("群相关接口")
@RequestMapping("/group")
@RestController
public class GroupIndexController {

    @Resource
    private GroupService groupService;

    @Resource
    private GroupUserService groupUserService;


    @Resource
    private UserService userService;

    @ApiOperation("查询群用户列表")
    @RequiredPermission
    @GetMapping("/lists")
    public BaseResVO lists(@RequestParam("groupId") Long groupId,
                           @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                           HttpServletRequest request) {

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);


        Long uid = userLoginDTO.getUid();
        // 判断是不是在群里边
        GroupUser groupUser = groupUserService.findByGroupIdAndUid(groupId, uid);
        if (groupUser == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "请先加入群~");
        }
        limit = limit > 500 ? 500 : limit;
        List<GroupUser> groupUsers = groupUserService.listByGroupId(groupId, page, limit);

        List<Long> uids = groupUsers.stream().map(GroupUser::getUid).collect(Collectors.toList());
        Map<Long, UserInfoListResVO> userInfoListResVOMap = userService.listByUidIn(uids);

        List<GroupIndexListResVO> groupIndexListResVOS = new ArrayList<>();
        groupUsers.forEach(v -> {
            GroupIndexListResVO groupIndexListResVO = new GroupIndexListResVO();
            BeanUtils.copyProperties(v, groupIndexListResVO);
            groupIndexListResVO.setUser(userInfoListResVOMap.get(v.getUid()));
            groupIndexListResVOS.add(groupIndexListResVO);
        });

        return ResultVOUtils.success(groupIndexListResVOS);
    }

    @ApiOperation("创建群聊")
    @RequiredPermission
    @PostMapping("/create")
    public BaseResVO create(@Valid @RequestBody GroupSaveReqVO groupSaveReqVO,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);


        Long uid = userLoginDTO.getUid();

        String avatar = groupSaveReqVO.getAvatar();
        
        avatar = avatar == null || "".equals(avatar) ? "http://prbsvykmy.bkt.clouddn.com/static/image/group-default.png" : avatar;
        
        Group group = new Group();
        group.setUid(uid);
        group.setName(groupSaveReqVO.getName());
        group.setAvatar(avatar);
        group.setMemberNum(1);
    
        String remark = groupSaveReqVO.getRemark();
        remark = remark == null || "".equals(remark) ? "你今生有没有坚定不移地相信过一件事或一个人？是那种至死不渝的相信？" : remark;
        group.setRemark(remark);
        boolean b = groupService.insertGroup(group);

        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }

        // 加入群列表
        GroupUser groupUser = new GroupUser();
        groupUser.setGroupId(group.getGroupId());
        groupUser.setUid(uid);
        groupUser.setRank(2); // 群主
        boolean b1 = groupUserService.insertGroupUser(groupUser);

        return ResultVOUtils.success(group);
    }

    @ApiOperation("更新群信息")
    @RequiredPermission
    @PostMapping("/update")
    public BaseResVO update(@Valid @RequestBody GroupSaveReqVO groupSaveReqVO,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }

        if (groupSaveReqVO.getGroupId() == null
                || groupSaveReqVO.getGroupId() <= 0) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "参数错误~");
        }

        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);


        Long uid = userLoginDTO.getUid();

        Group group1 = groupService.findByGroupId(groupSaveReqVO.getGroupId());
        // 判断是否是群主
        if (group1 == null || !uid.equals(group1.getUid())) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "没有该群信息~");
        }

        Group group = new Group();
        group.setGroupId(groupSaveReqVO.getGroupId());
        boolean isUp = false;
        if (null != groupSaveReqVO.getName() && !groupSaveReqVO.getName().equals(group1.getName())) {
            group.setName(groupSaveReqVO.getName());
            isUp = true;
        }
        if (null != groupSaveReqVO.getAvatar() && !groupSaveReqVO.getAvatar().equals(group1.getAvatar())) {
            group.setAvatar(groupSaveReqVO.getAvatar());
            isUp = true;
        }
        if (null != groupSaveReqVO.getRemark() && !groupSaveReqVO.getRemark().equals(group1.getRemark())) {
            group.setRemark(groupSaveReqVO.getRemark());
            isUp = true;
        }
        boolean b = true;
        if (isUp) {
            b = groupService.updateGroup(group);
        }
        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }

        return ResultVOUtils.success();
    }

    @ApiOperation("删除群\\解散群")
    @RequiredPermission
    @PostMapping("/delete")
    public BaseResVO delete(@RequestParam(value = "groupId") Long groupId,
                            HttpServletRequest request) {

        if (groupId == null
                || groupId <= 0) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "参数错误~");
        }


        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        Long uid = userLoginDTO.getUid();

        Group group1 = groupService.findByGroupId(groupId);
        // 判断是否是群主
        if (group1 == null || !uid.equals(group1.getUid())) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "没有该群信息~");
        }

        // 删除群成员信息
        boolean b = groupUserService.deleteByGroupId(groupId);
        if (!b) {
            return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
        }

        return ResultVOUtils.success();
    }

}
