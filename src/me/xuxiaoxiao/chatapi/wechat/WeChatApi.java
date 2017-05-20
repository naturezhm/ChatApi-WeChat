package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.protocol.*;
import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.chatapi.wechat.protocol.ReqSendMsg.Msg;
import me.xuxiaoxiao.xtools.XHttpTools.XBody;
import me.xuxiaoxiao.xtools.XHttpTools.XUrl;
import me.xuxiaoxiao.xtools.XTools;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

final class WeChatApi {
    private static final String HOST_V1 = "wx.qq.com";
    private static final String HOST_V2 = "wx2.qq.com";

    private final long TIME_INIT = System.currentTimeMillis();
    private final AtomicBoolean FIRST_LOGIN = new AtomicBoolean(true);
    String uin;
    String sid;
    private long time = TIME_INIT;
    private String host;
    private String uuid;
    private String skey;
    private String passticket;
    private RspInit.SyncKey synckey;

    String jslogin() {
        XUrl xUrl = XUrl.base("https://login.wx.qq.com/jslogin");
        xUrl.param("_", time++);
        xUrl.param("appid", "wx782c26e4c19acffb");
        xUrl.param("fun", "new");
        xUrl.param("lang", "zh_CN");
        xUrl.param("redirect_uri", "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage");
        String rspStr = WeChatTools.request(xUrl, null, "\".+\"");
        if (XTools.strEmpty(rspStr)) {
            throw new RuntimeException("获取登录二维码出错，请检查https相关设置");
        } else {
            this.uuid = rspStr.substring(rspStr.indexOf('"') + 1, rspStr.lastIndexOf('"'));
            WeChatTools.LOGGER.info("登录二维码为：" + "https://login.weixin.qq.com/qrcode/" + uuid);
            return "https://login.weixin.qq.com/qrcode/" + uuid;
        }
    }

    RspLogin login() {
        XUrl xUrl = XUrl.base("https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login");
        xUrl.param("_", time++);
        xUrl.param("loginicon", true);
        xUrl.param("r", (int) (~(System.currentTimeMillis())));
        xUrl.param("tip", FIRST_LOGIN.getAndSet(false) ? 1 : 0);
        xUrl.param("uuid", uuid);
        RspLogin rspLogin = new RspLogin(WeChatTools.request(xUrl, null, "window"));
        if (!XTools.strEmpty(rspLogin.redirectUri)) {
            if (rspLogin.redirectUri.contains(HOST_V1)) {
                this.host = HOST_V1;
            } else {
                this.host = HOST_V2;
            }
            WeChatTools.LOGGER.info("主机为：" + host);
        }
        return rspLogin;
    }

    void webwxnewloginpage(String url) {
        String rspStr = XTools.http(XUrl.base(url)).string();
        if (!XTools.strEmpty(rspStr) && Pattern.compile("<error>.+</error>").matcher(rspStr).find()) {
            this.uin = rspStr.substring(rspStr.indexOf("<wxuin>") + "<wxuin>".length(), rspStr.indexOf("</wxuin>"));
            this.sid = rspStr.substring(rspStr.indexOf("<wxsid>") + "<wxsid>".length(), rspStr.indexOf("</wxsid>"));
            this.skey = rspStr.substring(rspStr.indexOf("<skey>") + "<skey>".length(), rspStr.indexOf("</skey>"));
            this.passticket = rspStr.substring(rspStr.indexOf("<pass_ticket>") + "<pass_ticket>".length(), rspStr.indexOf("</pass_ticket>"));
            WeChatTools.LOGGER.info(String.format("获取到uin:%s，sid:%s，skey:%s,passticket:%s", uin, sid, skey, passticket));
        }
    }

    RspInit webwxinit() {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxinit", host));
        xUrl.param("r", (int) (~(this.TIME_INIT)));
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqInit(new BaseRequest(uin, sid, skey))));
        RspInit rspInit = WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspInit.class);
        this.skey = rspInit.SKey;
        this.synckey = rspInit.SyncKey;
        WeChatTools.LOGGER.info(String.format("初始化成功skey:%s，synckey:%s", skey, synckey));
        return rspInit;
    }

    //消息已读接口
    RspStatusNotify webwxstatusnotify(String MyName) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxstatusnotify", host));
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqStatusNotify(new BaseRequest(uin, sid, skey), 3, MyName)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspStatusNotify.class);
    }

    RspGetContact webwxgetcontact() {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxgetcontact", host));
        xUrl.param("r", System.currentTimeMillis());
        xUrl.param("seq", 0);
        xUrl.param("skey", this.skey);
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, null, "\\{"), RspGetContact.class);
    }

    //获取群成员信息，不同之处在于该接口获取到的信息稍微详细点
    RspBatchGetContact webwxbatchgetcontact(ArrayList<Contact> contactList) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxbatchgetcontact", host));
        xUrl.param("r", System.currentTimeMillis());
        xUrl.param("type", "ex");
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqBatchGetContact(new BaseRequest(uin, sid, skey), contactList)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspBatchGetContact.class);
    }

    RspSyncCheck synccheck() {
        XUrl xUrl = XUrl.base(String.format("https://webpush.%s/cgi-bin/mmwebwx-bin/synccheck", host));
        xUrl.param("uin", this.uin);
        xUrl.param("sid", this.sid);
        xUrl.param("skey", this.skey);
        xUrl.param("deviceId", BaseRequest.deviceId());
        xUrl.param("synckey", this.synckey);
        xUrl.param("r", System.currentTimeMillis());
        xUrl.param("_", time++);
        return new RspSyncCheck(WeChatTools.request(xUrl, null, "\\{(.|\\s)+\\}"));
    }

    RspSync webwxsync() {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxsync", host));
        xUrl.param("sid", this.sid);
        xUrl.param("skey", this.skey);
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqSync(new BaseRequest(uin, sid, skey), this.synckey)));
        RspSync rspSync = WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspSync.class);
        this.synckey = rspSync.SyncKey;
        WeChatTools.LOGGER.info(String.format("更新syncKey:%s", synckey));
        return rspSync;
    }

    RspSendMsg webwxsendmsg(Msg msg) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxsendmsg", host));
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqSendMsg(new BaseRequest(uin, sid, skey), msg)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspSendMsg.class);
    }
}