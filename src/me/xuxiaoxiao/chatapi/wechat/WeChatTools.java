package me.xuxiaoxiao.chatapi.wechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xuxiaoxiao.chatapi.wechat.entity.AddMsg;
import me.xuxiaoxiao.xtools.XHttpTools;
import me.xuxiaoxiao.xtools.XHttpTools.XBody;
import me.xuxiaoxiao.xtools.XHttpTools.XUrl;
import me.xuxiaoxiao.xtools.XTools;

import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WeChatTools {
    public static final int TYPE_TEXT = 1;//文字
    public static final int TYPE_IMAGE = 3;//图片
    public static final int TYPE_VOICE = 34;//语音
    public static final int TYPE_CARD = 42;//名片
    public static final int TYPE_VIDEO = 43;//视频
    public static final int TYPE_FACE = 47;//收藏的表情
    public static final int TYPE_OTHER = 49;//转账、文件、链接、笔记等

    static final int TYPE_NOTIFY = 51;//消息已读
    static final int TYPE_SYSTEM = 10000;//系统消息

    static final Logger LOGGER = Logger.getLogger("me.xuxiaoxiao.chatapi.wechat");
    static final Gson GSON = new GsonBuilder().create();

    private WeChatTools() {
    }

    public static boolean isGroupMsg(AddMsg addMsg) {
        return addMsg.FromUserName.startsWith("@@");
    }

    public static String textMsgSender(AddMsg addMsg) {
        if (addMsg.AppMsgType == TYPE_TEXT && isGroupMsg(addMsg)) {
            return addMsg.Content.substring(0, addMsg.Content.indexOf(':'));
        } else {
            return addMsg.FromUserName;
        }
    }

    public static String textMsgContent(AddMsg addMsg) {
        if (addMsg.AppMsgType == TYPE_TEXT && isGroupMsg(addMsg)) {
            return addMsg.Content.substring(addMsg.Content.indexOf("<br/>") + "<br/>".length());
        } else {
            return addMsg.Content;
        }
    }

    static String request(XUrl url, XBody body, String regex) {
        for (int i = 0; i < 5; i++) {
            String respStr = XHttpTools.request(new XHttpTools.XOption("utf-8", 60 * 1000, 60 * 1000), url, body).string();
            if (!XTools.strEmpty(respStr) && Pattern.compile(regex).matcher(respStr).find()) {
                return respStr;
            }
        }
        return null;
    }
}
