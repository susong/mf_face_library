package com.mf.face.entity;

import java.io.Serializable;

public class FaceTipsEntity implements Serializable {
    /**
     * 是否显示预览框
     */
    protected boolean isShowPreviewView;
    /**
     * 状态码
     */
    protected int code;
    /**
     * 状态消息
     */
    protected String msg;
    /**
     * 状态消息详情
     */
    protected String detail;
    /**
     * 使用类型，只开启RGB 1
     * 使用类型，只开启NIR 2
     * 使用类型，分别开启RGB和NIR 3
     * 使用类型，同时开启RGB和NIR 4
     */
    protected int useType;
    /**
     * uuid 标识
     */
    protected String tag;
    /**
     * 人脸注册 0
     * 人脸识别 1
     */
    protected int which;

    public FaceTipsEntity() {
    }

    public boolean isShowPreviewView() {
        return isShowPreviewView;
    }

    public void setShowPreviewView(boolean showPreviewView) {
        isShowPreviewView = showPreviewView;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getUseType() {
        return useType;
    }

    public void setUseType(int useType) {
        this.useType = useType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getWhich() {
        return which;
    }

    public void setWhich(int which) {
        this.which = which;
    }

    @Override
    public String toString() {
        return "FaceTipsEntity{" +
                "isShowPreviewView=" + isShowPreviewView +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", detail='" + detail + '\'' +
                ", useType=" + useType +
                ", tag='" + tag + '\'' +
                ", which=" + which +
                '}';
    }
}
