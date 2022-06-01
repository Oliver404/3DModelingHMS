package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructQueryResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

public class ReconstructService {
    private static Modeling3dReconstructEngine MODELING_3D = null; // Modeling Reconstructor
    private static Modeling3dReconstructSetting SETTINGS = null; // Modeling Settings
    private static Modeling3dReconstructTaskUtils TASK_UTILS = null;
    private String filePath; // Path for images directory
    private String taskID; // Task ID for current process
    public Modeling3dReconstructUploadListener listener; // Listener for caching modeling events
    public OnGetTaskIDListener taskIDListener; // Listener to notify when get Task ID

    // Constructor
    public ReconstructService(Context context) {
        if (MODELING_3D == null) {
            MODELING_3D = Modeling3dReconstructEngine.getInstance(context);
            SETTINGS = new Modeling3dReconstructSetting.Factory()
                    .setReconstructMode(Modeling3dReconstructConstants.ReconstructMode.PICTURE)
                    .setTextureMode(Modeling3dReconstructConstants.TextureMode.PBR)
                    .create();
            TASK_UTILS = Modeling3dReconstructTaskUtils.getInstance(context);
        }
    }

    public void setUploadProcessListener(Modeling3dReconstructUploadListener listener) {
        this.listener = listener;
    }

    public void setTaskIDListener(OnGetTaskIDListener taskIDListener) {
        this.taskIDListener = taskIDListener;
    }

    public int queryTask(String taskID) {
        Modeling3dReconstructQueryResult result = TASK_UTILS.queryTask(taskID);

        return result.getStatus();
    }

    public void initTask(
            String path,
            OnGetTaskIDListener taskListener,
            Modeling3dReconstructUploadListener modelingUploadListener
    ) {
        new ReconstructAsyncTask(path, taskListener, modelingUploadListener)
                .execute(new Object[] {MODELING_3D, SETTINGS});
    }

    public interface OnGetTaskIDListener {
        void onTaskCreate(ItemEnt item);
        void onFailCreateTask();
    }
}
