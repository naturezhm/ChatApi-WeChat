package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.util.ArrayList;

public class RspUpdateChatroom {
    public BaseResponse BaseResponse;
    public int MemberCount;
    public ArrayList<User> MemberList;
}
