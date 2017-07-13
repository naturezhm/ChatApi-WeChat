package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.util.ArrayList;

public class RspCreateChatroom {
    public BaseResponse BaseResponse;
    public String Topic;
    public String PYInitial;
    public String QuanPin;
    public int MemberCount;
    public ArrayList<User> MemberList;
    public String ChatRoomName;
    public String BlackList;
}
