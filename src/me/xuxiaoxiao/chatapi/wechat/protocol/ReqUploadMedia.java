package me.xuxiaoxiao.chatapi.wechat.protocol;

public class ReqUploadMedia {
    public BaseRequest BaseRequest;
    public int UploadType;
    public long ClientMediaId;
    public long TotalLen;
    public long StartPos;
    public long DataLen;
    public int MediaType;
    public String FromUserName;
    public String ToUserName;
    public String FileMd5;

    public ReqUploadMedia(BaseRequest baseRequest, int uploadType, long totalLen, long startPos, long dataLen, String fileMd5, int mediaType, String fromUserName, String toUserName) {
        this.BaseRequest = baseRequest;
        this.UploadType = uploadType;
        this.ClientMediaId = System.currentTimeMillis();
        this.TotalLen = totalLen;
        this.StartPos = startPos;
        this.DataLen = dataLen;
        this.FileMd5 = fileMd5;
        this.MediaType = mediaType;
        this.FromUserName = fromUserName;
        this.ToUserName = toUserName;
    }
}
