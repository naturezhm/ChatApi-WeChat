package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit.SyncKey;

public class ReqSync {
    public final BaseRequest BaseRequest;
    public final SyncKey SyncKey;
    public final int rr;

    public ReqSync(BaseRequest BaseRequest, SyncKey SyncKey) {
        this.BaseRequest = BaseRequest;
        this.SyncKey = SyncKey;
        this.rr = (int) (~(System.currentTimeMillis()));
    }
}
