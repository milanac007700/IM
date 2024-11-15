package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.utils.CommonFunction;

/**
 * Created by zqguo on 2016/11/17.
 */
public class AboutOAFragment extends BaseFragment implements View.OnClickListener{

    private View newVersionView;
    private TextView appVersionView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_oa_layout, null);
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_ABOUT_OA;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View mBack = view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText("关于");

        View develoerLayout = view.findViewById(R.id.develoerLayout);
        View versionUpdateLayout = view.findViewById(R.id.versionUpdateLayout);
        newVersionView = view.findViewById(R.id.newVersion);
        appVersionView = (TextView)view.findViewById(R.id.app_version);

        String versionStr = CommonFunction.getVersion();
        if(!TextUtils.isEmpty(versionStr)){
            appVersionView.setText(String.format("%s v%s", getResources().getString(R.string.app_name), versionStr));
        }

        //TODO
//        boolean hasNewVersion = CommonFunction.hasNewVersion();
        boolean hasNewVersion = false;
        if(hasNewVersion){
            newVersionView.setVisibility(View.VISIBLE);
        }else {
            newVersionView.setVisibility(View.INVISIBLE);
        }

        View[] views = {mBack, develoerLayout, versionUpdateLayout};
        for(View v : views){
            v.setOnClickListener(this);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                onBack();
            }break;
            case R.id.versionUpdateLayout:{
//                if(updateMan == null){
//                    updateMan = UpdateManager.getInstance();
//                    updateMan.setUpdateListener(appUpdateCb);
//                }
//                updateMan.checkUpdate(getActivity());

                checkAppUpdate();

            }break;
            default:break;
        }
    }

    public void checkAppUpdate(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_APP_CHECK_UPDATE, 0, null);
    }

}
