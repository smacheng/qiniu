package com.qyer;

import com.google.common.collect.Lists;

import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.List;

/**
 * Hello world!
 */
public class App {

  public static final String HOST = "https://a.b.com/";

  public static void main(String[] args) {
    String fileName = "device_token." + System.currentTimeMillis();
    String path = "/Users/20170220b/etc/video" + File.separatorChar + fileName;
    System.out.println(path);

    //省略本地上传步骤,接收requst里的文件，前端使用plupload组件
    //DiskFileItemFactory factory = new DiskFileItemFactory();
    //ServletFileUpload upload = new ServletFileUpload(factory);
    //List<FileItem> items;
    //try {
    //  items = upload.parseRequest(req);
    //} catch (FileUploadException e) {
    //  e.printStackTrace();
    //  return;
    //}
    //File dest = new File(path);
    //for (FileItem fi : items) {
    //  FileUtils.copyInputStreamToFile(fi.getInputStream(), dest);
    //}

    List<File> files = loadFile(path);
    QiniuConfig config = new QiniuConfig("sdkdsjkghjshgjhdsljglh",
        "jgksdfkljklsjhkljgfkljhkjfgdk;hj", "a-b-com");
    Auth auth = Auth.create(config.getAk(), config.getSk());
    String uptoken = auth.uploadToken(config.getBucket());
    Configuration configuration = new Configuration(Zone.autoZone());
    UploadManager uploadManager = new UploadManager(configuration);

    String finalPath = "";
    for (File file : files) {
      String remotePath = "ad/video/" + file.getName();
      try {
        uploadManager.put(file, remotePath, uptoken);
      } catch (Exception e) {
        e.printStackTrace();
      }

      QiniuVideoConverter converter = new QiniuVideoConverter(config);
      String afterPath = "ad/video/"+ RandomStringUtils.randomAlphanumeric(16);
      try {
        converter.doConvert(remotePath, afterPath);
      }catch (Exception e){
        e.printStackTrace();
      }

      finalPath = HOST+afterPath;
      System.out.println(finalPath);
    }

  }


  public static List<File> loadFile(String path) {
    File file = new File(path);
    if (!file.exists()) {
      return null;
    }
    List<File> files = null;
    if (file.isFile()) {
      files = Lists.newArrayList(file);
    } else {
      File[] fileArr = file.listFiles();
      files = Lists.newArrayList(fileArr);
    }
    return files;
  }
}
