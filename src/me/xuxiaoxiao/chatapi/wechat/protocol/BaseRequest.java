package me.xuxiaoxiao.chatapi.wechat.protocol;

public class BaseRequest {
    public final String Uin;
    public final String Sid;
    public final String Skey;
    public final String DeviceID;

    public BaseRequest(String Uin, String Sid, String Skey) {
        this.Uin = Uin;
        this.Sid = Sid;
        this.Skey = Skey;
        this.DeviceID = deviceId();
    }

    public static String deviceId() {
        StringBuilder sb = new StringBuilder("e");
        for (int i = 0; i < 15; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
