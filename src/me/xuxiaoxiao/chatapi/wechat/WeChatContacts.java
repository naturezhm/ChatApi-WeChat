package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.entity.User;
import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspBatchGetContact;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

final class WeChatContacts {
    private final WeChatApi wxApi;
    private final ConcurrentHashMap<String, User> friends = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> publics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> chatrooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> contacts = new ConcurrentHashMap<>();
    private User me;

    WeChatContacts(WeChatApi wxApi) {
        this.wxApi = wxApi;
    }

    User getMe() {
        return me.clone();
    }

    void setMe(User me) {
        User clone = me.clone();
        this.me = clone;
        this.contacts.put(me.UserName, clone);
        WeChatTools.LOGGER.info(String.format("获取到自身信息：%s", WeChatTools.GSON.toJson(clone)));
    }

    User getFriend(String UserName) {
        User uFriend = this.friends.get(UserName);
        return uFriend != null ? uFriend.clone() : null;
    }

    ArrayList<User> getFriends() {
        ArrayList<User> friendList = new ArrayList<>();
        for (String key : friends.keySet()) {
            friendList.add(friends.get(key).clone());
        }
        return friendList;
    }

    User getPublic(String UserName) {
        User uPublic = this.publics.get(UserName);
        return uPublic != null ? uPublic.clone() : null;
    }

    ArrayList<User> getPublics() {
        ArrayList<User> publicList = new ArrayList<>();
        for (String key : publics.keySet()) {
            publicList.add(publics.get(key).clone());
        }
        return publicList;
    }

    User getChatroom(String UserName) {
        User uChatroom = this.chatrooms.get(UserName);
        return uChatroom != null ? uChatroom.clone() : null;
    }

    ArrayList<User> getCharrooms() {
        ArrayList<User> chatroomList = new ArrayList<>();
        for (String key : chatrooms.keySet()) {
            chatroomList.add(chatrooms.get(key).clone());
        }
        return chatroomList;
    }

    void addContact(User contact) {
        User clone = contact.clone();
        contacts.put(clone.UserName, clone);
        if (clone.UserName.startsWith("@@")) {
            chatrooms.put(clone.UserName, clone);
            for (User member : clone.MemberList) {
                contacts.put(member.UserName, member);
            }
            WeChatTools.LOGGER.info(String.format("获取到微信群：%s", clone.NickName));
        } else if (clone.VerifyFlag > 0) {
            publics.put(clone.UserName, clone);
            WeChatTools.LOGGER.info(String.format("获取到微信公众号：%s", clone.NickName));
        } else if (clone.ContactFlag > 0) {
            friends.put(clone.UserName, clone);
            WeChatTools.LOGGER.info(String.format("获取到微信好友：%s", clone.NickName));
        }
    }

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

    void rmvContact(String UserName) {
        this.friends.remove(UserName);
        this.publics.remove(UserName);
        this.chatrooms.remove(UserName);
        this.contacts.remove(UserName);
    }

    void loadContacts(ArrayList<Contact> contacts) {
        RspBatchGetContact rspBatchGetContact = wxApi.webwxbatchgetcontact(contacts);
        for (User user : rspBatchGetContact.ContactList) {
            addContact(user);
        }
    }
}
