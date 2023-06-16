package com.example.clothesday.Adapter.recyclerView;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.clothesday.Adapter.viewPager2.ImageSliderAdapter;
import com.example.clothesday.DAO.PostDTO;
import com.example.clothesday.R;
import com.example.clothesday.common.time.DayOfWeek;
import com.example.clothesday.common.viewPager2.Indicator;
import com.example.clothesday.fragment.PostFragment;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class TwoLineRecyclerViewAdapter extends RecyclerView.Adapter<TwoLineRecyclerViewAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<PostDTO> mData = new ArrayList<PostDTO>();
    private Activity activity;
    private FragmentManager fm;
    private int stack= 0;

    public TwoLineRecyclerViewAdapter(Context context, ArrayList<PostDTO> PostDTO, Activity activity, FragmentManager fm, int gu)  {
        this.context = context;
        this.mData = PostDTO;
        this.activity = activity;
        this.fm = fm;
        stack = gu;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_two_line, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Indicator indicator = new Indicator();
        ArrayList<Uri> uriList = new ArrayList<>();
        PostDTO post = mData.get(position);

        StringTokenizer stk;
        int image_count = 0;
        //뷰페이저
        if (post.getPO_PIC() != null) {
            stk = new StringTokenizer(post.getPO_PIC(), ",");
            image_count = stk.countTokens();

            // 이미지 url을 얻어서 리스트에 넣기
            while(stk.hasMoreTokens()){
                uriList.add(Uri.parse("http://dlsxjsptb.cafe24.com/post/image/" + stk.nextToken()));
            }
        }
        ImageSliderAdapter imageslideradapter = new ImageSliderAdapter(context,uriList, activity);
        holder.viewPager2.setOffscreenPageLimit(1); // 뷰페이저에서 이전 혹은 다음 페이지를 몇개까지 미리 로딩할지 정함
        holder.viewPager2.setAdapter(imageslideradapter);
        holder.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                indicator.setCurrentIndicator(position, holder.layoutIndicator, context);
            }
        });
        indicator.setupIndicators(image_count, holder.layoutIndicator, context);

        //날짜
        try {
            DayOfWeek dayOfWeek = new DayOfWeek();
            String reg = post.getPO_REG_DA() + " " + dayOfWeek.getDayOfWeek(post.getPO_REG_DA());
            holder.date.setText(reg + "요일");
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.date.setTag(position);
    }

    @Override
    public int getItemCount() { return mData.size(); }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView date, tag;
        ViewPager2 viewPager2;
        LinearLayout layoutIndicator;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager2 = itemView.findViewById(R.id.recyclerview_two_line_viewpager2);
            date = itemView.findViewById(R.id.recyclerview_two_line_date);
            tag = itemView.findViewById(R.id.recyclerview_two_line_tag);
            layoutIndicator = itemView.findViewById(R.id.recyclerview_two_line_indicator);

            date.setOnClickListener(this::onClick);
            viewPager2.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View v) {
            PostFragment postFragment = new PostFragment();
            int pos = getBindingAdapterPosition();
            PostDTO PostDTO = mData.get(pos);
            Bundle result = new Bundle();
            switch (v.getId()) {
                case R.id.recyclerview_two_line_date:
                    result.putInt("PO_ID", PostDTO.getPO_ID());
                    result.putInt("stack", stack);
                    fm.setFragmentResult("requestKey", result);
                    postFragment.setArguments(result);
                    fm.beginTransaction().detach(postFragment).attach(postFragment).replace(R.id.container, postFragment).addToBackStack(null).commit();
                    break;
                case R.id.recyclerview_two_line_viewpager2:
                    result.putInt("PO_ID", PostDTO.getPO_ID());
                    result.putInt("stack", stack);
                    fm.setFragmentResult("requestKey", result);
                    postFragment.setArguments(result);
                    fm.beginTransaction().detach(postFragment).attach(postFragment).replace(R.id.container, postFragment).addToBackStack(null).commit();
                    break;

            }
        }

    } // 뷰홀더 끝

}
