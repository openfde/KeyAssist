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
import com.fde.keyassist.entity.AmplifyMappingEntity;
import com.fde.keyassist.entity.CursorEntity;
import com.fde.keyassist.entity.DialogEntity;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.DoubleClickMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.entity.ScaleMappingEntity;
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

    @SuppressLint("NotifyDataSetChanged")
    public void setPlans(List<Plan> newPlans) {
        this.plans = newPlans;
        notifyDataSetChanged();
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
        Plan plan = plans.get(position);
        holder.key_mapping_plan_text.setText(plan.getPlanName());
        holder.key_mapping_plan_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // 按 planId 更新，避免把改名前的名字当查询条件导致二次改名失败
                String newName = holder.key_mapping_plan_text.getText().toString();
                String oldName = plan.getPlanName();
                Plan dbPlan = plan.getId() == null ? null : LitePal.find(Plan.class, plan.getId());
                if (dbPlan != null) {
                    dbPlan.setPlanName(newName);
                    dbPlan.save();
                    // 同步内存中的列表，保证后续绑定的显示名正确
                    plan.setPlanName(newName);
                    if (oldName != null && oldName.equals(Constant.planName)) {
                        Constant.planName = newName;
                        textView.setText(newName);
                    }
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
                String planName = holder.key_mapping_plan_text.getText().toString();
                List<Plan> planList = LitePal.where("planName = ?", planName).find(Plan.class);
                if(planList!= null && planList.size() >=1){
                    Integer planId = planList.get(0).getId();
                    String planIdStr = String.valueOf(planId);
                    // 删除方案时需要同时清掉所有类型的映射，否则会残留孤儿数据
                    LitePal.delete(Plan.class, planId);
                    LitePal.deleteAll(KeyMappingEntity.class, "planId = ?", planIdStr);
                    LitePal.deleteAll(DirectMappingEntity.class, "planId = ?", planIdStr);
                    LitePal.deleteAll(DoubleClickMappingEntity.class, "planId = ?", planIdStr);
                    LitePal.deleteAll(ScaleMappingEntity.class, "planId = ?", planIdStr);
                    LitePal.deleteAll(AmplifyMappingEntity.class, "planId = ?", planIdStr);
                    LitePal.deleteAll(CursorEntity.class, "planId = ?", planIdStr);
                    LitePal.deleteAll(DialogEntity.class, "planId = ?", planIdStr);
                    setPlans(LitePal.findAll(Plan.class));
                    // 删除的是当前选中的方案时，自动切换到第一个可用方案
                    if (planName.equals(Constant.planName)) {
                        if (plans.isEmpty()) {
                            Constant.planName = "";
                            textView.setText("");
                        } else {
                            Constant.planName = plans.get(0).getPlanName();
                            textView.setText(Constant.planName);
                        }
                    }
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
