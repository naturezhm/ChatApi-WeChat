package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;
import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.logging.Level;

public class WeChatDemo {
    //网页微信登录时有两个重要的值（wxsid，wxuin）是在cookie中返回的，这里使用了默认的内存Cookie管理器
    public static final CookieManager cookieManager = new CookieManager();
    //新建一个模拟微信客户端，并绑定一个简单的监听器
    public static WeChatClient wechatClient = new WeChatClient(new WeChatClient.WeChatListener() {
        @Override
        public void onQRCode(String qrCode) {
            System.out.println("onQRCode:" + qrCode);
        }

        @Override
        public void onAvatar(String base64Avatar) {
            System.out.println("onAvatar:" + base64Avatar);
        }

        @Override
        public void onFailure(String reason) {
            System.out.println("onFailure:" + reason);
        }

        @Override
        public void onLogin() {
            System.out.println("onLogin");
            System.out.println(String.format("您有%d名好友、关注%d个公众号、活跃微信群%d个", wechatClient.uFriends().size(), wechatClient.uPublics().size(), wechatClient.uChatrooms().size()));
        }

        @Override
        public void onMessageText(String msgId, User userWhere, User userFrom, String text) {
            System.out.println("onMessage:" + text);
            //学习别人说话
            if (!userFrom.UserName.equals(wechatClient.uMe().UserName)) {
                wechatClient.mText(userWhere.UserName, text);
            }
        }

        @Override
        public void onMessageImage(String msgId, User userWhere, User userFrom, File image) {
        }

        @Override
        public void onMessageVoice(String msgId, User userWhere, User userFrom, File voice) {
        }

        @Override
        public void onMessageCard(String msgId, User userWhere, User userFrom, String userName, String nick, int gender) {
        }

        @Override
        public void onMessageVideo(String msgId, User userWhere, User userFrom, File thumbnail, File video) {
        }

        @Override
        public void onMessageOther(String msgId, User userWhere, User userFrom) {
        }

        @Override
        public void onNotify(AddMsg addMsg) {
        }

        @Override
        public void onSystem(AddMsg addMsg) {
        }

        @Override
        public void onLogout() {
            System.out.println("onLogout");
        }
    }, cookieManager, null, Level.ALL);

    public static void main(String[] args) {
        CookieHandler.setDefault(cookieManager);
        //启动模拟微信客户端
        wechatClient.startup();
        //查看模拟微信客户端是否正在运行
        //wechatClient.isWorking();
        //关闭模拟微信客户端
        //wechatClient.shutdown();
    }
}