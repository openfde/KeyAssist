package com.fde.keyassist.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fde.keyassist.R;



public class ConfigManageFragment extends Fragment implements View.OnClickListener{

    private LinearLayout configManageAllConfig;
    private LinearLayout configManageWz;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config_manage, container, false);
        init(view);
        return view;
    }

    public void init(View view){
        configManageAllConfig = view.findViewById(R.id.config_manage_all_config);
        configManageAllConfig.setOnClickListener(this);
        configManageWz = view.findViewById(R.id.config_manage_wz);
        configManageWz.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

    }
}