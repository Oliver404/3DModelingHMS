package com.oliverbotello.a3dmodelinghms.application;

import android.app.Application;
import android.util.Log;

import com.huawei.hms.materialgeneratesdk.MaterialGenApplication;
import com.huawei.hms.objreconstructsdk.ReconstructApplication;
import com.oliverbotello.a3dmodelinghms.R;

public class App3DModelingHMS extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("AG", "onCreateApplication");
        MaterialGenApplication.getInstance().setApiKey("DAEDAB16uUB087vOev3i6IhuGl4G6mhFHrXEyHOuGQKtCGWZu+bX3RQe94kCkw4y9IbblcgC8eGR2KO/SN/gflT2g5ZZ3EmyQ2twzA==");
        ReconstructApplication.getInstance().setApiKey("DAEDAB16uUB087vOev3i6IhuGl4G6mhFHrXEyHOuGQKtCGWZu+bX3RQe94kCkw4y9IbblcgC8eGR2KO/SN/gflT2g5ZZ3EmyQ2twzA==");
    }
}
