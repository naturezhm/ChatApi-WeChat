package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit.SyncKey;

public class ReqSync {
    public final BaseRequest BaseRequest;
    public final SyncKey SyncKey;
    public final int rr;

    public ReqSync(BaseRequest baseRequest, SyncKey syncKey) {
        this.BaseRequest = baseRequest;
        this.SyncKey = syncKey;
        this.rr = (int) (~(System.currentTimeMillis()));
    }
}
