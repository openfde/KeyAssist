package com.fde.keyassist.adapter;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fde.keyassist.FloatingService;
import com.fde.keyassist.R;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.util.Constant;

import org.litepal.LitePal;

import java.util.List;

public class PlaySpinnerAdapter extends RecyclerView.Adapter<PlaySpinnerAdapter.ViewHolder> {
    private TextView textView;
    List<Plan> plans;
    private Boolean isClick = true;
    public PlaySpinnerAdapter(List<Plan> plans ,TextView textView) {
        this.textView = textView;
        this.plans = plans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.key_mapping_spinner_item,null,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint({"RecyclerView","ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.key_mapping_plan_text.setText(plans.get(position).getPlanName());
        holder.key_mapping_plan_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // 查找
                List<Plan> planList = LitePal.where("planName = ?", plans.get(position).getPlanName()).find(Plan.class);
                if(planList != null && planList.size() >=1){
                    planList.get(0).setPlanName(holder.key_mapping_plan_text.getText().toString());
                    planList.get(0).save();
                }
                isClick = true;
                holder.key_mapping_plan_text.clearFocus();

                return true;
            }
        });

        holder.key_mapping_plan_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v,  MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && isClick) {
                    // 处理点击事件
                    // 返回 true 表示事件被消费，避免继续传递到焦点处理逻辑
                    textView.setText(holder.key_mapping_plan_text.getText());
                    Constant.planName = holder.key_mapping_plan_text.getText().toString();
                    FloatingService.closePopupWindow();

                    return true;
                }
                return false;
            }
        });



        holder.key_mapping_spinner_item_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.key_mapping_plan_text.setEnabled(true);
                holder.key_mapping_plan_text.requestFocus();
                isClick = false;
            }
        });

        holder.key_mapping_spinner_item_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(holder.key_mapping_plan_text.getText());
                Constant.planName = holder.key_mapping_plan_text.getText().toString();
                FloatingService.closePopupWindow();
            }
        });

        holder.key_mapping_spinner_item_delete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                List<Plan> planList = LitePal.where("planName = ?", holder.key_mapping_plan_text.getText().toString()).find(Plan.class);
                if(planList!= null && planList.size() >=1){
                    LitePal.deleteAll(Plan.class, "planName = ?" , planList.get(0).getPlanName());
                    LitePal.deleteAll(KeyMappingEntity.class, "planId = ?" , planList.get(0).getId().toString());
                    LitePal.deleteAll(DirectMappingEntity.class, "planId = ?" , planList.get(0).getId().toString());
                    plans.clear();
                    plans = LitePal.findAll(Plan.class);
                    notifyDataSetChanged();
                }
            }
        });





    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private EditText key_mapping_plan_text;
        private ImageView key_mapping_spinner_item_edit;
        private ImageView key_mapping_spinner_item_choose;
        private ImageView key_mapping_spinner_item_delete;



        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            key_mapping_plan_text = itemView.findViewById(R.id.key_mapping_spinner_item_text);
            key_mapping_spinner_item_edit = itemView.findViewById(R.id.key_mapping_spinner_item_edit);
            key_mapping_spinner_item_choose = itemView.findViewById(R.id.key_mapping_spinner_item_choose);
            // 删除
            key_mapping_spinner_item_delete = itemView.findViewById(R.id.key_mapping_spinner_item_delete);

        }

    }

}
