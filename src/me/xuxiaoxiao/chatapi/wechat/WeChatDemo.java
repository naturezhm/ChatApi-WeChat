package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;
import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Arrays;
import java.util.Scanner;

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
            System.out.println(String.format("您有%d名好友、关注%d个公众号、活跃微信群%d个", wechatClient.userFriends().size(), wechatClient.userPublics().size(), wechatClient.userChatrooms().size()));
        }

        @Override
        public void onMessageText(String msgId, User userWhere, User userFrom, String text) {
            System.out.println("onMessageText:" + text);
            //学习别人说话
            if (!userFrom.UserName.equals(wechatClient.userMe().UserName)) {
                wechatClient.sendText(userWhere.UserName, text);
            }
        }

        @Override
        public void onMessageImage(String msgId, User userWhere, User userFrom, File image) {
            System.out.println("onMessageImage");
        }

        @Override
        public void onMessageVoice(String msgId, User userWhere, User userFrom, File voice) {
            System.out.println("onMessageVoice");
        }

        @Override
        public void onMessageVideo(String msgId, User userWhere, User userFrom, File thumbnail, File video) {
            System.out.println("onMessageVideo");
        }

        @Override
        public void onMessageCard(String msgId, User userWhere, User userFrom, AddMsg.RecommendInfo recommendInfo) {
            System.out.println(String.format("onMessageCard：%s", WeChatTools.GSON.toJson(recommendInfo)));
        }

        @Override
        public void onMessageVerify(String msgId, User userWhere, User userFrom, AddMsg.RecommendInfo recommendInfo) {
            System.out.println(String.format("onMessageVerify：%s", WeChatTools.GSON.toJson(recommendInfo)));
        }

        @Override
        public void onMessageOther(String msgId, User userWhere, User userFrom) {
            System.out.println("onMessageOther");
        }

        @Override
        public void onNotify(AddMsg addMsg) {
            System.out.println("onNotify");
        }

        @Override
        public void onSystem(AddMsg addMsg) {
            System.out.println("onSystem");
        }

        @Override
        public void onUnknown(AddMsg addMsg) {
            System.out.println("onUnknown");
        }

        @Override
        public void onLogout() {
            System.out.println("onLogout");
        }
    }, cookieManager, null, null);

    public static void main(String[] args) {
        //设置CookieManager
        CookieHandler.setDefault(cookieManager);
        //启动模拟微信客户端
        wechatClient.startup();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入指令");
            switch (scanner.nextLine()) {
                case "sendText": {
                    System.out.println("toUserName:");
                    String toUserName = scanner.nextLine();
                    System.out.println("textContent:");
                    String text = scanner.nextLine();
                    wechatClient.sendText(toUserName, text);
                }
                break;
                case "sendImage": {
                    try {
                        System.out.println("toUserName:");
                        String toUserName = scanner.nextLine();
                        System.out.println("imagePath:");
                        File image = new File(scanner.nextLine());
                        wechatClient.sendImage(toUserName, image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case "sendVerify": {
                    System.out.println("userName:");
                    String userName = scanner.nextLine();
                    System.out.println("verifyContent:");
                    String verifyContent = scanner.nextLine();
                    wechatClient.sendVerify(userName, verifyContent);
                }
                break;
                case "passVerify": {
                    System.out.println("userName:");
                    String userName = scanner.nextLine();
                    System.out.println("verifyTicket:");
                    String verifyTicket = scanner.nextLine();
                    wechatClient.passVerify(userName, verifyTicket);
                }
                break;
                case "editRemark": {
                    System.out.println("userName:");
                    String userName = scanner.nextLine();
                    System.out.println("remarkName:");
                    String remark = scanner.nextLine();
                    wechatClient.editRemark(userName, remark);
                }
                break;
                case "createChatroom": {
                    System.out.println("topic:");
                    String topic = scanner.nextLine();
                    System.out.println("members,split by ',':");
                    String members = scanner.nextLine();
                    String chatroomName = wechatClient.createChatroom(topic, Arrays.asList(members.split(",")));
                    System.out.println("create chatroom " + chatroomName);
                }
                break;
                case "addChatroomMember": {
                    System.out.println("chatRoomName:");
                    String chatroomName = scanner.nextLine();
                    System.out.println("members,split by ',':");
                    String members = scanner.nextLine();
                    wechatClient.addChatroomMember(chatroomName, Arrays.asList(members.split(",")));
                }
                break;
                case "delChatroomMember": {
                    System.out.println("chatRoomName:");
                    String chatroomName = scanner.nextLine();
                    System.out.println("members,split by ',':");
                    String members = scanner.nextLine();
                    wechatClient.delChatroomMember(chatroomName, Arrays.asList(members.split(",")));
                }
                break;
                case "quit":
                    System.out.println("logging out");
                    wechatClient.shutdown();
                    return;
                default:
                    System.out.println("未知指令");
                    break;
            }
        }
    }
}