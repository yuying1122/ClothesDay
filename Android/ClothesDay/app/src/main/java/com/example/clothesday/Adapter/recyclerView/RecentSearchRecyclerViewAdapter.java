package com.example.clothesday.Adapter.recyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothesday.DAO.SearchDTO;
import com.example.clothesday.R;

import java.util.ArrayList;

public class RecentSearchRecyclerViewAdapter  extends RecyclerView.Adapter<RecentSearchRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<SearchDTO> mData = new ArrayList<SearchDTO>();

    public RecentSearchRecyclerViewAdapter(ArrayList<SearchDTO> searchDTO)  {
        this.mData = searchDTO;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_recent_search, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        SearchDTO searchDTO = mData.get(position);

        holder.content.setText(searchDTO.getSE_CON()); // 검색 기록 나열

    }

    @Override
    public int getItemCount() { return mData.size(); }

    public class MyViewHolder extends RecyclerView.ViewHolder  {

        TextView content;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.recyclerview_recent_search_text);

        }
    }
}
