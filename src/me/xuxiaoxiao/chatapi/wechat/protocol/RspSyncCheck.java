package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RspSyncCheck {
    public int retcode;
    public int selector;

    public RspSyncCheck(String str) {
        Matcher matcher = Pattern.compile("window.synccheck=\\{retcode:\"(\\d*)\",selector:\"(\\d*)\"\\}").matcher(str);
        if (matcher.find()) {
            retcode = Integer.valueOf(matcher.group(1));
            selector = Integer.valueOf(matcher.group(2));
        }
    }
}
