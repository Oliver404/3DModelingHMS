package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;

import androidx.camera.core.ImageProxy;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.motioncapturesdk.Modeling3dFrame;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngine;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngineFactory;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngineSetting;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureJoint;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;

import java.util.List;

public class PoseService {
    private static Modeling3dMotionCaptureEngineSetting SETTINGS = null;
    private static Modeling3dMotionCaptureEngine ENGINE = null;

    // Constructor
    public PoseService(Context context) {
        if (ENGINE == null) {
            SETTINGS = new Modeling3dMotionCaptureEngineSetting.Factory()
                    .setAnalyzeType(
                            Modeling3dMotionCaptureEngineSetting.TYPE_3DSKELETON_QUATERNION
                            | Modeling3dMotionCaptureEngineSetting.TYPE_3DSKELETON
                    ).create();
            ENGINE = Modeling3dMotionCaptureEngineFactory.getInstance()
                    .getMotionCaptureEngine(SETTINGS);
        }
    }

    public void analizePose(Modeling3dFrame frame, ImageProxy image, OnSkeletroSuccess listener) {
        Task<List<Modeling3dMotionCaptureSkeleton>> task = ENGINE.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<List<Modeling3dMotionCaptureSkeleton>>() {
            @Override
            public void onSuccess(List<Modeling3dMotionCaptureSkeleton> results) {
                // Detection success.
                Log.e("AG", "Results: " + results.size());

                if (results.size() > 0) {
                    Bitmap bitmap = drawMotion(image.getWidth(), image.getHeight(), results.get(0));

                    if (listener != null)
                        listener.onSuccess(bitmap);
                }


                image.close();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // Detection failure.
                Log.e("AG", "Error: " + e.getMessage(), e);
                image.close();
            }
        });
    }

    private Bitmap drawMotion(int width, int height, Modeling3dMotionCaptureSkeleton modeling3dMotionCaptureSkeleton) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);
        float lastX = 0f;
        float lastY = 0f;
        List<Modeling3dMotionCaptureJoint> lstPoints = modeling3dMotionCaptureSkeleton.getJoints();

        // Manos
        paint.setColor(Color.BLUE);
        canvas.drawCircle(
                width / 2 - 40,
                height / 3 + 150,
                10,
                paint
        );
        paint.setColor(Color.BLUE);
        canvas.drawCircle(
                width / 2 + 40,
                height / 3 + 150,
                10,
                paint
        );
        // Codos
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(
                width / 2 - 40 - (150 * modeling3dMotionCaptureSkeleton.getQuaternion(19).getPointX()),
                height / 3 + 75 - (150 * getAbsValue(modeling3dMotionCaptureSkeleton.getQuaternion(19).getPointY())),
                10,
                paint
        );
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(
                width / 2 + 40  + (150 * modeling3dMotionCaptureSkeleton.getQuaternion(18).getPointX()),
                height / 3 + 150 - (150 * getAbsValue(modeling3dMotionCaptureSkeleton.getQuaternion(18).getPointY())),
                10,
                paint
        );
        paint.setColor(Color.GREEN);
        canvas.drawLine(
                width / 2,
                height / 3,
                width / 2 - 40 - (150 * modeling3dMotionCaptureSkeleton.getQuaternion(19).getPointX()),
                height / 3 + 150 - (150 * getAbsValue(modeling3dMotionCaptureSkeleton.getQuaternion(19).getPointY())),
                paint
        );
        paint.setColor(Color.WHITE);
        canvas.drawLine(
                width / 2,
                height / 3,
                width / 2 - 40 - (150 * modeling3dMotionCaptureSkeleton.getQuaternion(18).getPointX()),
                height / 3 + 150 - (150 * getAbsValue(modeling3dMotionCaptureSkeleton.getQuaternion(18).getPointY())),
                paint
        );
        // Cuello bajo
        paint.setColor(Color.RED);
        canvas.drawCircle(
                width / 2,
                height / 3,
                10,
                paint
        );
        paint.setColor(Color.WHITE);
        canvas.drawLine(
                width / 2,
                height / 3,
                width / 2,
                height / 3 * 2,
                paint
        );
        // Cadera
        paint.setColor(Color.RED);
        canvas.drawCircle(
                width / 2,
                height / 3 * 2,
                10,
                paint
        );
        // Rodillas
        paint.setColor(Color.RED);
        canvas.drawCircle(
                width / 2 - 40,
                height / 3 * 2 + 75,
                10,
                paint
        );
        paint.setColor(Color.RED);
        canvas.drawCircle(
                width / 2 + 40,
                height / 3 * 2 + 75,
                10,
                paint
        );
        // Pies
        paint.setColor(Color.RED);
        canvas.drawCircle(
                width / 2 - 40,
                height / 3 * 2 + 150,
                10,
                paint
        );
        paint.setColor(Color.RED);
        canvas.drawCircle(
                width / 2 + 40,
                height / 3 * 2 + 150,
                10,
                paint
        );

        return bitmap;
    }

    private float getAbsValue(float value) {
        return value * (value >= 0 ?  1 : -1);
    }

    public interface OnSkeletroSuccess {
        void onSuccess(Bitmap bitmap);
    }
}
