package com.mf.face.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mf.face.entity.FaceRecognitionEntity
import com.mf.face.entity.FaceRegisterEntity
import com.mf.face.entity.FaceTipsEntity
import com.mf.face.helper.FaceHelper
import com.mf.face.listener.FaceHelperListener
import java.util.UUID
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    final val TAG = MainActivity::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_startFaceService).setOnClickListener {
            FaceHelper.getInstance().startFaceService(applicationContext)
        }
        findViewById<Button>(R.id.btn_stopFaceService).setOnClickListener {
            FaceHelper.getInstance().stopFaceService(applicationContext)
        }
        findViewById<Button>(R.id.btn_faceRegister).setOnClickListener {
            val provinceCodes = arrayOf(
                "京", "沪", "津", "渝", "冀", "豫", "云", "辽", "黑", "湘",
                "皖", "鲁", "新", "苏", "浙", "赣", "鄂", "桂", "甘", "晋", "蒙", "陕", "吉", "闽",
                "贵", "粤", "青", "藏", "川", "宁", "琼"
            )

            val letterCodes = arrayOf(
                "A", "B", "C", "D", "E", "F", "G", "H", "J", "K",
                "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
            )

            val numberCodes = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")

            val random = Random(System.currentTimeMillis())
            val provinceCode = provinceCodes[random.nextInt(provinceCodes.size)]
            val letterCode1 = letterCodes[random.nextInt(letterCodes.size)]
            val letterCode2 = letterCodes[random.nextInt(letterCodes.size)]
            val letterCode3 = letterCodes[random.nextInt(letterCodes.size)]
            val numberCode1 = numberCodes[random.nextInt(numberCodes.size)]
            val numberCode2 = numberCodes[random.nextInt(numberCodes.size)]
            val numberCode3 = numberCodes[random.nextInt(numberCodes.size)]

            val licensePlate =
                "$provinceCode$letterCode1$letterCode2$letterCode3$numberCode1$numberCode2$numberCode3"

            FaceHelper.getInstance().faceRegister(true, licensePlate)
        }
        findViewById<Button>(R.id.btn_faceRecognition).setOnClickListener {
            FaceHelper.getInstance().faceRecognition(true)
        }
        findViewById<Button>(R.id.btn_cancelFaceRegisterAndRecognition).setOnClickListener {
            FaceHelper.getInstance().cancelFaceRegisterAndRecognition()
        }
        findViewById<Button>(R.id.btn_syncFaceData).setOnClickListener {
            FaceHelper.getInstance().syncFaceData(
                "faceId_" + UUID.randomUUID().toString(),
                "faceFeature_" + UUID.randomUUID().toString()
            )
        }
        findViewById<Button>(R.id.btn_removeFaceData).setOnClickListener {
            FaceHelper.getInstance().removeFaceData()
        }
        FaceHelper.getInstance().setListener(object : FaceHelperListener {
            override fun onError(code: Int, msg: String?) {
                Log.i(TAG, "onError code:$code msg:$msg")
            }

            override fun onFaceRegisterTipsResult(entity: FaceTipsEntity?) {
                Log.i(TAG, "onFaceRegisterTipsResult entity:$entity")
            }

            override fun onFaceRecognitionTipsResult(entity: FaceTipsEntity?) {
                Log.i(TAG, "onFaceRecognitionTipsResult entity:$entity")
            }

            override fun onFaceRegisterResult(entity: FaceRegisterEntity?) {
                Log.i(TAG, "onFaceRegisterResult entity:$entity")
            }

            override fun onFaceRecognitionResult(entity: FaceRecognitionEntity?) {
                Log.i(TAG, "onFaceRecognitionResult entity:$entity")
            }

            override fun onCancelFaceRegisterAndRecognitionResult(code: Int, msg: String?) {
                Log.i(TAG, "onCancelFaceRegisterAndRecognitionResult code:$code msg:$msg")
            }

            override fun onSyncFaceDataResult(code: Int, msg: String?) {
                Log.i(TAG, "onSyncFaceDataResult code:$code msg:$msg")
            }

            override fun onRemoveFaceDataResult(code: Int, msg: String?) {
                Log.i(TAG, "onRemoveFaceDataResult code:$code msg:$msg")
            }

            override fun onManualCancelFaceRegister() {
                Log.i(TAG, "onManualCancelFaceRegister")
            }

            override fun onManualCancelFaceRecognition() {
                Log.i(TAG, "onManualCancelFaceRecognition")
            }

            override fun onPickCarByPlate() {
                Log.i(TAG, "onPickCarByPlate")
            }

            override fun onPickCarByBerth() {
                Log.i(TAG, "onPickCarByBerth")
            }
        })
    }
}