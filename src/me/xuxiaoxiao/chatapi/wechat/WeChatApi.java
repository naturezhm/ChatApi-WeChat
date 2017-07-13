package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.Msg;
import me.xuxiaoxiao.chatapi.wechat.protocol.*;
import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.xtools.XHttpTools.XBody;
import me.xuxiaoxiao.xtools.XHttpTools.XUrl;
import me.xuxiaoxiao.xtools.XTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * 网页版微信全部接口
 */
final class WeChatApi {
    private static final String HOST_V1 = "wx.qq.com";
    private static final String HOST_V2 = "wx2.qq.com";

    private final long TIME_INIT = System.currentTimeMillis();
    private final AtomicBoolean FIRST_LOGIN = new AtomicBoolean(true);
    private final File folder;

    String uin;
    String sid;
    String dataTicket;
    private long time = TIME_INIT;
    private String host;
    private String uuid;
    private String skey;
    private String passticket;
    private RspInit.SyncKey synckey;

    WeChatApi(File folder) {
        this.folder = folder;
    }

    /**
     * 获取登录二维码
     *
     * @return 登录二维码网址
     */
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

    /**
     * 监听登录，循环请求该接口，如果用户扫描或授权登录，该接口立即返回，否则将会在25秒后返回
     *
     * @return 监听结果，code=200用户授权登录，code=201用户扫描二维码，code=408等待用户扫描或授权，其他则表示登录超时
     */
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
        }
        return rspLogin;
    }

    /**
     * 用户登录，返回uin,sid等重要信息，如果该接口返回数据为空，则uin，sid等数据在cookie中获取
     *
     * @param url 登录url
     */
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

    /**
     * 初始化，获取自身信息，好友列表，活跃群等
     *
     * @return 初始化结果
     */
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

    /**
     * 消息已读接口，去掉手机端的消息红点提示
     *
     * @param MyName 自己的UserName
     * @return 接口调用结果
     */
    RspStatusNotify webwxstatusnotify(String MyName) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxstatusnotify", host));
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqStatusNotify(new BaseRequest(uin, sid, skey), 3, MyName)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspStatusNotify.class);
    }

    /**
     * 获取联系人列表
     *
     * @return 联系人列表
     */
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

    /**
     * 批量获取联系人详细信息
     *
     * @param contactList 要获取的联系人列表
     * @return 联系人的详细信息
     */
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

    /**
     * 同步检查接口，需要无线循环请求该接口，如果有消息要同步，则该接口立即返回并携带参数，否则将在60秒左右返回
     *
     * @return 检查结果
     */
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

    /**
     * 同步接口，将服务端数据同步到本地，并更新本地SyncKey
     *
     * @return 获取到的数据
     */
    RspSync webwxsync() {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxsync", host));
        xUrl.param("sid", this.sid);
        xUrl.param("skey", this.skey);
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", this.passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqSync(new BaseRequest(uin, sid, skey), this.synckey)));
        RspSync rspSync = WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspSync.class);
        this.synckey = rspSync.SyncCheckKey;
        WeChatTools.LOGGER.info(String.format("更新syncKey:%s", synckey));
        return rspSync;
    }

    /**
     * 发送消息接口
     *
     * @param msg 需要发送的消息
     * @return 发送的结果
     */
    RspSendMsg webwxsendmsg(Msg msg) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxsendmsg", host));
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqSendMsg(new BaseRequest(uin, sid, skey), msg)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspSendMsg.class);
    }

    /**
     * 发送图片消息
     *
     * @param msg 需要发送的图片消息
     * @return 发送的结果
     */
    RspSendMsg webwxsendmsgimg(Msg msg) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxsendmsgimg", host)).param("fun", "async").param("f", "json");
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqSendMsg(new BaseRequest(uin, sid, skey), msg)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspSendMsg.class);
    }

    /**
     * 根据MsgId获取那条消息的图片
     *
     * @param msgId 消息ID
     * @param type  图片的类型，slave：小图，big：大图，不传该参数则获取普通大小
     * @return 获取到的图片文件
     */
    File webwxgetmsgimg(String msgId, String type) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxgetmsgimg", host));
        xUrl.param("MsgID", msgId);
        xUrl.param("skey", skey);
        if (!XTools.strEmpty(type)) {
            xUrl.param("type", type);
        }
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        return XTools.http(xUrl).file(folder.getPath() + File.separator + String.format("%d-%d", System.currentTimeMillis(), (int) (1000 * Math.random())));
    }

    /**
     * 根据MsgId获取那条消息的语音
     *
     * @param msgId 消息ID
     * @return 获取到的语音文件
     */
    File webwxgetvoice(String msgId) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxgetvoice", host));
        xUrl.param("msgid", msgId);
        xUrl.param("skey", skey);
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        return XTools.http(xUrl).file(folder.getPath() + File.separator + String.format("%d-%d", System.currentTimeMillis(), (int) (1000 * Math.random())));
    }

    /**
     * 根据MsgId获取那条消息的视频
     *
     * @param msgId 消息ID
     * @return 获取到的视频文件
     */
    File webwxgetvideo(String msgId) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxgetvideo", host));
        xUrl.param("msgid", msgId);
        xUrl.param("skey", skey);
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        return XTools.http(xUrl).file(folder.getPath() + File.separator + String.format("%d-%d", System.currentTimeMillis(), (int) (1000 * Math.random())));
    }

    /**
     * 发送好友请求
     *
     * @param userName      目标用户的UserName
     * @param verifyContent 验证消息
     * @return 发送的结果
     */
    RspVerifyUser webwxverifyuser(String userName, String verifyContent) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxverifyuser", host)).param("r", System.currentTimeMillis());
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqVerifyUser(new BaseRequest(uin, sid, skey), userName, verifyContent)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspVerifyUser.class);
    }

    /**
     * 修改用户备注
     *
     * @param userName   目标用户的UserName
     * @param remarkName 备注名称
     * @return 修改备注的结果
     */
    RspOplog webwxoplog(String userName, String remarkName) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxoplog", host));
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqOplog(new BaseRequest(uin, sid, skey), userName, remarkName)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspOplog.class);
    }

    /**
     * 创建聊天室
     *
     * @param topic      聊天室的名称
     * @param memberList 成员的UserName
     * @return 创建的结果
     */
    RspCreateChatroom webwxcreatechatroom(String topic, ArrayList<String> memberList) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxcreatechatroom", host)).param("r", System.currentTimeMillis());
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqCreateChatroom(new BaseRequest(uin, sid, skey), topic, memberList)));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspCreateChatroom.class);
    }

    /**
     * 添加或移除聊天室成员
     *
     * @param chatroom   聊天室的UserName
     * @param fun        addmember：添加成员，delmember：移除成员
     * @param memberList 成员列表
     * @return 添加或移除的结果
     */
    RspUpdateChatroom webwxupdatechartroom(String chatroom, String fun, ArrayList<String> memberList) {
        XUrl xUrl = XUrl.base(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxupdatechatroom", host)).param("fun", fun);
        if (!XTools.strEmpty(this.passticket)) {
            xUrl.param("pass_ticket", passticket);
        }
        XBody body = XBody.type(XBody.JSON).param(WeChatTools.GSON.toJson(new ReqUpdateChatroom(new BaseRequest(uin, sid, skey), chatroom, fun, XTools.strJoin(memberList, ","))));
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspUpdateChatroom.class);
    }

    /**
     * 上传资源文件
     *
     * @param fromUserName 消息的发送方UserName
     * @param toUserName   消息的接收方UserName
     * @param file         要上传的资源文件
     * @param mediatype    资源文件类型，pic：图片
     * @return 上传结果，包含了MediaId
     * @throws IOException 文件IO异常
     */
    RspUploadMedia webwxuploadmedia(String fromUserName, String toUserName, File file, String mediatype) throws IOException {
        XUrl xUrl = XUrl.base(String.format("https://file.%s/cgi-bin/mmwebwx-bin/webwxuploadmedia", host)).param("f", "json");
        XBody body = XBody.type(XBody.MULTIPART);
        body.param("id", "WU_FILE_0");
        body.param("name", file.getName());
        body.param("type", Files.probeContentType(Paths.get(file.getAbsolutePath())));
        body.param("lastModifiedDate", new Date(file.lastModified()));
        body.param("size", file.length());
        body.param("mediatype", mediatype);
        body.param("uploadmediarequest", WeChatTools.GSON.toJson(new ReqUploadMedia(new BaseRequest(uin, sid, skey), 2, file.length(), 0, file.length(), XTools.md5(file), 4, fromUserName, toUserName)));
        body.param("webwx_data_ticket", dataTicket);
        body.param("pass_ticket", XTools.strEmpty(passticket) ? "undefined" : passticket);
        body.param("filename", file);
        return WeChatTools.GSON.fromJson(WeChatTools.request(xUrl, body, "\\{"), RspUploadMedia.class);
    }
}