package com.huangchao.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<MainContent.MainItem> mValues;

    public MyItemRecyclerViewAdapter(List<MainContent.MainItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_info, parent, false);
        view.setOnClickListener(v -> {
            TextView contentView = v.findViewById(R.id.content);
            if (contentView.getText().toString().startsWith("Preferred SIM")) {
                Intent newIntent = new Intent();
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.sim.SimDialogActivity");
                newIntent.setComponent(cn);
                newIntent.putExtra("dialog_type", 0);
                view.getContext().startActivity(newIntent);
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(position + 1));
        holder.mContentView.setText(mValues.get(position).content);
        holder.mDetailView.setText(mValues.get(position).details);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDetailView;
        public MainContent.MainItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            mDetailView = view.findViewById(R.id.detail);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}