package top.xxpblog.easyChat.api.utils;

import top.xxpblog.easyChat.api.vo.req.WSBaseReqVO;
import top.xxpblog.easyChat.api.vo.req.WSMessageReqVO;
import top.xxpblog.easyChat.api.vo.req.WSUserReqVO;

public class WSBaseReqUtils {

    /**
     * 创建请求实体类
     * @return
     */
    public static WSBaseReqVO create(Integer type, Long receiveId, Integer msgType, String msgContent, Long uid, String name, String avatar, String remark) {

        // 消息实体
        WSMessageReqVO wsMessageReqVO = new WSMessageReqVO();
        wsMessageReqVO.setReceiveId(receiveId != null ? receiveId : 0);
        wsMessageReqVO.setMsgType(msgType != null ? msgType : 0);
        wsMessageReqVO.setMsgContent(msgContent);

        // 用户信息
        WSUserReqVO wsUserReqVO = new WSUserReqVO();
        wsUserReqVO.setUid(uid != null ? uid : 0);
        wsUserReqVO.setName(name != null ? name : "");
        wsUserReqVO.setAvatar(avatar != null ? avatar : "");
        wsUserReqVO.setRemark(remark != null ? remark : "");

        WSBaseReqVO wsBaseReqVO = new WSBaseReqVO();
        wsBaseReqVO.setType(type != null ? type : 0);
        wsBaseReqVO.setMessage(wsMessageReqVO);
        wsBaseReqVO.setUser(wsUserReqVO);
        return wsBaseReqVO;
    }

}
