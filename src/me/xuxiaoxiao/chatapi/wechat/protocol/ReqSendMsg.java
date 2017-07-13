package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.entity.Msg;

public class ReqSendMsg {
    public BaseRequest BaseRequest;
    public Msg Msg;
    public int Scene;

    public ReqSendMsg(BaseRequest baseRequest, Msg msg) {
        this.BaseRequest = baseRequest;
        this.Msg = msg;
        this.Scene = 0;
    }
}
