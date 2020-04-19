package top.xxpblog.easyChat.api.controller.user;

import io.swagger.annotations.ApiOperation;
import top.xxpblog.easyChat.api.annotation.RequiredPermission;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.constant.WSMsgTypeConstant;
import top.xxpblog.easyChat.api.constant.WSResTypeConstant;
import top.xxpblog.easyChat.api.service.user.UserFriendMsgService;
import top.xxpblog.easyChat.api.service.user.UserFriendService;
import top.xxpblog.easyChat.api.service.user.UserService;
import top.xxpblog.easyChat.api.ws.WSServer;
import top.xxpblog.easyChat.api.utils.PageUtils;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.api.utils.WSBaseReqUtils;
import top.xxpblog.easyChat.api.vo.req.UserFriendMsgClearMsgCountReqVO;
import top.xxpblog.easyChat.api.vo.req.UserFriendMsgSaveReqVO;
import top.xxpblog.easyChat.api.vo.req.WSBaseReqVO;
import top.xxpblog.easyChat.common.entity.user.User;
import top.xxpblog.easyChat.common.entity.user.UserFriend;
import top.xxpblog.easyChat.common.entity.user.UserFriendMsg;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 朋友相关
 */
@RequestMapping("/user/friendMsg")
@RestController
public class UserFriendMsgController {
    
    @Resource
    private UserFriendMsgService userFriendMsgService;
    
    @Resource
    private UserFriendService userFriendService;
    
    @Resource
    private UserService userService;

    @Resource
    private WSServer wsServer;
    
    @ApiOperation("获取好友消息列表")
    @RequiredPermission
    @GetMapping("/lists")
    public BaseResVO lists(@RequestParam(value = "senderUid") Long senderUid,
                           @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                           HttpServletRequest request) {
        
        // 验证登录
        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);
        
        Long uid = userLoginDTO.getUid();
        
        if (limit > 50) {
            limit = 50;
        }
        
        Integer offset = PageUtils.createOffset(page, limit);
        
        // 把最小的那个 用户ID作为查询条件
        Long toUid = senderUid;
        if (uid > senderUid) {
            toUid = uid;
            uid = senderUid;
        }
        
        List<UserFriendMsg> userFriendMsgs = userFriendMsgService.listByUidAndToUid(uid, toUid, offset, limit);
        
        return ResultVOUtils.success(userFriendMsgs);
        
    }
    
    @ApiOperation("好友间发送消息")
    @RequiredPermission
    @PostMapping("/create")
    public BaseResVO create(@Valid @RequestBody UserFriendMsgSaveReqVO userFriendMsgSaveReqVO,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }
        

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        
        Long uid = userLoginDTO.getUid();
        
        Long receiverUid = userFriendMsgSaveReqVO.getReceiverUid();
        
        // 判断是不是朋友
        UserFriend userFriend = userFriendService.findByUidAndFriendUid(uid, receiverUid);
        if (userFriend == null) { //TODO 用null来判断有点不合适，是否能换一种方式
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "该用户还不是你的好友~");
        }
    
        Long senderUid = uid;
        // 追加消息
        UserFriendMsg userFriendMsg = new UserFriendMsg();
        // 把最小的那个 用户ID作为 之后的查询uid //TODO 不是很理解
        Long toUid = receiverUid;
        if (uid > receiverUid) {
            toUid = uid;
            uid = receiverUid;
        }
        userFriendMsg.setUid(uid);
        userFriendMsg.setToUid(toUid);
        userFriendMsg.setSenderUid(senderUid);
        Integer msgType = userFriendMsgSaveReqVO.getMsgType();
        String msgContent = userFriendMsgSaveReqVO.getMsgContent();
        String lastMsgContent = msgContent;
        switch (msgType) {
            case WSMsgTypeConstant.TEXT:
                break;
            case WSMsgTypeConstant.IMAGE:
                lastMsgContent = "[图片消息]";
                break;
            case WSMsgTypeConstant.FILE:
                lastMsgContent = "[文件消息]";
                break;
            case WSMsgTypeConstant.VOICE:  //TODO 现在还没有实现
                lastMsgContent = "[语言消息]";
                break;
            case WSMsgTypeConstant.VIDEO: //TODO 现在还没有实现
                lastMsgContent = "[视频消息]";
                break;
            default:
                return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, "位置消息类型");
        }
        userFriendMsg.setMsgType(msgType);
        userFriendMsg.setMsgContent(msgContent);
        boolean result = userFriendMsgService.insertUserFriendMsg(userFriendMsg);
        if (!result) {
            return ResultVOUtils.error();
        }
        
        List<UserFriend> userFriends = new ArrayList<>();
        // 给接收者更新最后一次的消息
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUid(receiverUid);
        userFriend1.setFriendUid(senderUid);
        userFriend1.setUnMsgCount(1);
        userFriend1.setLastMsgContent(lastMsgContent);
        userFriend1.setCreateTime(new Date());
        userFriend1.setModifiedTime(new Date());
        userFriends.add(userFriend1);
        // 给当前用户更新最后一次的消息
        UserFriend userFriend2 = new UserFriend();
        userFriend2.setUid(senderUid);
        userFriend2.setFriendUid(receiverUid);
        userFriend2.setUnMsgCount(0);
        userFriend2.setLastMsgContent(lastMsgContent);
        userFriend2.setCreateTime(new Date());
        userFriend2.setModifiedTime(new Date());
        userFriends.add(userFriend2);
        
        userFriendService.insertUserFriendAll(userFriends);

        // 发送在线消息
        // 查询用户信息
        User user = userService.findByUid(senderUid);
        Long sUid = user.getUid();
        String name = user.getName();
        String avatar = user.getAvatar();
        String remark = user.getRemark();
        WSBaseReqVO wsBaseReqVO = WSBaseReqUtils.create(WSResTypeConstant.FRIEND, receiverUid, msgType, msgContent, sUid, name, avatar, remark);
        wsServer.sendMsg(receiverUid, wsBaseReqVO);
        
        return ResultVOUtils.success();
    }
    
    
    @ApiOperation("清空未读的消息数量")
    @RequiredPermission
    @PostMapping("/clearUnMsgCount")
    public BaseResVO clearUnMsgCount(@Valid @RequestBody UserFriendMsgClearMsgCountReqVO msgCountReqVO,
                                     BindingResult bindingResult,
                                     HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResultVOUtils.error(ResultEnum.PARAM_VERIFY_FALL, bindingResult.getFieldError().getDefaultMessage());
        }
        

        UserLoginDTO userLoginDTO = UserLoginUtils.getUserLoginDTO(request);

        
        Long uid = userLoginDTO.getUid();
        
        Long receiverUid = msgCountReqVO.getReceiverUid();
        
        // 更新最后一次的消息
        UserFriend userFriend = new UserFriend();
        userFriend.setUid(uid);
        userFriend.setFriendUid(receiverUid);
        userFriend.setUnMsgCount(0);
        boolean b = userFriendService.updateUserFriend(userFriend);
        
        return ResultVOUtils.success();
    }
    
}
