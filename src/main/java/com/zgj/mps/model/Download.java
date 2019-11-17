package com.zgj.mps.model;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zgj.mps.tool.SnowFlakeUtil;

import javax.persistence.*;
import java.sql.Timestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "download")
public class Download {

  private String id = String.valueOf(SnowFlakeUtil.getFlowIdInstance().nextId());
  private long status;
  private long size;
  private Timestamp createTime;
  private Timestamp finishTime;
  private Long userId;
  private String resourceId;
  @TableLogic
  private Short isDelete;
  private String deviceId;


  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id", unique = true, nullable = false, length = 255)
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  @Column(name = "status")
  public long getStatus() {
    return status;
  }

  public void setStatus(long status) {
    this.status = status;
  }


  @Column(name = "size")
  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }


  @Column(name = "create_time", length = 19)
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  public Timestamp getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Timestamp createTime) {
    this.createTime = createTime;
  }


  @Column(name = "finishTime", length = 19)
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  public Timestamp getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(Timestamp finishTime) {
    this.finishTime = finishTime;
  }


  @Column(name = "user_id")
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }


  @Column(name = "resource_id",nullable = false)
  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  @Column(name = "is_delete")
  public Short getIsDelete() {
    return isDelete;
  }

  public void setIsDelete(Short isDelete) {
    this.isDelete = isDelete;
  }

  @Column(name = "device_id")
  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
}
