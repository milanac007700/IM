package com.milanac007.demo.im.adapter;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainFragmentPagerAdapter2 extends FragmentPagerAdapter {

	ArrayList<? extends Fragment> list;

	public MainFragmentPagerAdapter2(FragmentManager fm, ArrayList<? extends Fragment> list) {
		super(fm);
		this.list = list;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Fragment getItem(int index) {
		return list.get(index);
	}


	@Override
	public int getCount() {
		return list.size();
	}

}
