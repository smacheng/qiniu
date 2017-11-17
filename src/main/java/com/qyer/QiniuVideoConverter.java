package com.qyer;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import com.qiniu.common.Zone;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 20170220b on 2017/11/7.
 */
public class QiniuVideoConverter {


  private final QiniuConfig conf;

  private static final String PERSISTENT_PIPELINE = "liveconvert";

  private Map<String, String> paramMap = Maps.newHashMapWithExpectedSize(10);

  private int waitForSeconds;

  public QiniuVideoConverter(QiniuConfig conf) {
    this.conf = conf;
    this.waitForSeconds = 90;
    paramMap.put("ab", "64k");//声音码率
    paramMap.put("vb", "1m");//视频码率，原始码率不足1024就是原来码率，大于1024就等于1024
    paramMap.put("ar", "44100");//音频采样率
    paramMap.put("vcodec", "libx264");//视频的解码格式
    paramMap.put("acodec", "aac");//音频的解码格式
    paramMap.put("s", "700x300");//分辨率
    paramMap.put("autoscale", "1");//保持宽高比
    paramMap.put("r", "24");//帧率
    paramMap.put("t", "30");//视频时长
    paramMap.put("h264Profile", "baseline");//lib264的profile级别
  }


  private String toFop() {
    StringBuilder sb = new StringBuilder();
    sb.append("avthumb/mp4");
    if (MapUtils.isNotEmpty(this.paramMap)) {
      for (String key : this.paramMap.keySet()) {
        sb.append('/').append(key).append('/').append(this.paramMap.get(key));
      }
    }
    return sb.toString();
  }

  public String doConvert(String inputKey, String outputKey) throws Exception,
      InterruptedException {
    Auth auth = Auth.create(conf.getAk(), conf.getSk());
    Configuration cfg = new Configuration(Zone.autoZone());
    OperationManager operationManager = new OperationManager(auth, cfg);
    String save = String.format("%s:%s", conf.getBucket(), outputKey);
    String fops = toFop() + "|saveas/%s";
    String avthumbMp4Fop = String.format(fops, UrlSafeBase64.encodeToString(save));
    String persistentOpfs = StringUtils.join(new String[]{
        avthumbMp4Fop
    }, ";");
    System.out.println("Encode video - ops - {}" + persistentOpfs);

    String persistentId = operationManager
        .pfop(conf.getBucket(), inputKey, persistentOpfs, PERSISTENT_PIPELINE, "", true);
    for (int i = 0; i < waitForSeconds; i++) {
      OperationStatus status = operationManager.prefop(persistentId);
      System.out.println(new Gson().toJson(status));
      int code = status.code;
      if (code == 0) {
        System.out.println("Encode video - Key={}" + status.inputKey);
        System.out.println("Encode video - Encoded={}" + status.items[0].key);
        break;
      } else if (code == 1) {
        System.out.println("The fop is waiting for execution");
      } else if (code == 2) {
        System.out.println("The fop is executing now");
      } else {
        throw new Exception("");
      }
      TimeUnit.SECONDS.sleep(1);
    }
    return outputKey;
  }

}

