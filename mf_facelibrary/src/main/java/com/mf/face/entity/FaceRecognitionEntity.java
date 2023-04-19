package com.mf.face.entity;


public class FaceRecognitionEntity extends FaceTipsEntity {

    /**
     * 人脸id
     */
    private String faceId;

    public FaceRecognitionEntity() {
    }


    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }


    @Override
    public String toString() {
        return "FaceRecognitionEntity{" +
                "isShowPreviewView=" + isShowPreviewView +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", detail='" + detail + '\'' +
                ", faceId='" + faceId + '\'' +
                ", useType=" + useType +
                ", tag='" + tag + '\'' +
                '}';
    }
}
