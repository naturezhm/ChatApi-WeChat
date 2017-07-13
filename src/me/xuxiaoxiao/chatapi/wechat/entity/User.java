package me.xuxiaoxiao.chatapi.wechat.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable, Cloneable {
    public long Uin;
    public String UserName;
    public String NickName;
    public String HeadImgUrl;
    public String RemarkName;
    public String PYInitial;
    public String PYQuanPin;
    public String RemarkPYInitial;
    public String RemarkPYQuanPin;
    public int HideInputBarFlag;
    public int StarFriend;
    public int Sex;
    public String Signature;
    public int AppAccountFlag;
    public int VerifyFlag;
    public int ContactFlag;
    public int WebWxPluginSwitch;
    public int HeadImgFlag;
    public int SnsFlag;

    public long OwnerUin;
    public int MemberCount;
    public ArrayList<User> MemberList;
    public int Statues;
    public long AttrStatus;
    public long MemberStatus;
    public String Province;
    public String City;
    public String Alias;
    public long UniFriend;
    public String DisplayName;
    public long ChatRoomId;
    public String KeyWord;
    public String EncryChatRoomId;

    @Override
    public User clone() {
        try {
            User user = (User) super.clone();
            if (MemberList != null) {
                user.MemberList = new ArrayList<>();
                for (User member : MemberList) {
                    user.MemberList.add(member.clone());
                }
            }
            return user;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
