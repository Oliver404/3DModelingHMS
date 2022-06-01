package com.oliverbotello.a3dmodelinghms.ui;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.oliverbotello.a3dmodelinghms.R;
import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;

public class Model3DVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ItemEnt item;
    private AppCompatImageView imgvwImage;
    private AppCompatTextView txtvwName;
    private AppCompatTextView txtvwType;
    private AppCompatTextView txtvwStatus;
    private AppCompatImageButton btnAction;
    private AppCompatImageButton btnDelete;
    private OnActionClickListener listener;

    public static Model3DVH newInstance(@NotNull ViewGroup parent, OnActionClickListener listener) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_model, parent, false);

        return new Model3DVH(view, listener);
    }

    public Model3DVH(@NonNull View itemView, OnActionClickListener listener) {
        super(itemView);
        initView();

        this.listener = listener;
    }

    private void initView() {
        imgvwImage = itemView.findViewById(R.id.imgvw_model_picture);
        txtvwName = itemView.findViewById(R.id.txtvw_name);
        txtvwType = itemView.findViewById(R.id.txtvw_type);
        txtvwStatus = itemView.findViewById(R.id.txtvw_status);
        btnAction = itemView.findViewById(R.id.btn_item_action);
        btnDelete = itemView.findViewById(R.id.btn_item_delete);

        btnAction.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    public void bind(ItemEnt item) {
        this.item = item;
        File dirFile = new File(item.getPath());
        int currentStatus = item.getStatus();

        txtvwName.setText(dirFile.getName().toUpperCase());
        txtvwType.setText(item.getType() == ItemEnt.TYPE_TEXTURE ? "Texture" : "3D Model");

        imgvwImage.setImageURI(Uri.fromFile(dirFile.listFiles()[0]));

        if (currentStatus == ItemEnt.STATUS_WAITING_TASK_ID) {
            txtvwStatus.setText("Waiting ID");
            txtvwStatus.setTextColor(Color.DKGRAY);
            showButton(true, R.drawable.ic_refresh);
        }
        else if (currentStatus == ItemEnt.STATUS_UPLOAD_FAIL) {
            txtvwStatus.setText("Upload fail");
            txtvwStatus.setTextColor(Color.RED);
            showButton(false, R.drawable.ic_refresh);
        }
        else if (currentStatus == ItemEnt.STATUS_UPLOADING) {
            txtvwStatus.setText("Uploading...");
            txtvwStatus.setTextColor(Color.YELLOW);
            showButton(true, R.drawable.ic_refresh);
        }
        else if (currentStatus == ItemEnt.STATUS_UPLOAD_SUCCESS) {
            txtvwStatus.setText("Uploaded");
            txtvwStatus.setTextColor(Color.BLUE);
            showButton(true, R.drawable.ic_refresh);
        }
        else if (currentStatus == ItemEnt.STATUS_MODELING) {
            txtvwStatus.setText("Modeling...");
            txtvwStatus.setTextColor(Color.YELLOW);
            showButton(true, R.drawable.ic_refresh);
        }
        else if (currentStatus == ItemEnt.STATUS_MODEL_SUCCESS) {
            txtvwStatus.setText("Model created");
            txtvwStatus.setTextColor(Color.GREEN);
            showButton(true, R.drawable.ic_3d);
        }
        else if (currentStatus == ItemEnt.STATUS_MODEL_FAIL) {
            txtvwStatus.setText("Model fail");
            txtvwStatus.setTextColor(Color.RED);
            showButton(false, R.drawable.ic_refresh);
        }
    }

    private void showButton(boolean visible, int iconID) {
        btnAction.setImageResource(iconID);
        btnAction.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_item_action) {
            if (item.getStatus() == ItemEnt.STATUS_MODEL_SUCCESS)
                listener.previewModel(item.getTaskID());
            else
                listener.checkStatus(item.getTaskID());
        }
        else
            listener.deleteModel(item.getTaskID());
    }

    public interface OnActionClickListener {
        void previewModel(String taskID);
        void deleteModel(String taskID);
        void checkStatus(String taskID);
    }
}
