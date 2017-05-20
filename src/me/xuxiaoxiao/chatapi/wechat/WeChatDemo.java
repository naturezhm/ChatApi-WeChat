package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;

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
        public void onMessage(AddMsg addMsg) {
            System.out.println("onMessage:" + addMsg.Content);
            //学习别人说话
            if (addMsg.MsgType == WeChatTools.TYPE_TEXT && !WeChatTools.textMsgSender(addMsg).equals(wechatClient.me().UserName)) {
                wechatClient.sendTextMsg(addMsg.FromUserName, WeChatTools.textMsgContent(addMsg));
            }
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
    }, cookieManager, Level.ALL);

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