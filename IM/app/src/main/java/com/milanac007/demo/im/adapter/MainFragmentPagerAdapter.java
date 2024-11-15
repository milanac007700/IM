package com.milanac007.demo.im.adapter;

import android.view.ViewGroup;

import com.milanac007.demo.im.fragment.BaseFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<? extends Fragment> mFragmentList;
    private  int mMode;
    private Callback mCallback;

    public MainFragmentPagerAdapter(FragmentManager fm, List<? extends Fragment> list){
        super(fm);
        mFragmentList = list;
    }

    public MainFragmentPagerAdapter(FragmentManager fm, List<? extends Fragment> list, int mode, Callback callback){
        super(fm);
        mFragmentList = list;
        mMode = mode;
        mCallback = callback;
    }

    @Override
    /**
     * 该函数的目的为生成新的 Fragment 对象。重载该函数时需要注意这一点。在需要时，该函数将被 instantiateItem() 所调用。
     *      如果需要向 Fragment 对象传递相对静态的数据时，我们一般通过 Fragment.setArguments() 来进行，这部分代码应当放到 getItem()。
     *      它们只会在新生成 Fragment 对象时执行一遍。
     *
     *      如果需要在生成 Fragment 对象后，将数据集里面一些动态的数据传递给该 Fragment，那么，这部分代码不适合放到 getItem() 中。
     *      因为当数据集发生变化时，往往对应的 Fragment 已经生成，如果传递数据部分代码放到了 getItem() 中，这部分代码将不会被调用。
     *      这也是为什么很多人发现调用 PagerAdapter.notifyDataSetChanged() 后，getItem() 没有被调用的一个原因。
     */
    public Fragment getItem(int position) {
        return mFragmentList == null ? null : mFragmentList.get(position);
    }

    @NonNull
    @Override
    /**
     * instantiateItem()
     *判断一下要生成的 Fragment 是否已经生成过了，如果生成过了，就使用旧的，旧的将被 Fragment.attach()；
     * 如果没有，就调用 getItem() 生成一个新的，新的对象将被 FragmentTransation.add()。
     */
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        BaseFragment fragment =  (BaseFragment)super.instantiateItem(container, position);
        if(fragment != null) {
//            fragment.setMode(mMode);
//            fragment.setListener(mCallback);
        }
        return fragment;

    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    public interface Callback {
        void onItemClick(Object item);
        void getItems(int total, List<Object> list);
        void onError(String error);
    }

}
