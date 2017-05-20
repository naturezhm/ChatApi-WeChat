package me.xuxiaoxiao.chatapi.wechat.protocol;

import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;
import me.xuxiaoxiao.chatapi.wechat.entity.User;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit.SyncKey;

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
