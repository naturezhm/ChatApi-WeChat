package me.xuxiaoxiao.chatapi.wechat.protocol;

public class ReqOplog {
    public BaseRequest BaseRequest;
    public String UserName;
    public int CmdId;
    public String RemarkName;

    public ReqOplog(BaseRequest baseRequest, String userName, String remarkName) {
        this.BaseRequest = baseRequest;
        this.CmdId = 2;
        this.UserName = userName;
        this.RemarkName = remarkName;
    }
}
