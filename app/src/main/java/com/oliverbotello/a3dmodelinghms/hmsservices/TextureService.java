package com.oliverbotello.a3dmodelinghms.hmsservices;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.materialgeneratesdk.Modeling3dTextureConstants;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureInitResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureQueryResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureSetting;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureTaskUtils;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructQueryResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextureService {
    private static Modeling3dTextureEngine ENGINE = null;
    private static Modeling3dTextureSetting SETTINGS = null;
    private static Modeling3dTextureTaskUtils TASK_UTILS = null;
    public Modeling3dTextureUploadListener listener;
    public ReconstructService.OnGetTaskIDListener taskIDListener;

    public TextureService(Context context) {
        if (ENGINE == null) {
            ENGINE = Modeling3dTextureEngine.getInstance(context);
            SETTINGS = new Modeling3dTextureSetting.Factory()
                    .setTextureMode(Modeling3dTextureConstants.AlgorithmMode.AI)
                    .create();
            TASK_UTILS = Modeling3dTextureTaskUtils.getInstance(context);
        }
    }

    public void setUploadListener(Modeling3dTextureUploadListener uploadListener) {
        this.listener = uploadListener;
    }

    public void setTaskIDListener(ReconstructService.OnGetTaskIDListener taskIDListener) {
        this.taskIDListener = taskIDListener;
    }

    public void initTask(
            String path,
            ReconstructService.OnGetTaskIDListener taskListener,
            Modeling3dReconstructUploadListener modelingUploadListener
    ) {
        new ReconstructAsyncTask(path, taskListener, modelingUploadListener)
                .execute(ENGINE, SETTINGS);
    }

    public int queryTask(String taskID) {
        Modeling3dTextureQueryResult result = TASK_UTILS.queryTask(taskID);

        return result.getStatus();
    }
}
