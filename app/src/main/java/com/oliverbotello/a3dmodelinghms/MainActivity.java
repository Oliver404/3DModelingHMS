package com.oliverbotello.a3dmodelinghms;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructPreviewConfig;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructPreviewListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;
import com.oliverbotello.a3dmodelinghms.hmsservices.ReconstructService;
import com.oliverbotello.a3dmodelinghms.hmsservices.TextureService;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;
import com.oliverbotello.a3dmodelinghms.services.FilePathService;
import com.oliverbotello.a3dmodelinghms.services.SharedPreferencesService;
import com.oliverbotello.a3dmodelinghms.ui.Model3DAdapter;
import com.oliverbotello.a3dmodelinghms.ui.Model3DVH;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        Modeling3dReconstructUploadListener,
        Model3DVH.OnActionClickListener,
        ReconstructService.OnGetTaskIDListener,
        DialogInterface.OnClickListener {
    private static final int RQ_CODE_PERMISSION = 100;
    private static final int SELECT_FILE_CODE = 200;

    private TextureService textureService;
    private ReconstructService reconstructService;
    private FilePathService filePathService;
    private SharedPreferencesService sharedService;
    private String imagePath;
    private String selectedItem;
    // View components
    private AppCompatImageView imgvwSeletedImage;
    private RecyclerView recyclerView;
    private Model3DAdapter adapter;
    private View vwLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textureService = new TextureService(getApplicationContext());
        reconstructService = new ReconstructService(getApplicationContext());
        filePathService = new FilePathService();
        sharedService = new SharedPreferencesService(getApplicationContext());
        imagePath = null;

        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intentData) {
        super.onActivityResult(requestCode, resultCode, intentData);

        if (requestCode == SELECT_FILE_CODE) {
            if (intentData != null) showDataImage(intentData.getData());
            else {
                clearDataImage();
                showToast("Choose one");
            }
        }
    }

    private void initView() {
        imgvwSeletedImage = findViewById(R.id.imgvw_preview_selected_image);
        vwLoading = findViewById(R.id.lyt_loading);
        recyclerView = findViewById(R.id.rcclrvw_models);
        adapter = new Model3DAdapter(sharedService.getListModels(), this);

        recyclerView.setAdapter(adapter);
        imgvwSeletedImage.setOnClickListener(this);
        findViewById(R.id.btn_create_texture).setOnClickListener(this);
        findViewById(R.id.btn_create_model3d).setOnClickListener(this);
    }

    private boolean verifyPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        return ActivityCompat.checkSelfPermission(getApplicationContext(), permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission_read = Manifest.permission.READ_EXTERNAL_STORAGE;
            String permission_manage = Manifest.permission.MANAGE_EXTERNAL_STORAGE;

            requestPermissions(
                    new String[]{permission_read, permission_manage},
                    RQ_CODE_PERMISSION
            );
        }
    }

    private void requestForImage() {
        Intent i = new Intent();
        String filetype = "image/*";
        String title = "Choose Image";

        i.setType(filetype);
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, title), SELECT_FILE_CODE);
    }

    private void showDataImage(Uri selectedImage) {
        imagePath = filePathService.getPath(getApplicationContext(), selectedImage);
        imagePath = new File(imagePath).getParent();
        Log.e("AG", imagePath);
        imgvwSeletedImage.setImageURI(selectedImage);
    }

    private void clearDataImage() {
        imagePath = null;

        imgvwSeletedImage.setImageURI(null);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showLoadingDialog(boolean bussy) {
        vwLoading.setVisibility( bussy ? View.VISIBLE : View.GONE);
    }

    private void previewModelgin(String taskID) {
        Modeling3dReconstructPreviewListener previewListener = new Modeling3dReconstructPreviewListener() {
            @Override
            public void onResult(String taskId, Object ext) {
                Log.e("AG", "Result: ");
            }
            @Override
            public void onError(String taskId, int errorCode, String message) {
                Log.e("AG", "ErrorPreview: " + errorCode + " Message: " + message);
            }
        };

        Modeling3dReconstructPreviewConfig config = new Modeling3dReconstructPreviewConfig.Factory().setTextureMode(Modeling3dReconstructConstants.TextureMode.PBR).create();
        Modeling3dReconstructEngine modeling3dReconstructEngine = Modeling3dReconstructEngine.getInstance(MainActivity.this);
        modeling3dReconstructEngine.previewModelWithConfig(taskID, MainActivity.this, config, previewListener);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgvw_preview_selected_image) {
            if (verifyPermission()) requestForImage();
            else requestForPermission();
        }
        else if (v.getId() == R.id.btn_create_texture) {
            if (imagePath != null) textureService.create(imagePath);
            else showToast("Choose a image");
        }
        else if (v.getId() == R.id.btn_create_model3d) {
            if (imagePath != null) {
//                sharedService.putNewModel(new ItemEnt("900340539235762560", imagePath, ItemEnt.STATUS_MODEL_SUCCESS, ItemEnt.TYPE_MODEL));
                showLoadingDialog(true);
                reconstructService.setTaskIDListener(this);
                reconstructService.setUploadProcessListener(this);
                reconstructService.initTask(imagePath, this, this);
            }
            else showToast("Choose a image");
        }
    }

    @Override
    public void onUploadProgress(String taskId, double progress, Object ext) {
        Log.e("AG", "Progress: " + progress);
    }
    @Override
    public void onResult(String taskId, Modeling3dReconstructUploadResult result, Object ext) {
        Log.e("AG", "Result: " + result.isComplete());
        if (result.isComplete()) {
            // Update status of model
            ItemEnt item = adapter.getItemByTaskID(taskId);

            item.setStatus(ItemEnt.STATUS_UPLOAD_SUCCESS);
            sharedService.putNewModel(item);

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemChanged(item);
                        }
                    }
            );


            int status = reconstructService.queryTask(item.getTaskID());
            while (status == 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("AG", "Cannot sleep");
                }

                status = reconstructService.queryTask(item.getTaskID());
            }

            item.setStatus(status);
            sharedService.putNewModel(item);

            runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(item);
                    }
                }
            );
        }
    }
    @Override
    public void onError(String taskId, int errorCode, String message) {
        Log.e("AG", "Error: " + errorCode + ". Message: " + message);
        ItemEnt item = adapter.getItemByTaskID(taskId);

        item.setStatus(ItemEnt.STATUS_UPLOAD_FAIL);
        sharedService.putNewModel(item);

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(item);
                    }
                }
        );
    }

    @Override
    public void previewModel(String taskID) {
        Log.e("AG", "Task ID: " + taskID);
        Modeling3dReconstructPreviewListener previewListener = new Modeling3dReconstructPreviewListener() {
            @Override
            public void onResult(String taskId, Object ext) {
                Log.e("AG", "Result: ");
            }
            @Override
            public void onError(String taskId, int errorCode, String message) {
                Log.e("AG", "ErrorPreview: " + errorCode + " Message: " + message);
            }
        };
        Modeling3dReconstructPreviewConfig config = new Modeling3dReconstructPreviewConfig.Factory().setTextureMode(Modeling3dReconstructConstants.TextureMode.PBR).create();
        Modeling3dReconstructEngine modeling3dReconstructEngine = Modeling3dReconstructEngine.getInstance(MainActivity.this);
        modeling3dReconstructEngine.previewModelWithConfig(taskID, MainActivity.this, config, previewListener);
    }

    @Override
    public void deleteModel(String taskID) {
        selectedItem = taskID;

        new AlertDialog.Builder(this)
                .setNegativeButton("No", this)
                .setPositiveButton("Yes", this)
                .setMessage("Do you want to delete the Model?")
                .show();
    }

    @Override
    public void checkStatus(String taskID) {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... objects) {
                int status = reconstructService.queryTask(taskID);
                ItemEnt item = adapter.getItemByTaskID(taskID);

                item.setStatus(status);
                sharedService.putNewModel(item);

                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyItemChanged(item);
                                            }
                                        }
                                );
                            }
                        }
                );
                return null;
            }
        }.execute();
    }

    @Override
    public void onTaskCreate(ItemEnt item) {
        Log.e("AG", item.getTaskID());
        Log.e("AG", item.getPath());
        Log.e("AG", item.getStatus() + "");
        item.setStatus(ItemEnt.STATUS_UPLOADING);
        MainActivity.this.sharedService.putNewModel(item);
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.addItem(item);
                        showLoadingDialog(false);
                    }
                }
        );
    }

    @Override
    public void onFailCreateTask() {
        Log.e("AG", "No se pudo crear el objeto");
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    showLoadingDialog(false);
                }
            }
        );
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE)
            adapter.removeItem(selectedItem);
        else
            dialog.dismiss();

        selectedItem = null;
    }
}