package me.xuxiaoxiao.chatapi.wechat;

import me.xuxiaoxiao.chatapi.wechat.protocol.ReqBatchGetContact.Contact;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspBatchGetContact;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspInit;

import java.util.ArrayList;
import java.util.HashMap;

class WeChatContacts {
    public final WeChatApi wxApi;

    private RspInit.User me;
    private HashMap<String, RspInit.User> friends = new HashMap<>();
    private HashMap<String, RspInit.User> publics = new HashMap<>();
    private HashMap<String, RspInit.User> chatrooms = new HashMap<>();
    private HashMap<String, RspInit.User> contacts = new HashMap<>();

    public WeChatContacts(WeChatApi wxApi) {
        this.wxApi = wxApi;
    }

    public final RspInit.User getMe() {
        return me.clone();
    }

    public final void setMe(RspInit.User me) {
        RspInit.User clone = me.clone();
        this.me = clone;
        this.contacts.put(me.UserName, clone);
        WeChatTools.LOGGER.info(String.format("获取到自身信息：%s", WeChatTools.GSON.toJson(clone)));
    }

    public final RspInit.User getFriend(String UserName) {
        return this.friends.get(UserName).clone();
    }

    public final ArrayList<RspInit.User> getFriends() {
        ArrayList<RspInit.User> friendList = new ArrayList<>();
        for (String key : friends.keySet()) {
            friendList.add(friends.get(key).clone());
        }
        return friendList;
    }

    public final RspInit.User getPublic(String UserName) {
        return publics.get(UserName).clone();
    }

    public final ArrayList<RspInit.User> getPublics() {
        ArrayList<RspInit.User> publicList = new ArrayList<>();
        for (String key : publics.keySet()) {
            publicList.add(publics.get(key).clone());
        }
        return publicList;
    }

    public final RspInit.User getChatroom(String UserName) {
        return this.chatrooms.get(UserName).clone();
    }

    public final ArrayList<RspInit.User> getCharrooms() {
        ArrayList<RspInit.User> chatroomList = new ArrayList<>();
        for (String key : chatrooms.keySet()) {
            chatroomList.add(chatrooms.get(key).clone());
        }
        return chatroomList;
    }

    public final void addContact(RspInit.User contact) {
        RspInit.User clone = contact.clone();
        contacts.put(clone.UserName, clone);
        if (clone.UserName.startsWith("@@")) {
            chatrooms.put(clone.UserName, clone);
            for (RspInit.User member : clone.MemberList) {
                contacts.put(member.UserName, member);
            }
            WeChatTools.LOGGER.info(String.format("获取到微信群：%s", clone.NickName));
        } else if (clone.VerifyFlag > 0) {
            publics.put(clone.UserName, clone);
            WeChatTools.LOGGER.info(String.format("获取到微信公众号：%s", clone.NickName));
        } else {
            friends.put(clone.UserName, clone);
            WeChatTools.LOGGER.info(String.format("获取到微信好友：%s", clone.NickName));
        }
    }

    public final RspInit.User getContact(String UserName) {
        return this.contacts.get(UserName).clone();
    }

    public final void rmvContact(String UserName) {
        this.friends.remove(UserName);
        this.chatrooms.remove(UserName);
        this.contacts.remove(UserName);
    }

    public void loadContacts(ArrayList<Contact> contacts) {
        RspBatchGetContact rspBatchGetContact = wxApi.webwxbatchgetcontact(contacts);
        for (RspInit.User user : rspBatchGetContact.ContactList) {
            addContact(user);
        }
    }
}
