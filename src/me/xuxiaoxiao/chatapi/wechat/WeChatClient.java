package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;
import me.xuxiaoxiao.chatapi.wechat.entity.User;
import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.chatapi.wechat.protocol.*;
import me.xuxiaoxiao.xtools.XTools;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public final class WeChatClient {
    public static final String LOGIN_TIMEOUT = "登陆超时";
    public static final String LOGIN_EXCEPTION = "登陆异常";
    public static final String INIT_EXCEPTION = "初始化异常";
    public static final String LISTEN_EXCEPTION = "监听异常";

    private final WeChatThread wxThread = new WeChatThread();
    private final WeChatApi wxAPI;
    private final WeChatContacts wxContacts;
    private final WeChatListener wxListener;
    private final CookieManager cookieManager;

    private volatile boolean isOnline = false;

    public WeChatClient(WeChatListener wxListener, CookieManager cookieManager, Level level) {
        this.wxAPI = new WeChatApi();
        this.wxContacts = new WeChatContacts(wxAPI);

        this.wxListener = wxListener;
        this.cookieManager = cookieManager;
        WeChatTools.LOGGER.setLevel(level);
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public void startup() {
        wxThread.start();
    }

    public boolean isWorking() {
        return !wxThread.isInterrupted();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void shutdown() {
        wxThread.interrupt();
    }

    public User me() {
        return wxContacts.getMe();
    }

    public User uFriend(String userName) {
        return wxContacts.getFriend(userName);
    }

    public ArrayList<User> uFriends() {
        return wxContacts.getFriends();
    }

    public User uPublic(String userName) {
        return wxContacts.getPublic(userName);
    }

    public ArrayList<User> uPublics() {
        return wxContacts.getPublics();
    }

    public User uChatroom(String userName) {
        return wxContacts.getChatroom(userName);
    }

    public ArrayList<User> uChatrooms() {
        return wxContacts.getCharrooms();
    }

    public User contact(String userName) {
        return wxContacts.getContact(userName);
    }

    public void sendTextMsg(String toUserName, String msgContent) {
        WeChatTools.LOGGER.info(String.format("正在向%s发送消息：%s", toUserName, msgContent));
        wxAPI.webwxsendmsg(new ReqSendMsg.Msg(WeChatTools.TYPE_TEXT, msgContent, wxContacts.getMe().UserName, toUserName));
    }

    public interface WeChatListener {
        void onQRCode(String qrCode);

        void onAvatar(String base64Avatar);

        void onFailure(String reason);

        void onLogin();

        void onMessage(AddMsg addMsg);

        void onNotify(AddMsg addMsg);

        void onSystem(AddMsg addMsg);

        void onLogout();
    }

    private class WeChatThread extends Thread {

        @Override
        public void run() {
            String loginErr = login();
            if (!XTools.strEmpty(loginErr)) {
                wxListener.onFailure(loginErr);
                return;
            }
            String initErr = initial();
            if (!XTools.strEmpty(initErr)) {
                wxListener.onFailure(initErr);
                return;
            }
            isOnline = true;
            wxListener.onLogin();
            String listenErr = listen();
            if (!XTools.strEmpty(listenErr)) {
                wxListener.onFailure(listenErr);
                return;
            }
            isOnline = false;
            wxListener.onLogout();
        }

        private String login() {
            try {
                wxListener.onQRCode(wxAPI.jslogin());
                while (true) {
                    RspLogin rspLogin = wxAPI.login();
                    switch (rspLogin.code) {
                        case 200:
                            wxAPI.webwxnewloginpage(rspLogin.redirectUri);
                            return null;
                        case 201:
                            wxListener.onAvatar(rspLogin.userAvatar);
                            break;
                        case 408:
                            WeChatTools.LOGGER.info("等待用户授权登录");
                            break;
                        default:
                            return LOGIN_TIMEOUT;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString() + Arrays.toString(e.getStackTrace());
            }
        }

        private String initial() {
            try {
                for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                    if (cookie.getName().equalsIgnoreCase("wxsid")) {
                        wxAPI.sid = cookie.getValue();
                    }
                    if (cookie.getName().equalsIgnoreCase("wxuin")) {
                        wxAPI.uin = cookie.getValue();
                    }
                }

                RspInit rspInit = wxAPI.webwxinit();
                wxContacts.setMe(rspInit.User);

                RspGetContact rspGetContact = wxAPI.webwxgetcontact();
                for (User user : rspGetContact.MemberList) {
                    wxContacts.addContact(user);
                }

                ArrayList<Contact> contacts = new ArrayList<>();
                if (rspInit.ContactList != null) {
                    for (User user : rspInit.ContactList) {
                        if (user.UserName.startsWith("@")) {
                            Contact item = new Contact(user.UserName, "");
                            contacts.add(item);
                        }
                    }
                }
                wxContacts.loadContacts(contacts);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString() + Arrays.toString(e.getStackTrace());
            }
        }

        private String listen() {
            try {
                wxAPI.webwxstatusnotify(me().UserName);
                while (!isInterrupted()) {
                    RspSyncCheck rspSyncCheck = wxAPI.synccheck();
                    if (rspSyncCheck.retcode > 0) {
                        return null;
                    } else if (rspSyncCheck.selector > 0) {
                        RspSync rspSync = wxAPI.webwxsync();
                        if (rspSync.ModContactList != null) {//被拉入群第一条消息，群里有人加入,群里踢人之后第一条信息，添加好友
                            for (User user : rspSync.ModContactList) {
                                wxContacts.addContact(user);
                            }
                        }
                        if (rspSync.DelContactList != null) {//删除好友，删除群后的任意一条消息
                            for (User user : rspSync.DelContactList) {
                                wxContacts.rmvContact(user.UserName);
                            }
                        }
                        if (rspSync.ModChatRoomMemberList != null) {
                            for (User user : rspSync.ModChatRoomMemberList) {
                                wxContacts.addContact(user);
                            }
                        }
                        if (rspSync.AddMsgList != null) {
                            for (AddMsg addMsg : rspSync.AddMsgList) {
                                switch (addMsg.MsgType) {
                                    case WeChatTools.TYPE_NOTIFY:
                                        ArrayList<Contact> contacts = new ArrayList<>();
                                        for (String contact : addMsg.StatusNotifyUserName.split(",")) {
                                            if (wxContacts.getContact(contact) == null && contact.startsWith("@")) {
                                                contacts.add(new Contact(contact, ""));
                                                if (contacts.size() >= 50) {
                                                    wxContacts.loadContacts(contacts);
                                                    contacts.clear();
                                                }
                                            }
                                        }
                                        if (contacts.size() > 0) {
                                            wxContacts.loadContacts(contacts);
                                        }
                                        wxListener.onNotify(addMsg);
                                        break;
                                    case WeChatTools.TYPE_SYSTEM:
                                        wxListener.onSystem(addMsg);
                                        break;
                                    default:
                                        wxListener.onMessage(addMsg);
                                        break;
                                }
                            }
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString() + Arrays.toString(e.getStackTrace());
            }
        }
    }
}
