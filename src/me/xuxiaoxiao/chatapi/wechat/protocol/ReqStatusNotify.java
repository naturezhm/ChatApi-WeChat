package me.xuxiaoxiao.chatapi.wechat.protocol;

public class ReqStatusNotify {
    public BaseRequest BaseRequest;
    public int Code;
    public String FromUserName;
    public String ToUserName;
    public long ClientMsgId;

    public ReqStatusNotify(BaseRequest baseRequest, int code, String myName) {
        this.BaseRequest = baseRequest;
        this.Code = code;
        this.FromUserName = myName;
        this.ToUserName = myName;
        this.ClientMsgId = System.currentTimeMillis();
    }
}
