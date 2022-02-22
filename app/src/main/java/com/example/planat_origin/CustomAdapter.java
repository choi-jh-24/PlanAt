package com.example.planat_origin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private ArrayList<User> arrayList;
    private Context context;

    public CustomAdapter(ArrayList<User> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_commontime, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
       return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Glide.with(holder.itemView)
        .load(arrayList.get(position).getProfile()).into(holder.iv_userprofile);
        holder.tv_email.setText(arrayList.get(position).getEmail());
        holder.tv_strtime.setText(String.valueOf(arrayList.get(position).getStrtime()));
        holder.tv_endtime.setText(String.valueOf(arrayList.get(position).getEndtime()));
    }


        @Override
    public int getItemCount() {
        return (arrayList !=null? arrayList.size(): 0);  //배열 크기
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_userprofile;
        TextView tv_email;
        TextView tv_strtime;
        TextView tv_endtime;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv_userprofile = itemView.findViewById(R.id.iv_userprofile);
            this.tv_email = itemView.findViewById(R.id.tv_email);
            this.tv_strtime = itemView.findViewById(R.id.tv_strtime);
            this.tv_endtime = itemView.findViewById(R.id.tv_endtime);
        }
    }
}
