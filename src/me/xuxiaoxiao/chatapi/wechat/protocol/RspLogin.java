package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RspLogin {
    public int code;
    public String userAvatar;
    public String redirectUri;

    public RspLogin(String str) {
        Matcher matcher = Pattern.compile("window.code=(\\d{3});(window.userAvatar = '(.+)'|\\swindow.redirect_uri=\"(.+)\")?").matcher(str);
        if (matcher.find()) {
            code = Integer.valueOf(matcher.group(1));
            userAvatar = matcher.group(3);
            redirectUri = matcher.group(4);
        }
    }
}
