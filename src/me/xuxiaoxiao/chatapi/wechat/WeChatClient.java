package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.chatapi.wechat.protocol.*;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit.User;
import me.xuxiaoxiao.xtools.XTools;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.ArrayList;
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

    public ArrayList<User> friends() {
        return wxContacts.getFriends();
    }

    public ArrayList<User> publics() {
        return wxContacts.getPublics();
    }

    public ArrayList<User> chatrooms() {
        return wxContacts.getCharrooms();
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

        void onMessage(RspSync.AddMsg addMsg);

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
                return LOGIN_EXCEPTION;
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
                        if (user.UserName.startsWith("@@")) {
                            Contact item = new Contact(user.UserName, "");
                            contacts.add(item);
                        }
                    }
                }
                wxContacts.loadContacts(contacts);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return INIT_EXCEPTION;
            }
        }

        private String listen() {
            try {
                while (!isInterrupted()) {
                    RspSyncCheck rspSyncCheck = wxAPI.synccheck();
                    if (rspSyncCheck.retcode > 0) {
                        return null;
                    } else if (rspSyncCheck.selector > 0) {
                        RspSync rspSync = wxAPI.webwxsync();
                        if (rspSync.AddMsgList != null) {
                            for (RspSync.AddMsg addMsg : rspSync.AddMsgList) {
                                if (addMsg.FromUserName.startsWith("@@") && wxContacts.getChatroom(addMsg.FromUserName) == null) {
                                    ArrayList<Contact> contacts = new ArrayList<>();
                                    contacts.add(new Contact(addMsg.FromUserName, ""));
                                    wxContacts.loadContacts(contacts);
                                }
                                wxListener.onMessage(addMsg);
                            }
                        }
                        if (rspSync.ModContactList != null) {
                            for (User user : rspSync.ModContactList) {
                                wxContacts.addContact(user);
                            }
                        }
                        if (rspSync.DelContactList != null) {
                            for (User user : rspSync.DelContactList) {
                                wxContacts.rmvContact(user.UserName);
                            }
                        }
                        if (rspSync.ModChatRoomMemberList != null) {
                            for (User user : rspSync.ModChatRoomMemberList) {
                                wxContacts.addContact(user);
                            }
                        }
                    }
                    if (System.currentTimeMillis() - wxAPI.lastNotify > 5 * 60 * 1000) {
                        wxAPI.webwxstatusnotify(wxContacts.getMe().UserName);
                        wxAPI.lastNotify = System.currentTimeMillis();
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return LISTEN_EXCEPTION;
            }
        }
    }
}
