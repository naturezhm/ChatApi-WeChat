package me.xuxiaoxiao.chatapi.wechat.protocol;


import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.util.ArrayList;

public class RspGetContact {
    public BaseResponse BaseResponse;
    public int MemberCount;
    public ArrayList<User> MemberList;
    public int Seq;
}
