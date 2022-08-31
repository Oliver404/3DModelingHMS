package com.oliverbotello.a3dmodelinghms;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.huawei.agconnect.AGConnectInstance;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.AREnginesApk;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.ARWorldTrackingConfig;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadResult;
import com.huawei.hms.mlkit.face.FaceCreator;
import com.huawei.hms.motioncapturesdk.Modeling3dFrame;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructDownloadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructDownloadResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructPreviewConfig;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructPreviewListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;
import com.huawei.hms.scene.common.base.error.exception.UpdateNeededException;
import com.huawei.hms.scene.sdk.ARView;
import com.huawei.hms.scene.sdk.FaceView;
import com.huawei.hms.scene.sdk.SceneView;
import com.huawei.hms.scene.sdk.common.LandmarkType;
import com.huawei.hms.scene.sdk.render.SceneKit;
import com.oliverbotello.a3dmodelinghms.hmsservices.PoseService;
import com.oliverbotello.a3dmodelinghms.hmsservices.ReconstructService;
import com.oliverbotello.a3dmodelinghms.hmsservices.TextureService;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;
import com.oliverbotello.a3dmodelinghms.services.FilePathService;
import com.oliverbotello.a3dmodelinghms.services.SharedPreferencesService;
import com.oliverbotello.a3dmodelinghms.ui.Model3DAdapter;
import com.oliverbotello.a3dmodelinghms.ui.Model3DVH;
import com.oliverbotello.a3dmodelinghms.utils.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        Modeling3dReconstructUploadListener,
        Model3DVH.OnActionClickListener,
        ReconstructService.OnGetTaskIDListener,
        DialogInterface.OnClickListener,
        Modeling3dReconstructDownloadListener,
        Modeling3dTextureUploadListener {
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
    private SceneView sceneView;

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
//        sceneView = findViewById(R.id.arvw_scene);

        recyclerView.setAdapter(adapter);
        imgvwSeletedImage.setOnClickListener(this);
        findViewById(R.id.btn_create_texture).setOnClickListener(this);
        findViewById(R.id.btn_create_model3d).setOnClickListener(this);
        findViewById(R.id.btn_get_pose).setOnClickListener(this);
        findViewById(R.id.btn_close_camera).setOnClickListener(this);
    }

    private boolean verifyPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        return ActivityCompat.checkSelfPermission(getApplicationContext(), permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission_read = Manifest.permission.READ_EXTERNAL_STORAGE;
            String permission_write = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            String permission_manage = Manifest.permission.MANAGE_EXTERNAL_STORAGE;

            requestPermissions(
                    new String[]{permission_read, permission_write, permission_manage},
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
            if (imagePath != null) {
                showLoadingDialog(true);
                textureService.setTaskIDListener(this);
                textureService.setUploadListener(this);
                textureService.initTask(imagePath, this, this);
            }
            else showToast("Choose a image");
        }
        else if (v.getId() == R.id.btn_create_model3d) {
//            sharedService.putNewModel(
//                    new ItemEnt("900340539235762560", imagePath, ItemEnt.STATUS_MODEL_SUCCESS, ItemEnt.TYPE_MODEL)
//            );
            if (imagePath != null) {
                showLoadingDialog(true);
                reconstructService.setTaskIDListener(this);
                reconstructService.setUploadProcessListener(this);
                reconstructService.initTask(imagePath, this, this);
//                try {
//                    ImageUtils.changeSizeImagesInDirectory(imagePath);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.e("AG:", "Error", e);
//                }
            }
            else showToast("Choose a image");
        }
        else if (v.getId() == R.id.btn_get_pose) {
            initCamera();
        }
        else if (v.getId() == R.id.btn_close_camera) {
            closeCamera();
        }
    }

    private void initCamera() {
        findViewById(R.id.cnstrntlyt_preview).setVisibility(View.VISIBLE);
        initializeSceneKit();
//        ListenableFuture<ProcessCameraProvider> providerFuture =
//                ProcessCameraProvider.getInstance(getApplicationContext());
//
//        providerFuture.addListener(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            ProcessCameraProvider cameraProvider = providerFuture.get();
//                            Preview preview = new Preview.Builder().build();
//
//                            preview.setSurfaceProvider(
//                                    ((PreviewView) MainActivity.this
//                                            .findViewById(R.id.camera_preview)).getSurfaceProvider()
//                            );
//                            ImageAnalysis imga = new ImageAnalysis.Builder()
//                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                                    .build();
//                            imga.setAnalyzer(
//                                    Executors.newSingleThreadExecutor()
//                                    ,
////                                    ContextCompat.getMainExecutor(MainActivity.this),
//                            new ImageAnalysis.Analyzer() {
//                                        @Override
//                                        public void analyze(@NonNull ImageProxy image) {
//                                            Modeling3dFrame.Property property =
//                                                    new Modeling3dFrame.Property.Creator()
//                                                            .setFormatType(ImageFormat.NV21)
//                                                            .setWidth(image.getWidth())
//                                                            .setHeight(image.getHeight())
//                                                            .setQuadrant(3)
//                                                            .setItemIdentity((int) image.getImageInfo().getTimestamp())
//                                                            .create();
////                                            Log.e("AG", "Planes: " + image.getPlanes().length);
////                                            Modeling3dFrame frame = Modeling3dFrame.fromBitmap(imageProxyToBitmap(image));
//                                            Modeling3dFrame frame1 = Modeling3dFrame.fromByteArray(image2byteArray(image), property);
////
////                                            image.close();
//                                            new PoseService(getApplicationContext()).analizePose(frame1, image,
//                                                    new PoseService.OnSkeletroSuccess() {
//                                                        @Override
//                                                        public void onSuccess(Bitmap bitmap) {
//                                                            runOnUiThread(
//                                                                    new Runnable() {
//                                                                        @Override
//                                                                        public void run() {
//                                                                            ((ImageView) MainActivity.this.findViewById(R.id.imgvw_preview_camera))
//                                                                                    .setImageBitmap(bitmap);
//                                                                        }
//                                                                    }
//                                                            );
//
//                                                        }
//                                                    }
//                                            );
//                                        }
//
//                                private byte[] imageProxyToByteArray(ImageProxy imageProxy) {
////                                    Image image = imageProxy.getImage();
//                                    ByteBuffer yBuffer = imageProxy.getPlanes()[0].getBuffer();
//                                    ByteBuffer xBuffer = imageProxy.getPlanes()[1].getBuffer(); // VU
//                                    ByteBuffer vuBuffer = imageProxy.getPlanes()[2].getBuffer(); // VU
//
//                                    int ySize = yBuffer.remaining();
//                                    int xSize = xBuffer.remaining();
//                                    int vuSize = vuBuffer.remaining();
//
//                                    byte[] nv21 = new byte[ySize + xSize + vuSize];//
//
//
//                                    yBuffer.get(nv21, 0, ySize);
//                                    xBuffer.get(nv21, ySize + vuSize, xSize);
//                                    vuBuffer.get(nv21, ySize , vuSize);
//
//                                    YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
//                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                                    yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 25, out);
//                                    byte[] imageBytes = out.toByteArray();
//                                    return nv21;
//                                }
//
//                                private byte[] image2byteArray(ImageProxy image) {
////                                    Image image = imageProxy.getImage();
////                                    if (image.getFormat() != ImageFormat.YUV_420_888) {
////                                        throw new IllegalArgumentException("Invalid image format");
////                                    }
//
//                                    int width = image.getWidth();
//                                    int height = image.getHeight();
//
////                                    ImageProxy.Plane yPlane = image.getPlanes()[0];
////                                    ImageProxy.Plane uPlane = image.getPlanes()[1];
////                                    ImageProxy.Plane vPlane = image.getPlanes()[2];
//                                    ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
//                                    ImageProxy.PlaneProxy uPlane = image.getPlanes()[1];
//                                    ImageProxy.PlaneProxy vPlane = image.getPlanes()[2];
//
//                                    ByteBuffer yBuffer = yPlane.getBuffer();
//                                    ByteBuffer uBuffer = uPlane.getBuffer();
//                                    ByteBuffer vBuffer = vPlane.getBuffer();
//
//                                    // Full size Y channel and quarter size U+V channels.
//                                    int numPixels = (int) (width * height * 1.5f);
//                                    byte[] nv21 = new byte[numPixels];
//                                    int index = 0;
//
//                                    // Copy Y channel.
//                                    int yRowStride = yPlane.getRowStride();
//                                    int yPixelStride = yPlane.getPixelStride();
//                                    for(int y = 0; y < height; ++y) {
//                                        for (int x = 0; x < width; ++x) {
//                                            nv21[index++] = yBuffer.get(y * yRowStride + x * yPixelStride);
//                                        }
//                                    }
//
//                                    // Copy VU data; NV21 format is expected to have YYYYVU packaging.
//                                    // The U/V planes are guaranteed to have the same row stride and pixel stride.
//                                    int uvRowStride = uPlane.getRowStride();
//                                    int uvPixelStride = uPlane.getPixelStride();
//                                    int uvWidth = width / 2;
//                                    int uvHeight = height / 2;
//
//                                    for(int y = 0; y < uvHeight; ++y) {
//                                        for (int x = 0; x < uvWidth; ++x) {
//                                            int bufferIndex = (y * uvRowStride) + (x * uvPixelStride);
//                                            // V channel.
//                                            nv21[index++] = vBuffer.get(bufferIndex);
//                                            // U channel.
//                                            nv21[index++] = uBuffer.get(bufferIndex);
//                                        }
//                                    }
//                                    return nv21;
//                                }
//                            });
//
//                            cameraProvider.unbindAll();
//                            cameraProvider.bindToLifecycle(
//                                    MainActivity.this,
//                                    CameraSelector.DEFAULT_FRONT_CAMERA,
//                                    imga,
//                                    preview
//                            );
//                            initializeSceneKit();
//                        } catch (ExecutionException e) {
//                            e.printStackTrace();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                ContextCompat.getMainExecutor(this)
//        );
    }

    private void closeCamera() {
        findViewById(R.id.cnstrntlyt_preview).setVisibility(View.GONE);
    }

    @Override
    public void onUploadProgress(String taskId, double progress, Object ext) {
        Log.e("AG", "Progress: " + progress);
    }

    @Override
    public void onResult(String taskID, Modeling3dTextureUploadResult result, Object o) {
        Log.e("AG", "Result: " + result.isComplete());
        if (result.isComplete()) {
            // Update status of model
            ItemEnt item = adapter.getItemByTaskID(taskID);

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

            int status = textureService.queryTask(item.getTaskID());
            Log.e("AG:", "Estatus: " + status);
        }
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
    public void onDownloadProgress(String taskID, double progress, Object o) {
        Log.e("AG", "Download " + taskID + ":" + progress);
    }

    @Override
    public void onResult(String taskID, Modeling3dReconstructDownloadResult result, Object o) {
        Log.e("AG", "Download " + taskID + ": result" + result.isComplete());
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
    public void downloadModel(String taskID) {
        reconstructService.setDownloadListener(this);
        reconstructService.downloadModel(taskID, imagePath + "/model/");
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
        if (which == DialogInterface.BUTTON_POSITIVE) {
            sharedService.popModel(adapter.getItemByTaskID(selectedItem));
            adapter.removeItem(selectedItem);
        }
        else
            dialog.dismiss();

        selectedItem = null;
    }

    private void initializeSceneKit() {
        final float[] position = { 0.0f, 0.0f, 0.0f };
        final float[] rotation = { 1.0f, 0.0f, 0.0f, 0.0f };
        final float[] scale = { 1.0f, 1.0f, 1.0f };
//        sceneView.loadScene("out.gltf");
//        AGConnectInstance.initialize(this);'

//        ARSession mArSession = new ARSession(this);
//// Select a specific Config to initialize the ARSession based on the application scenario.
//        ARWorldTrackingConfig config = new ARWorldTrackingConfig(mArSession);
////        config.setCameraLensFacing(ARConfigBase.CameraLensFacing.FRONT);
//        config.setPowerMode(ARConfigBase.PowerMode.ULTRA_POWER_SAVING);
//        mArSession.configure(config);
//        mArSession.resume();
//        mArSession.configure(config);
//        FaceView faceView = findViewById(R.id.face_view);
//        faceView.loadAsset("out.gltf", LandmarkType.TIP_OF_NOSE);
//        faceView.setInitialPose(0, position, scale, rotation);
    }
}