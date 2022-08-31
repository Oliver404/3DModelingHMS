package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.content.Context;
import android.os.AsyncTask;

import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructDownloadConfig;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructDownloadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructQueryResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

public class ReconstructService {
    private static Modeling3dReconstructEngine MODELING_3D = null; // Modeling Reconstructor
    private static Modeling3dReconstructSetting SETTINGS = null; // Modeling Settings
    private static Modeling3dReconstructTaskUtils TASK_UTILS = null;
    private static Modeling3dReconstructDownloadConfig DOWNLOAD_CONFIG = null;
    public Modeling3dReconstructUploadListener listener; // Listener for caching modeling events
    public OnGetTaskIDListener taskIDListener; // Listener to notify when get Task ID
    private Modeling3dReconstructDownloadListener downloadListener; // Listener to notify download process
    // Constructor
    public ReconstructService(Context context) {
        if (MODELING_3D == null) {
            MODELING_3D = Modeling3dReconstructEngine.getInstance(context);
            SETTINGS = new Modeling3dReconstructSetting.Factory()
                    .setReconstructMode(Modeling3dReconstructConstants.ReconstructMode.PICTURE)
                    .setTextureMode(Modeling3dReconstructConstants.TextureMode.PBR)
                    .create();
            TASK_UTILS = Modeling3dReconstructTaskUtils.getInstance(context);
            DOWNLOAD_CONFIG = new Modeling3dReconstructDownloadConfig.Factory()
                    .setModelFormat(Modeling3dReconstructConstants.ModelFormat.FBX)
                    .setTextureMode(Modeling3dReconstructConstants.TextureMode.NORMAL)
                    .create();
        }
    }

    public void setUploadProcessListener(Modeling3dReconstructUploadListener listener) {
        this.listener = listener;

        MODELING_3D.setReconstructUploadListener(this.listener);
    }

    public void setDownloadListener(Modeling3dReconstructDownloadListener downloadListener) {
        this.downloadListener = downloadListener;

        MODELING_3D.setReconstructDownloadListener(this.downloadListener);
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
                .execute(MODELING_3D, SETTINGS);
    }

    public void downloadModel(String taskId, String path) {
        new android.os.AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object[] objects) {
                MODELING_3D.downloadModelWithConfig(taskId, path, DOWNLOAD_CONFIG);
                return null;
            }
        }.execute(new Object[]{});
    }

    public interface OnGetTaskIDListener {
        void onTaskCreate(ItemEnt item);
        void onFailCreateTask();
    }
}
