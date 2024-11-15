package com.milanac007.demo.im.adapter;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqguo on 2016/9/14.
 */
public abstract class SelectBaseAdapter extends BaseAdapter {

    private List<Object> mSelectedObject = new ArrayList<>();
    private List<Object> mDisableEditObject = new ArrayList<>();

    public void addSelectedObject(Object object){
        mSelectedObject.add(object);
    }
    public void addSelectedObject(List<? extends Object> list){
        mSelectedObject.addAll(list);
    }

    public void removeSelectedObject(Object object){
        mSelectedObject.remove(object);
    }

    public void removeSelectedMulti(List<? extends Object> list) {
        mSelectedObject.removeAll(list);
    }
    public void removeSelectedObject(int position){
        mSelectedObject.remove(position);
    }

    public void clearSelectedObject(){
        mSelectedObject.clear();
    }

    public boolean isObjectSelected(Object object){
        return mSelectedObject.contains(object);
    }

    public List<? extends Object> allSelectedObject(){
        return mSelectedObject;
    }

    public void addDisableEditObject(Object object){
        mDisableEditObject.add(object);
    }
    public void addDisableEditObject(List<? extends Object> list){
        mDisableEditObject.addAll(list);
    }

    public void removeDisableEditObject(Object object){
        mDisableEditObject.remove(object);
    }
    public void removeDisableEditObject(int position){
        mDisableEditObject.remove(position);
    }

    public void clearDisableEditObject(){
        mDisableEditObject.clear();
    }

    public boolean isObjectDisableEdit(Object object){
        return mDisableEditObject.contains(object);
    }
    public List<? extends Object> allDisableEditObject(){
        return mDisableEditObject;
    }

    public int getSelectItemNum() {
        return mSelectedObject.size();
    }

}
