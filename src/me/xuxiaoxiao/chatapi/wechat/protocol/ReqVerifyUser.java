package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.ArrayList;

public class ReqVerifyUser {
    public BaseRequest BaseRequest;
    public int Opcode;
    public int VerifyUserListSize;
    public ArrayList<VerifyUser> VerifyUserList;
    public String VerifyContent;
    public int SceneListCount;
    public ArrayList<Integer> SceneList;
    public String skey;

    public ReqVerifyUser(BaseRequest baseRequest, int opCode, String userName, String verifyTicket, String verifyContent) {
        this.BaseRequest = baseRequest;
        this.Opcode = opCode;
        this.VerifyUserListSize = 1;
        this.VerifyUserList = new ArrayList<>();
        this.VerifyUserList.add(new VerifyUser(userName, verifyTicket));
        this.VerifyContent = verifyContent;
        this.SceneListCount = 1;
        this.SceneList = new ArrayList<>();
        this.SceneList.add(33);
        this.skey = baseRequest.Skey;
    }

    public static class VerifyUser {
        public String Value;
        public String VerifyUserTicket = "";

        public VerifyUser(String value, String ticket) {
            this.Value = value;
            this.VerifyUserTicket = ticket;
        }
    }
}
