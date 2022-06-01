package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.materialgeneratesdk.Modeling3dTextureConstants;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureInitResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureSetting;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextureService {
    private static Modeling3dTextureEngine ENGINE = null;
    private static ManagerUploadListener MANAGER_UPLOAD_LISTENER;

    private String mTaskID;

    public TextureService(Context context) {
        if (ENGINE == null) {
            ENGINE = Modeling3dTextureEngine.getInstance(context);
            MANAGER_UPLOAD_LISTENER = new ManagerUploadListener();

            ENGINE.setTextureUploadListener(MANAGER_UPLOAD_LISTENER);
        }

        Modeling3dTextureInitResult modeling3dTextureInitResult = ENGINE.initTask(
                new Modeling3dTextureSetting.Factory()
                        .setTextureMode(Modeling3dTextureConstants.AlgorithmMode.AI)
                        .create()
        );
        mTaskID = modeling3dTextureInitResult.getTaskId();
    }

    public String getTaskID() {
        return mTaskID;
    }

    public void addUploadListener(Modeling3dTextureUploadListener uploadListener) {
        MANAGER_UPLOAD_LISTENER.addUploadListener(mTaskID, uploadListener);
    }

    public String create(String picturePath) {
        ENGINE.asyncUploadFile(mTaskID, picturePath);

        return mTaskID;
    }

    private class ManagerUploadListener implements Modeling3dTextureUploadListener {
        private Map<String, Modeling3dTextureUploadListener> uploadListeners;

        public ManagerUploadListener() {
            uploadListeners = new HashMap<>();
        }

        public void addUploadListener(String taskID, Modeling3dTextureUploadListener uploadListener) {
            uploadListeners.put(taskID, uploadListener);
        }

        /**
         * Modeling3dTextureUploadListener
         * */
        @Override
        public void onUploadProgress(String taskID, double progress, Object ext) {
            showMessageLog(taskID + "Progress: " + progress);

            if (uploadListeners.containsKey(taskID))
                uploadListeners.get(taskID).onUploadProgress(taskID, progress, ext);
        }

        @Override
        public void onResult(String taskID, Modeling3dTextureUploadResult result, Object ext) {
            showMessageLog(taskID + "Result: " + result.isComplete());

            if (uploadListeners.containsKey(taskID))
                uploadListeners.get(taskID).onResult(taskID, result, ext);
        }

        @Override
        public void onError(String taskID, int errorCode, String message) {
            showMessageLog(taskID + "Error Code: " + errorCode);

            if (uploadListeners.containsKey(taskID))
                uploadListeners.get(taskID).onError(taskID, errorCode, message);
        }

        private void showMessageLog(String message) {
            Log.e("AG", message);
        }
    }
}
