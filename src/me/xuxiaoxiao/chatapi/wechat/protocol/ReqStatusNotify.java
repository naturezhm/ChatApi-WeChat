package me.xuxiaoxiao.chatapi.wechat.protocol;

public class ReqStatusNotify {
    public BaseRequest BaseRequest;
    public int Code;
    public String FromUserName;
    public String ToUserName;
    public long ClientMsgId;

    public ReqStatusNotify(BaseRequest BaseRequest, int Code, String MyUserName) {
        this.BaseRequest = BaseRequest;
        this.Code = Code;
        this.FromUserName = MyUserName;
        this.ToUserName = MyUserName;
        this.ClientMsgId = System.currentTimeMillis();
    }
}
