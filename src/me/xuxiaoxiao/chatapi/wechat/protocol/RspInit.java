package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.util.ArrayList;

public class RspInit {
    public BaseResponse BaseResponse;
    public User User;
    public int Count;
    public ArrayList<User> ContactList;
    public SyncKey SyncKey;
    public String ChatSet;
    public String SKey;
    public long ClientVersion;
    public long SystemTime;
    public int GrayScale;
    public int InviteStartCount;
    public int MPSubscribeMsgCount;
    public ArrayList<MPSubscribeMsg> MPSubscribeMsgList;
    public int ClickReportInterval;

    public static class SyncKey {
        public int Count;
        public ArrayList<SyncKeyItem> List;

        @Override
        public String toString() {
            StringBuilder sbKey = new StringBuilder();
            for (SyncKeyItem item : List) {
                if (sbKey.length() > 0) {
                    sbKey.append("|");
                }
                sbKey.append(item.Key).append("_").append(item.Val);
            }
            return sbKey.toString();
        }

        public static class SyncKeyItem {
            public int Key;
            public int Val;
        }
    }

    public static class MPSubscribeMsg {
        public String UserName;
        public String NickName;
        public long Time;
        public int MPArticleCount;
        public ArrayList<MPArticle> MPArticleList;

        public static class MPArticle {
            public String Title;
            public String Digest;
            public String Cover;
            public String Url;
        }
    }

}
