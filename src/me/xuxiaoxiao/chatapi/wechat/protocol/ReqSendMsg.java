package me.xuxiaoxiao.chatapi.wechat.protocol;

public class ReqSendMsg {
    public final BaseRequest BaseRequest;
    public final Msg Msg;

    public ReqSendMsg(BaseRequest BaseRequest, Msg Msg) {
        this.BaseRequest = BaseRequest;
        this.Msg = Msg;
    }

    public static class Msg {
        public int Type;
        public String Content;
        public String FromUserName;
        public String ToUserName;
        public String LocalID;
        public String ClientMsgId;
        public int Scene;

        public Msg(int Type, String Content, String FromUserName, String ToUserName) {
            this.Type = Type;
            this.Content = Content;
            this.FromUserName = FromUserName;
            this.ToUserName = ToUserName;
            this.LocalID = msgId();
            this.ClientMsgId = LocalID;
            this.Scene = 0;
        }

        public static String msgId() {
            StringBuilder sbRandom = new StringBuilder().append(System.currentTimeMillis());
            for (int i = 0; i < 4; i++) {
                sbRandom.append((int) (Math.random() * 10));
            }
            return sbRandom.toString();
        }
    }
}
