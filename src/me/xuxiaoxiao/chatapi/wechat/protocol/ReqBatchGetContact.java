package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.ArrayList;

public class ReqBatchGetContact {
    public final BaseRequest BaseRequest;
    public final int Count;
    public final ArrayList<Contact> List;

    public ReqBatchGetContact(BaseRequest BaseRequest, ArrayList<Contact> List) {
        this.BaseRequest = BaseRequest;
        this.Count = List.size();
        this.List = List;
    }

    public static class Contact {
        public final String UserName;
        public final String ChatRoomId;

        public Contact(String UserName, String ChatRoomId) {
            this.UserName = UserName;
            this.ChatRoomId = ChatRoomId;
        }
    }
}
