# ChatApi-WeChat
微信聊天接口，可用于制作微信聊天机器人

## 这是什么？
* 这是一个简易的微信聊天客户端
* 该客户端使用的接口来自于网页版微信

## 有何优点？
* 对接口和流程进行了封装，更加简单易用
* 暴露了一个监听器，可以自己实现监听器以开发自己的业务功能

## 待改进的地方
* 目前只能解析和发送文字类的消息

## 如何使用
* 首先下载必要的依赖，[gson](https://github.com/google/gson)和[xtools-common](https://github.com/xuxiaoxiao-xxx/XTools-Common)
* 以下是一个学别人说话的小机器人，用到了该库提供的大部分功能
```java
public class WeChatDemo {
    //网页微信登录时有两个重要的值（wxsid，wxuin）是在cookie中返回的，这里使用了默认的内存Cookie管理器
    public static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
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
            System.out.println(String.format("您有%d名好友、关注%d个公众号、活跃微信群%d个", wechatClient.friends().size(), wechatClient.publics().size(), wechatClient.chatrooms().size()));
        }

        @Override
        public void onMessage(RspSync.AddMsg addMsg) {
            System.out.println("onMessage:" + addMsg.Content);
            //学习别人说话
            if (addMsg.MsgType == WeChatTools.TYPE_TEXT && !WeChatTools.textMsgSender(addMsg).equals(wechatClient.me().UserName)) {
                wechatClient.sendTextMsg(addMsg.FromUserName, WeChatTools.textMsgContent(addMsg));
            }
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
```