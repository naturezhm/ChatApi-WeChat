package me.xuxiaoxiao.chatapi.wechat.entity;

import java.io.Serializable;

public class AddMsg implements Serializable, Cloneable {
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

    @Override
    public AddMsg clone() {
        try {
            AddMsg addMsg = (AddMsg) super.clone();
            if (RecommendInfo != null) {
                addMsg.RecommendInfo = RecommendInfo.clone();
            }
            if (AppInfo != null) {
                addMsg.AppInfo = AppInfo.clone();
            }
            return addMsg;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class RecommendInfo implements Serializable, Cloneable {
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

        @Override
        public RecommendInfo clone() {
            try {
                return (RecommendInfo) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class AppInfo implements Serializable, Cloneable {
        public String AppID;
        public int Type;

        @Override
        public AppInfo clone() {
            try {
                return (AppInfo) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
