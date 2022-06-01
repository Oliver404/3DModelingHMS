package com.oliverbotello.a3dmodelinghms.ui;

import android.content.ClipData;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Model3DAdapter extends RecyclerView.Adapter<Model3DVH> {
    private List<ItemEnt> lstItems;
    private Model3DVH.OnActionClickListener listener;

    public Model3DAdapter(List<ItemEnt> lstItems, Model3DVH.OnActionClickListener listener) {
        super();

        this.lstItems = lstItems;
        this.listener = listener;
    }

    public void addItem(ItemEnt newItem) {
        lstItems.add(newItem);
        notifyItemInserted(lstItems.size() - 1);
    }

    public void removeItem(String taskID) {
        int position = getItemPositionByTaskID(taskID);

        lstItems.remove(position);

        if (position >= 0)
            this.notifyItemRemoved(position);
    }

    public int getItemPositionByTaskID(String taskID) {
        for (int i = 0; i < lstItems.size(); i++) {
            ItemEnt item = lstItems.get(i);

            if (item.getTaskID().equals(taskID))
                return i;
        }

        return -1;
    }

    public ItemEnt getItemByTaskID(String taskID) {
        for (ItemEnt item : lstItems) {
            if (item.getTaskID().equals(taskID))
                return item;
        }

        return null;
    }

    public void notifyItemChanged(ItemEnt item) {
        int position = getItemPositionByTaskID(item.getTaskID());

        if (position >= 0)
            this.notifyItemChanged(position);
    }

    @NonNull
    @Override
    public Model3DVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return Model3DVH.newInstance(parent, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull Model3DVH holder, int position) {
        holder.bind(lstItems.get(position));
    }

    @Override
    public int getItemCount() {
        return lstItems.size();
    }
}
