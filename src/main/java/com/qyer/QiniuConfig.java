package com.qyer;

/**
 * Created by 20170220b on 2017/11/7.
 */
public class QiniuConfig {


  private String ak;

  private String sk;

  private String bucket;


  public QiniuConfig(String ak, String sk, String bucket) {
    this.ak = ak;
    this.sk = sk;
    this.bucket = bucket;
  }


  public String getAk() {
    return ak;
  }

  public void setAk(String ak) {
    this.ak = ak;
  }

  public String getSk() {
    return sk;
  }

  public void setSk(String sk) {
    this.sk = sk;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

}
