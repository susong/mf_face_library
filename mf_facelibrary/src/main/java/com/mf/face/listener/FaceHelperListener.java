package com.mf.face.listener;

import com.mf.face.entity.FaceRecognitionEntity;
import com.mf.face.entity.FaceRegisterEntity;
import com.mf.face.entity.FaceTipsEntity;

public interface FaceHelperListener {

    default void onError(int code, String msg) {

    }

    default void onFaceRegisterTipsResult(FaceTipsEntity entity) {

    }

    default void onFaceRecognitionTipsResult(FaceTipsEntity entity) {

    }

    default void onFaceRegisterResult(FaceRegisterEntity entity) {

    }

    default void onFaceRecognitionResult(FaceRecognitionEntity entity) {

    }

    default void onCancelFaceRegisterAndRecognitionResult(int code, String msg) {

    }

    default void onSyncFaceDataResult(int code, String msg) {

    }

    default void onRemoveFaceDataResult(int code, String msg) {

    }

    default void onManualCancelFaceRegister() {

    }

    default void onManualCancelFaceRecognition() {

    }

    default void onPickCarByPlate() {

    }

    default void onPickCarByBerth() {

    }
}
