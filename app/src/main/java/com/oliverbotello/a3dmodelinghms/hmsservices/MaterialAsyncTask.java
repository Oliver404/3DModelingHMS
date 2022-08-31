package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.os.AsyncTask;

import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureInitResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureSetting;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

public class MaterialAsyncTask extends AsyncTask<Object, Object, Object> {
    private ReconstructService.OnGetTaskIDListener taskListener;
    private Modeling3dTextureUploadListener materialUploadListener;
    private String path;

    public MaterialAsyncTask(
            String path,
            ReconstructService.OnGetTaskIDListener taskListener,
            Modeling3dTextureUploadListener  materialUploadListener
    ) {
        this.path = path;
        this.taskListener = taskListener;
        this.materialUploadListener = materialUploadListener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Modeling3dTextureEngine engine =  (Modeling3dTextureEngine) params[0];
        Modeling3dTextureSetting settings = (Modeling3dTextureSetting) params[1];
        Modeling3dTextureInitResult modeling3dTextureInitResult = engine.initTask(settings);
        String taskID = modeling3dTextureInitResult.getTaskId();


        if (taskID != null) {
            ItemEnt item = new ItemEnt(
                    taskID,
                    path,
                    ItemEnt.STATUS_WAITING_TASK_ID,
                    ItemEnt.TYPE_TEXTURE
            );

            taskListener.onTaskCreate(item);
            engine.setTextureUploadListener(materialUploadListener);
            engine.asyncUploadFile(taskID, path);
        }
        else taskListener.onFailCreateTask();

        return null;
    }
}
