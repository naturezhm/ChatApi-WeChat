package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;
import me.xuxiaoxiao.chatapi.wechat.entity.Msg;
import me.xuxiaoxiao.chatapi.wechat.entity.User;
import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.chatapi.wechat.protocol.*;
import me.xuxiaoxiao.xtools.XTools;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * 模拟网页微信客户端
 */
public final class WeChatClient {
    public static final String LOGIN_TIMEOUT = "登陆超时";
    public static final String LOGIN_EXCEPTION = "登陆异常";
    public static final String INIT_EXCEPTION = "初始化异常";
    public static final String LISTEN_EXCEPTION = "监听异常";

    private final WeChatThread wxThread = new WeChatThread();
    private final WeChatContacts wxContacts = new WeChatContacts();
    private final WeChatApi wxAPI;
    private final WeChatListener wxListener;
    private final CookieManager cookieManager;

    public WeChatClient(WeChatListener wxListener, CookieManager cookieManager, File folder, Handler handler) {
        this.wxAPI = new WeChatApi(folder == null ? new File("") : folder);
        this.wxListener = wxListener;
        this.cookieManager = cookieManager;
        if (handler != null) {
            WeChatTools.LOGGER.setLevel(handler.getLevel());
            WeChatTools.LOGGER.setUseParentHandlers(false);
            WeChatTools.LOGGER.addHandler(handler);
        }
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    /**
     * 获取联系人信息
     *
     * @param contacts 要获取的联系人的列表
     */
    private void loadContacts(List<Contact> contacts, boolean useCache) {
        if (useCache) {
            contacts.removeIf(contact -> wxContacts.getContact(contact.UserName) != null);
        }
        if (contacts.size() > 50) {
            LinkedList<Contact> temp = new LinkedList<>();
            for (Contact contact : contacts) {
                temp.add(contact);
                if (temp.size() >= 50) {
                    RspBatchGetContact rspBatchGetContact = wxAPI.webwxbatchgetcontact(contacts);
                    for (User user : rspBatchGetContact.ContactList) {
                        wxContacts.addContact(user);
                    }
                    temp.clear();
                }
            }
            contacts = temp;
        }
        if (contacts.size() > 0) {
            RspBatchGetContact rspBatchGetContact = wxAPI.webwxbatchgetcontact(contacts);
            for (User user : rspBatchGetContact.ContactList) {
                wxContacts.addContact(user);
            }
        }
    }

    /**
     * 启动客户端，注意：一个客户端类的实例只能被启动一次
     */
    public void startup() {
        wxThread.start();
    }

    /**
     * 客户端是否正在运行
     *
     * @return 是否正在运行
     */
    public boolean isWorking() {
        return !wxThread.isInterrupted();
    }

    /**
     * 关闭客户端，注意：关闭后的客户端不能再被启动
     */
    public void shutdown() {
        wxThread.interrupt();
    }

    /**
     * 获取当前登录的用户信息
     *
     * @return 当前登录的用户信息
     */
    public User userMe() {
        return wxContacts.getMe();
    }

    /**
     * 根据UserName获取用户好友
     *
     * @param userName 好友的UserName
     * @return 好友的信息
     */
    public User userFriend(String userName) {
        return wxContacts.getFriend(userName);
    }

    /**
     * 获取用户好友列表
     *
     * @return 用户好友列表
     */
    public ArrayList<User> userFriends() {
        return wxContacts.getFriends();
    }

    /**
     * 根据UserName获取用户公众号
     *
     * @param userName 公众号的UserName
     * @return 公众号的信息
     */
    public User userPublic(String userName) {
        return wxContacts.getPublic(userName);
    }

    /**
     * 获取用户公众号列表
     *
     * @return 公众号列表
     */
    public ArrayList<User> userPublics() {
        return wxContacts.getPublics();
    }

    /**
     * 根据聊天室UserName获取聊天室
     *
     * @param userName 聊天室UserName
     * @return 聊天室信息
     */
    public User userChatroom(String userName) {
        return wxContacts.getChatroom(userName);
    }

    /**
     * 获取用户聊天室列表
     *
     * @return 聊天室列表
     */
    public ArrayList<User> userChatrooms() {
        return wxContacts.getChatrooms();
    }

    /**
     * 根据联系人UserName获取用户联系人，好友、公众号、群、群成员等
     *
     * @param userName 联系人UserName
     * @return 联系人信息
     */
    public User userContact(String userName) {
        return wxContacts.getContact(userName);
    }

    /**
     * 发送文字消息
     *
     * @param toUserName 目标用户的UserName
     * @param text       文字内容
     */
    public void sendText(String toUserName, String text) {
        WeChatTools.LOGGER.info(String.format("向（%s）发送消息：%s", toUserName, text));
        wxAPI.webwxsendmsg(new Msg(WeChatTools.TYPE_TEXT, null, text, wxContacts.getMe().UserName, toUserName));
    }

    /**
     * 发送图片消息
     *
     * @param toUserName 目标用户的UserName
     * @param image      图片文件
     * @throws IOException 文件IO异常
     */
    public void sendImage(String toUserName, File image) throws IOException {
        WeChatTools.LOGGER.info(String.format("向（%s）发送图片：%s", toUserName, image.getAbsolutePath()));
        RspUploadMedia rspUploadMedia = wxAPI.webwxuploadmedia(wxContacts.getMe().UserName, toUserName, image, "pic");
        wxAPI.webwxsendmsgimg(new Msg(WeChatTools.TYPE_IMAGE, rspUploadMedia.MediaId, "", wxContacts.getMe().UserName, toUserName));
    }

    /**
     * 根据消息ID和图片类型获取图片
     *
     * @param msgId 消息ID
     * @param type  图片类型，slave：小图，big：大图，null：普通尺寸
     * @return 图片文件
     */
    public File fetchImage(String msgId, String type) {
        if ("slave".equals(type) || "big".equals(type)) {
            return wxAPI.webwxgetmsgimg(msgId, type);
        } else {
            return wxAPI.webwxgetmsgimg(msgId, null);
        }
    }

    /**
     * 发送好友申请
     *
     * @param userName      目标用户UserName
     * @param verifyContent 验证消息
     */
    public void sendVerify(String userName, String verifyContent) {
        WeChatTools.LOGGER.info(String.format("发送好友（%s）申请：%s", userName, verifyContent));
        wxAPI.webwxverifyuser(2, userName, "", verifyContent);
    }

    /**
     * 同意好友申请
     *
     * @param userName     目标用户UserName
     * @param verifyTicket 验证票据
     */
    public void passVerify(String userName, String verifyTicket) {
        WeChatTools.LOGGER.info(String.format("通过好友（%s）申请", userName));
        wxAPI.webwxverifyuser(3, userName, verifyTicket, "");
    }

    /**
     * 修改用户备注名
     *
     * @param userName 目标用户UserName
     * @param remark   备注名称
     */
    public void editRemark(String userName, String remark) {
        WeChatTools.LOGGER.info(String.format("修改（%s）的备注：%s", userName, remark));
        wxAPI.webwxoplog(userName, remark);
    }

    /**
     * 创建聊天室
     *
     * @param topic      聊天室名称
     * @param memberList 成员列表
     * @return 聊天室的UserName
     */
    public String createChatroom(String topic, List<String> memberList) {
        RspCreateChatroom rspCreateChatroom = wxAPI.webwxcreatechatroom(topic, memberList);
        WeChatTools.LOGGER.info(String.format("创建群：%s（%s），成员：%s", topic, rspCreateChatroom.ChatRoomName, memberList));
        return rspCreateChatroom.ChatRoomName;
    }

    /**
     * 添加聊天室的成员
     *
     * @param chatRoomName 聊天室的UserName
     * @param users        要添加的人员的UserName，必须是自己的好友
     */
    public void addChatroomMember(String chatRoomName, List<String> users) {
        WeChatTools.LOGGER.info(String.format("为群（%s）添加成员：%s", chatRoomName, users));
        wxAPI.webwxupdatechartroom(chatRoomName, "addmember", users);
    }

    /**
     * 移除聊天室的成员
     *
     * @param chatRoomName 聊天室的UserName
     * @param users        要移除的人员的UserName，必须是聊天室的成员，而且自己是管理员
     */
    public void delChatroomMember(String chatRoomName, List<String> users) {
        WeChatTools.LOGGER.info(String.format("为群（%s）删除成员：%s", chatRoomName, users));
        wxAPI.webwxupdatechartroom(chatRoomName, "delmember", users);
    }

    /**
     * 模拟网页微信客户端监听器
     */
    public interface WeChatListener {
        /**
         * 获取到用户登录的二维码
         *
         * @param qrCode 用户登录二维码的url
         */
        void onQRCode(String qrCode);

        /**
         * 获取用户头像，base64编码
         *
         * @param base64Avatar base64编码的用户头像
         */
        void onAvatar(String base64Avatar);

        /**
         * 模拟网页微信客户端异常退出
         *
         * @param reason 错误原因
         */
        void onFailure(String reason);

        /**
         * 用户登录并初始化成功
         */
        void onLogin();

        /**
         * 用户获取到文字消息
         *
         * @param msgId     消息ID
         * @param userWhere 消息来源
         * @param userFrom  消息发送者
         * @param content   文字内容
         */
        void onMessageText(String msgId, User userWhere, User userFrom, String content);

        /**
         * 用户获取到图像消息
         *
         * @param msgId     消息ID
         * @param userWhere 消息来源
         * @param userFrom  消息发送者
         * @param image     图像文件
         */
        void onMessageImage(String msgId, User userWhere, User userFrom, File image);

        /**
         * 用户获取到语音消息
         *
         * @param msgId     消息ID
         * @param userWhere 消息来源
         * @param userFrom  消息发送者
         * @param voice     语音文件
         */
        void onMessageVoice(String msgId, User userWhere, User userFrom, File voice);

        /**
         * 用户获取到视频消息
         *
         * @param msgId     消息ID
         * @param userWhere 消息来源
         * @param userFrom  消息发送者
         * @param thumbnail 视频封面
         * @param video     视频文件
         */
        void onMessageVideo(String msgId, User userWhere, User userFrom, File thumbnail, File video);

        /**
         * 用户获取到名片消息
         *
         * @param msgId         消息ID
         * @param userWhere     消息来源
         * @param userFrom      消息发送者
         * @param recommendInfo 被推荐人信息
         */
        void onMessageCard(String msgId, User userWhere, User userFrom, AddMsg.RecommendInfo recommendInfo);

        /**
         * 用户获取到好友请求
         *
         * @param msgId         消息ID
         * @param userWhere     消息来源
         * @param userFrom      消息发送者
         * @param recommendInfo 被推荐人信息
         */
        void onMessageVerify(String msgId, User userWhere, User userFrom, AddMsg.RecommendInfo recommendInfo);

        /**
         * 用户获取到未知类型的消息
         *
         * @param msgId     消息ID
         * @param userWhere 消息来源
         * @param userFrom  消息发送者
         */
        void onMessageOther(String msgId, User userWhere, User userFrom);

        /**
         * 用户获取到提醒消息
         *
         * @param addMsg 提醒消息
         */
        void onNotify(AddMsg addMsg);

        /**
         * 用户获取到系统消息
         *
         * @param addMsg 系统消息
         */
        void onSystem(AddMsg addMsg);

        /**
         * 用户获取到未知类型消息
         *
         * @param addMsg 未知类型消息
         */
        void onUnknown(AddMsg addMsg);

        /**
         * 模拟网页微信客户端正常退出
         */
        void onLogout();
    }

    /**
     * 模拟网页微信客户端工作线程
     */
    private class WeChatThread extends Thread {

        @Override
        public void run() {
            //用户登录
            WeChatTools.LOGGER.fine("正在登录");
            String loginErr = login();
            if (!XTools.strEmpty(loginErr)) {
                WeChatTools.LOGGER.severe(String.format("登录出现错误：%s", loginErr));
                wxListener.onFailure(loginErr);
                return;
            }
            //用户初始化
            WeChatTools.LOGGER.fine("正在初始化");
            String initErr = initial();
            if (!XTools.strEmpty(initErr)) {
                WeChatTools.LOGGER.severe(String.format("初始化出现错误：%s", initErr));
                wxListener.onFailure(initErr);
                return;
            }
            wxListener.onLogin();
            //同步消息
            WeChatTools.LOGGER.fine("正在监听消息");
            String listenErr = listen();
            if (!XTools.strEmpty(listenErr)) {
                WeChatTools.LOGGER.severe(String.format("监听消息出现错误：%s", listenErr));
                wxListener.onFailure(listenErr);
                return;
            }
            //退出
            wxListener.onLogout();
        }

        /**
         * 用户登录
         *
         * @return 登录时异常原因，为null表示正常登录
         */
        private String login() {
            try {
                String qrCode = wxAPI.jslogin();
                WeChatTools.LOGGER.finer(String.format("等待扫描二维码：%s", qrCode));
                wxListener.onQRCode(qrCode);
                while (true) {
                    RspLogin rspLogin = wxAPI.login();
                    switch (rspLogin.code) {
                        case 200:
                            WeChatTools.LOGGER.finer("已授权登录");
                            wxAPI.webwxnewloginpage(rspLogin.redirectUri);
                            return null;
                        case 201:
                            WeChatTools.LOGGER.finer("已扫描二维码");
                            wxListener.onAvatar(rspLogin.userAvatar);
                            break;
                        case 408:
                            WeChatTools.LOGGER.finer("等待授权登录");
                            break;
                        default:
                            WeChatTools.LOGGER.warning("登录超时");
                            return LOGIN_TIMEOUT;
                    }
                }
            } catch (Exception e) {
                WeChatTools.LOGGER.warning(String.format("登录异常：%s", e.getMessage()));
                e.printStackTrace();
                return e.toString() + Arrays.toString(e.getStackTrace());
            }
        }

        /**
         * 初始化
         *
         * @return 初始化异常原因，为null表示正常初始化
         */
        private String initial() {
            try {
                WeChatTools.LOGGER.finer("正在获取Cookie");
                for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                    if (cookie.getName().equalsIgnoreCase("wxsid")) {
                        wxAPI.sid = cookie.getValue();
                    } else if (cookie.getName().equalsIgnoreCase("wxuin")) {
                        wxAPI.uin = cookie.getValue();
                    } else if (cookie.getName().equalsIgnoreCase("webwx_data_ticket")) {
                        wxAPI.dataTicket = cookie.getValue();
                    }
                }

                //获取自身信息
                WeChatTools.LOGGER.finer("正在获取自身信息");
                RspInit rspInit = wxAPI.webwxinit();
                wxContacts.setMe(rspInit.User);

                //获取好友、群、公众号列表
                WeChatTools.LOGGER.finer("正在获取好友、群、公众号列表");
                RspGetContact rspGetContact = wxAPI.webwxgetcontact();
                for (User user : rspGetContact.MemberList) {
                    wxContacts.addContact(user);
                }

                //获取联系人详细信息
                WeChatTools.LOGGER.finer("正在获取联系人详细信息");
                LinkedList<Contact> contacts = new LinkedList<>();
                if (rspInit.ContactList != null) {
                    for (User user : rspInit.ContactList) {
                        contacts.add(new Contact(user.UserName, ""));
                    }
                }
                loadContacts(contacts, false);
                return null;
            } catch (Exception e) {
                WeChatTools.LOGGER.warning(String.format("初始化异常：%s", e.getMessage()));
                e.printStackTrace();
                return e.toString() + Arrays.toString(e.getStackTrace());
            }
        }

        /**
         * 循环同步消息
         *
         * @return 同步消息的异常原因，为null表示正常结束
         */
        private String listen() {
            try {
                wxAPI.webwxstatusnotify(userMe().UserName);
                while (!isInterrupted()) {
                    WeChatTools.LOGGER.finer("正在监听信息");
                    RspSyncCheck rspSyncCheck = wxAPI.synccheck();
                    if (rspSyncCheck.retcode > 0) {
                        WeChatTools.LOGGER.finer(String.format("停止监听信息：%d", rspSyncCheck.retcode));
                        return null;
                    } else if (rspSyncCheck.selector > 0) {
                        RspSync rspSync = wxAPI.webwxsync();
                        if (rspSync.ModContactList != null) {//被拉入群第一条消息，群里有人加入,群里踢人之后第一条信息，添加好友
                            for (User user : rspSync.ModContactList) {
                                WeChatTools.LOGGER.finer(String.format("变更联系人（%s）", user.UserName));
                                wxContacts.addContact(user);
                            }
                        }
                        if (rspSync.DelContactList != null) {//删除好友，删除群后的任意一条消息
                            for (User user : rspSync.DelContactList) {
                                WeChatTools.LOGGER.finer(String.format("删除联系人（%s）", user.UserName));
                                wxContacts.rmvContact(user.UserName);
                            }
                        }
                        if (rspSync.ModChatRoomMemberList != null) {
                            for (User user : rspSync.ModChatRoomMemberList) {
                                WeChatTools.LOGGER.finer(String.format("变更群成员（%s）", user.UserName));
                                wxContacts.addContact(user);
                            }
                        }
                        if (rspSync.AddMsgList != null) {
                            for (AddMsg addMsg : rspSync.AddMsgList) {
                                if (WeChatTools.LOGGER.isLoggable(Level.FINER)) {
                                    WeChatTools.LOGGER.finer(String.format("收到新消息：%s", WeChatTools.GSON.toJson(addMsg)));
                                }
                                loadContacts(new ArrayList<>(Arrays.asList(new Contact(addMsg.FromUserName, ""), new Contact(WeChatTools.msgSender(addMsg), ""))), true);
                                switch (addMsg.MsgType) {
                                    case WeChatTools.TYPE_TEXT:
                                        wxListener.onMessageText(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), WeChatTools.msgContent(addMsg));
                                        break;
                                    case WeChatTools.TYPE_IMAGE:
                                        wxListener.onMessageImage(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), wxAPI.webwxgetmsgimg(addMsg.MsgId, "slave"));
                                        break;
                                    case WeChatTools.TYPE_VOICE:
                                        wxListener.onMessageVoice(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), wxAPI.webwxgetvoice(addMsg.MsgId));
                                        break;
                                    case WeChatTools.TYPE_VIDEO:
                                        wxListener.onMessageVideo(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), wxAPI.webwxgetmsgimg(addMsg.MsgId, "slave"), wxAPI.webwxgetvideo(addMsg.MsgId));
                                        break;
                                    case WeChatTools.TYPE_CARD:
                                        wxListener.onMessageCard(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), addMsg.RecommendInfo);
                                        break;
                                    case WeChatTools.TYPE_VERIFY:
                                        wxListener.onMessageVerify(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), addMsg.RecommendInfo);
                                        break;
                                    case WeChatTools.TYPE_FACE:
                                        wxListener.onMessageImage(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)), wxAPI.webwxgetmsgimg(addMsg.MsgId, "big"));
                                        break;
                                    case WeChatTools.TYPE_OTHER:
                                        wxListener.onMessageOther(addMsg.MsgId, wxContacts.getContact(addMsg.FromUserName), wxContacts.getContact(WeChatTools.msgSender(addMsg)));
                                        break;
                                    case WeChatTools.TYPE_NOTIFY:
                                        LinkedList<Contact> contacts = new LinkedList<>();
                                        for (String contact : addMsg.StatusNotifyUserName.split(",")) {
                                            contacts.add(new Contact(contact, ""));
                                        }
                                        loadContacts(contacts, true);
                                        wxListener.onNotify(addMsg);
                                        break;
                                    case WeChatTools.TYPE_SYSTEM:
                                        wxListener.onSystem(addMsg);
                                        break;
                                    default:
                                        wxListener.onUnknown(addMsg);
                                        break;
                                }
                            }
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                WeChatTools.LOGGER.warning(String.format("监听消息异常：%s", e.getMessage()));
                e.printStackTrace();
                return e.toString() + Arrays.toString(e.getStackTrace());
            }
        }
    }
}
