package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.ArrayList;

public class ReqBatchGetContact {
    public final BaseRequest BaseRequest;
    public final int Count;
    public final ArrayList<Contact> List;

    public ReqBatchGetContact(BaseRequest baseRequest, ArrayList<Contact> contacts) {
        this.BaseRequest = baseRequest;
        this.Count = contacts.size();
        this.List = contacts;
    }

    public static class Contact {
        public final String UserName;
        public final String ChatRoomId;

        public Contact(String userName, String chatRoomId) {
            this.UserName = userName;
            this.ChatRoomId = chatRoomId;
        }
    }
}
