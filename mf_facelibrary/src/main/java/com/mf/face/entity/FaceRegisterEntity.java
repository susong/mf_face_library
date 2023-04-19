package com.mf.face.entity;


public class FaceRegisterEntity extends FaceTipsEntity {

    /**
     * 车牌号
     */
    private String licenseNo;
    /**
     * 人脸id
     */
    private String faceId;
    /**
     * 人脸特征值
     */
    private String faceFeature;
    /**
     * 人脸图片路径
     */
    private String faceImgPath;

    public FaceRegisterEntity() {
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getFaceImgPath() {
        return faceImgPath;
    }

    public void setFaceImgPath(String faceImgPath) {
        this.faceImgPath = faceImgPath;
    }

    @Override
    public String toString() {
        return "FaceRegisterEntity{" +
                "isShowPreviewView=" + isShowPreviewView +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", detail='" + detail + '\'' +
                ", licenseNo='" + licenseNo + '\'' +
                ", faceId='" + faceId + '\'' +
                ", faceFeature='" + faceFeature + '\'' +
                ", faceImgPath='" + faceImgPath + '\'' +
                ", useType=" + useType +
                ", tag='" + tag + '\'' +
                '}';
    }
}
