package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.User;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟网页微信客户端联系人
 */
final class WeChatContacts {
    private final ConcurrentHashMap<String, User> friends = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> publics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> chatrooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> contacts = new ConcurrentHashMap<>();
    private User me;

    /**
     * 获取自身信息
     *
     * @return 自身信息
     */
    User getMe() {
        return this.me;
    }

    /**
     * 设置自身信息
     *
     * @param me 自身信息
     */
    void setMe(User me) {
        this.me = me;
        this.contacts.put(me.UserName, me);
        WeChatTools.LOGGER.finer(String.format("获取到自身信息：%s", WeChatTools.GSON.toJson(me)));
    }

    /**
     * 获取好友信息
     *
     * @param UserName 好友UserName
     * @return 好友信息
     */
    User getFriend(String UserName) {
        return this.friends.get(UserName);
    }

    /**
     * 获取好友列表
     *
     * @return 好友列表
     */
    ArrayList<User> getFriends() {
        ArrayList<User> friendList = new ArrayList<>();
        for (String key : friends.keySet()) {
            friendList.add(friends.get(key));
        }
        return friendList;
    }

    /**
     * 获取公众号信息
     *
     * @param UserName 公众号UserName
     * @return 公众号信息
     */
    User getPublic(String UserName) {
        return this.publics.get(UserName);
    }

    /**
     * 获取公众号列表
     *
     * @return 公众号列表
     */
    ArrayList<User> getPublics() {
        ArrayList<User> publicList = new ArrayList<>();
        for (String key : publics.keySet()) {
            publicList.add(publics.get(key));
        }
        return publicList;
    }

    /**
     * 获取聊天室信息
     *
     * @param UserName 聊天室UserName
     * @return 聊天室信息
     */
    User getChatroom(String UserName) {
        return this.chatrooms.get(UserName);
    }

    /**
     * 获取聊天室列表
     *
     * @return 聊天室列表
     */
    ArrayList<User> getChatrooms() {
        ArrayList<User> chatroomList = new ArrayList<>();
        for (String key : chatrooms.keySet()) {
            chatroomList.add(chatrooms.get(key));
        }
        return chatroomList;
    }

    /**
     * 添加联系人，自动归类
     *
     * @param contact 联系人信息
     */
    void addContact(User contact) {
        contacts.put(contact.UserName, contact);
        if (contact.UserName.startsWith("@@")) {
            chatrooms.put(contact.UserName, contact);
            StringBuilder members = new StringBuilder("\n");
            for (User member : contact.MemberList) {
                members.append(String.format("%s（%s）\n", member.NickName, member.UserName));
                contacts.put(member.UserName, member);
            }
            WeChatTools.LOGGER.finer(String.format("获取到微信群：%s（%s），群成员：%s", contact.NickName, contact.UserName, members));
        } else if (contact.VerifyFlag > 0) {
            publics.put(contact.UserName, contact);
            WeChatTools.LOGGER.finer(String.format("获取到微信公众号：%s（%s）", contact.NickName, contact.UserName));
        } else if (contact.ContactFlag > 0) {
            friends.put(contact.UserName, contact);
            WeChatTools.LOGGER.finer(String.format("获取到微信好友：%s（%s）", contact.NickName, contact.UserName));
        }
    }

    /**
     * 获取联系人信息，自动分类
     *
     * @param UserName 联系人UserName
     * @return 联系人信息
     */
    User getContact(String UserName) {
        if (UserName.startsWith("@@")) {
            if (this.chatrooms.containsKey(UserName)) {
                return this.chatrooms.get(UserName);
            }
        } else {
            if (this.friends.containsKey(UserName)) {
                return this.friends.get(UserName);
            } else if (this.publics.containsKey(UserName)) {
                return this.publics.get(UserName);
            }
        }
        return this.contacts.get(UserName);
    }

    /**
     * 移除联系人
     *
     * @param UserName 联系人UserName
     */
    void rmvContact(String UserName) {
        this.friends.remove(UserName);
        this.publics.remove(UserName);
        this.chatrooms.remove(UserName);
        this.contacts.remove(UserName);
    }
}
