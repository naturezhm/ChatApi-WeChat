package me.xuxiaoxiao.chatapi.wechat.protocol;

public class ReqUpdateChatroom {
    public BaseRequest BaseRequest;
    public String ChatRoomName;
    public String AddMemberList;
    public String DelMemberList;

    public ReqUpdateChatroom(BaseRequest baseRequest, String chatroomName, String fun, String members) {
        this.BaseRequest = baseRequest;
        this.ChatRoomName = chatroomName;
        switch (fun) {
            case "addmember":
                this.AddMemberList = members;
                break;
            case "delmember":
                this.DelMemberList = members;
                break;
        }
    }
}
