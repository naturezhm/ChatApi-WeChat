package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit.SyncKey;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit.User;

import java.util.ArrayList;

public class RspSync {
    public BaseResponse BaseResponse;
    public int AddMsgCount;
    public ArrayList<AddMsg> AddMsgList;
    public int ModContactCount;
    public ArrayList<User> ModContactList;
    public int DelContactCount;
    public ArrayList<User> DelContactList;
    public int ModChatRoomMemberCount;
    public ArrayList<User> ModChatRoomMemberList;
    public Profile Profile;
    public int ContinueFlag;
    public SyncKey SyncKey;
    public String Skey;
    public SyncKey SyncCheckKey;

    public static class AddMsg {
        public String MsgId;
        public String FromUserName;
        public String ToUserName;
        public int MsgType;
        public String Content;
        public int Status;
        public int ImgStatus;
        public long CreateTime;
        public long VoiceLength;
        public long PlayLength;
        public String FileName;
        public String FileSize;
        public String MediaId;
        public String Url;
        public int AppMsgType;
        public int StatusNotifyCode;
        public String StatusNotifyUserName;
        public RecommendInfo RecommendInfo;
        public int ForwardFlag;
        public AppInfo AppInfo;
        public int HasProductId;
        public String Ticket;
        public int ImgHeight;
        public int ImgWidth;
        public int SubMsgType;
        public long NewMsgId;

        public static class RecommendInfo {
            public String UserName;
            public String NickName;
            public int QQNum;
            public String Province;
            public String City;
            public String Content;
            public String Signature;
            public String Alias;
            public int Scene;
            public int VerifyFlag;
            public int AttrStatus;
            public int Sex;
            public String Ticket;
            public int OpCode;

        }

        public static class AppInfo {
            public String AppID;
            public int Type;

        }
    }

    public static class Profile {
        public int BitFlag;
        public ProfileItem UserName;
        public ProfileItem NickName;
        public long BindUin;
        public ProfileItem BindEmail;
        public ProfileItem BindMobile;
        public int Status;
        public int Sex;
        public int PersonalCard;
        public String Alias;
        public int HeadImgUpdateFlag;
        public String HeadImgUrl;
        public String Signature;

        public static class ProfileItem {
            public String Buff;
        }
    }
}
