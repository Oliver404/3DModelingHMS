package com.oliverbotello.a3dmodelinghms.utils;

import android.app.Application;
import android.os.Environment;

import com.oliverbotello.a3dmodelinghms.BuildConfig;

import java.io.File;

public class Constants {
    public static final int TEXTURE_IMG_MIN_SIZE = 1024;
    public static final int TEXTURE_IMG_MAX_SIZE = 8192;
    public static final int MODEL_IMG_MIN_H = 1280;
    public static final int MODEL_IMG_MIN_W = 720;
    public static final int MODEL_IMG_MAX_H = 4096;
    public static final int MODEL_IMG_MAX_W = 3072;
    public static final String APP_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "3DModeling";
    public static final String MODELS_PATH = APP_PATH + File.separator + "Models";
    public static final String MATERIALS_PATH = APP_PATH + File.separator + "Materials";
}
