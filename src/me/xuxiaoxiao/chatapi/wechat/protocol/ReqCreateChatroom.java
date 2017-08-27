package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.ArrayList;
import java.util.List;

public class ReqCreateChatroom {
    public BaseRequest BaseRequest;
    public String Topic;
    public int MemberCount;
    public ArrayList<Member> MemberList;

    public ReqCreateChatroom(BaseRequest baseRequest, String topic, List<String> members) {
        this.BaseRequest = baseRequest;
        this.Topic = topic;
        this.MemberCount = members.size();
        this.MemberList = new ArrayList<>();
        for (String member : members) {
            this.MemberList.add(new Member(member));
        }
    }

    public static class Member {
        public String UserName;

        public Member(String userName) {
            this.UserName = userName;
        }
    }
}
