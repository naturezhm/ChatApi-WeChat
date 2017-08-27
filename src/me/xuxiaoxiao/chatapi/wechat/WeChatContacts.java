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
        return me.clone();
    }

    /**
     * 设置自身信息
     *
     * @param me 自身信息
     */
    void setMe(User me) {
        User clone = me.clone();
        this.me = clone;
        this.contacts.put(me.UserName, clone);
        WeChatTools.LOGGER.info(String.format("获取到自身信息：%s", WeChatTools.GSON.toJson(clone)));
    }

    /**
     * 获取好友信息
     *
     * @param UserName 好友UserName
     * @return 好友信息
     */
    User getFriend(String UserName) {
        User uFriend = this.friends.get(UserName);
        return uFriend != null ? uFriend.clone() : null;
    }

    /**
     * 获取好友列表
     *
     * @return 好友列表
     */
    ArrayList<User> getFriends() {
        ArrayList<User> friendList = new ArrayList<>();
        for (String key : friends.keySet()) {
            friendList.add(friends.get(key).clone());
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
        User uPublic = this.publics.get(UserName);
        return uPublic != null ? uPublic.clone() : null;
    }

    /**
     * 获取公众号列表
     *
     * @return 公众号列表
     */
    ArrayList<User> getPublics() {
        ArrayList<User> publicList = new ArrayList<>();
        for (String key : publics.keySet()) {
            publicList.add(publics.get(key).clone());
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
        User uChatroom = this.chatrooms.get(UserName);
        return uChatroom != null ? uChatroom.clone() : null;
    }

    /**
     * 获取聊天室列表
     *
     * @return 聊天室列表
     */
    ArrayList<User> getChatrooms() {
        ArrayList<User> chatroomList = new ArrayList<>();
        for (String key : chatrooms.keySet()) {
            chatroomList.add(chatrooms.get(key).clone());
        }
        return chatroomList;
    }

    /**
     * 添加联系人，自动归类
     *
     * @param contact 联系人信息
     */
    void addContact(User contact) {
        User clone = contact.clone();
        contacts.put(clone.UserName, clone);
        if (clone.UserName.startsWith("@@")) {
            chatrooms.put(clone.UserName, clone);
            StringBuilder members = new StringBuilder("\n");
            for (User member : clone.MemberList) {
                members.append(String.format("%s：%s\n", member.NickName, member.UserName));
                contacts.put(member.UserName, member);
            }
            WeChatTools.LOGGER.info(String.format("获取到微信群：%s，群成员：%s", clone.NickName, members));
        } else if (clone.VerifyFlag > 0) {
            publics.put(clone.UserName, clone);
            WeChatTools.LOGGER.info(String.format("获取到微信公众号：%s", clone.NickName));
        } else if (clone.ContactFlag > 0) {
            friends.put(clone.UserName, clone);
            WeChatTools.LOGGER.info(String.format("获取到微信好友：%s,%s", clone.NickName, clone.UserName));
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
            User uChatroom = getChatroom(UserName);
            if (uChatroom != null) {
                return uChatroom;
            }
        } else {
            User uFriend = getFriend(UserName);
            if (uFriend != null) {
                return uFriend;
            }
            User uPublic = getPublic(UserName);
            if (uPublic != null) {
                return uPublic;
            }
        }
        User uContact = this.contacts.get(UserName);
        return uContact != null ? uContact.clone() : null;
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
