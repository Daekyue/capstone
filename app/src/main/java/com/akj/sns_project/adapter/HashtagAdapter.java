package com.akj.sns_project.adapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akj.sns_project.Fragment04;
import com.akj.sns_project.Movie;
import com.akj.sns_project.MovieInfo;
import com.akj.sns_project.Poster;
import com.akj.sns_project.R;
import com.akj.sns_project.activity.WritePostActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class HashtagAdapter extends RecyclerView.Adapter<HashtagAdapter.ViewHolder> {

    private ArrayList<String> mFriendList = new ArrayList<>();

    @NonNull
    @Override
    public HashtagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hashtag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HashtagAdapter.ViewHolder holder, int position) {
        holder.onBind(mFriendList.get(position));
    }

    public void setFriendList(ArrayList<String> list){
        this.mFriendList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView HashText;
        Button AddHashBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            HashText = (TextView) itemView.findViewById(R.id.HashText);


        }

        void onBind(String item){
            HashText.setText(item);
        }
    }
}
