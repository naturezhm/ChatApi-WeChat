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

/**
 * 模拟网页微信客户端工具类
 */
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

    /**
     * 判断获取到的消息是否是群消息
     *
     * @param addMsg 获取到的消息
     * @return 是否是群消息
     */
    public static boolean isGroupMsg(AddMsg addMsg) {
        return addMsg.FromUserName.startsWith("@@");
    }

    /**
     * 获取到的消息的实际发送者
     *
     * @param addMsg 获取到的消息
     * @return 消息的实际发送者的UserName
     */
    public static String msgSender(AddMsg addMsg) {
        if (isGroupMsg(addMsg)) {
            return addMsg.Content.substring(0, addMsg.Content.indexOf(':'));
        } else {
            return addMsg.FromUserName;
        }
    }

    /**
     * 获取到的消息的实际内容
     *
     * @param addMsg 获取到的消息
     * @return 消息的实际内容
     */
    public static String msgContent(AddMsg addMsg) {
        if (isGroupMsg(addMsg)) {
            return addMsg.Content.substring(addMsg.Content.indexOf(':') + ":<br/>".length());
        } else {
            return addMsg.Content;
        }
    }

    /**
     * 接口请求
     *
     * @param url   接口地址
     * @param body  Post请求体
     * @param regex 返回值需要满足的正则表达式
     * @return 接口返回的字符串
     */
    static String request(XUrl url, XBody body, String regex) {
        for (int i = 0; i < 3; i++) {
            String respStr = XHttpTools.request(new XHttpTools.XOption("utf-8", 90 * 1000, 90 * 1000), url, body).string();
            if (!XTools.strEmpty(respStr) && Pattern.compile(regex).matcher(respStr).find()) {
                return respStr;
            }
        }
        return null;
    }
}
