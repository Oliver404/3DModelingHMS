package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.os.AsyncTask;

import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

public class ReconstructAsyncTask extends AsyncTask<Object, Object, Object> {
    private ReconstructService.OnGetTaskIDListener taskListener;
    private Modeling3dReconstructUploadListener modelingUploadListener;
    private String path;

    public ReconstructAsyncTask(
            String path,
            ReconstructService.OnGetTaskIDListener taskListener,
            Modeling3dReconstructUploadListener modelingUploadListener
    ) {
        this.path = path;
        this.taskListener = taskListener;
        this.modelingUploadListener = modelingUploadListener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Modeling3dReconstructEngine engine = (Modeling3dReconstructEngine) params[0];
        Modeling3dReconstructSetting settings = (Modeling3dReconstructSetting) params[1];
        Modeling3dReconstructInitResult modeling3dReconstructInitResult = engine.initTask(settings);
        String taskID = modeling3dReconstructInitResult.getTaskId();

        if (taskID != null) {
            ItemEnt item = new ItemEnt(
                    taskID,
                    path,
                    ItemEnt.STATUS_WAITING_TASK_ID,
                    ItemEnt.TYPE_MODEL
            );

            taskListener.onTaskCreate(item);
            engine.setReconstructUploadListener(modelingUploadListener);
            engine.uploadFile(taskID, path);
        }
        else taskListener.onFailCreateTask();

        return null;
    }
}
