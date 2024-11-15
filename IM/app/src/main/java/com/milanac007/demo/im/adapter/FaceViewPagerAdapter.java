package com.milanac007.demo.im.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.milanac007.demo.im.R;
import com.milanac007.demo.im.manager.FaceManager;
import com.milanac007.demo.im.utils.CommonFunction;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by zqguo on 2016/11/14.
 */
public class FaceViewPagerAdapter extends PagerAdapter {
    private int mCount = -1;
    private String mFaceStr;
    private GridViewFaceListener mListener;
    private Context mContext;
    private GridView[] mGridView;
    private DecimalFormat ft = new DecimalFormat("000");

    public interface GridViewFaceListener{
        void onGridViewFaceListener(String str);
    }

    public FaceViewPagerAdapter(Context context, GridViewFaceListener listener){
        mContext = context;
        mListener = listener;
        mCount = FaceManager.FACE_NUM/FaceManager.COUNT_PER_PAGE + 1;
        mGridView = new GridView[mCount];

        for(int i=0; i<mCount; i++){
            GridView gridView = new GridView(mContext);
            gridView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            gridView.setNumColumns(7);
            int padding = CommonFunction.dip2px(10);
            gridView.setPadding(padding, padding, padding, padding);
            gridView.setVerticalSpacing(CommonFunction.dip2px(8));
            gridView.setBackgroundResource(R.color.fragment_bg);
            mGridView[i] = gridView;
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view==o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GridView gridView = mGridView[position];
        final FaceGridAdapter adapter = new FaceGridAdapter(position);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int gridPosition, long id) {

                int faceId = adapter.getImgIds()[gridPosition];
                String output;
                if(faceId == -1){
                    output = "del";
                }else {
                    String faceIdStr = "face" + ft.format(faceId);
                    output = FaceManager.getInstance().find(faceIdStr);
                }

                if(mListener != null){
                    mListener.onGridViewFaceListener(output);
                }
            }
        });

        container.addView(gridView);
        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        GridView gridView = mGridView[position];
        container.removeView(gridView);
    }

    class FaceGridAdapter extends BaseAdapter {
        private final int PER_COUNT = 20;
        private int[] imgIds;
        private int count;
        private LayoutInflater inflater;


        public FaceGridAdapter(int position){
            inflater = LayoutInflater.from(mContext);

            int startCount = PER_COUNT * position;
            int low = FaceManager.FACE_NUM - startCount;
            if(low < PER_COUNT){
                count = low + 1; //1为删除键
            }else {
                count = PER_COUNT+1;
            }

            imgIds = new int[count];
            for(int i=0; i<count-1; i++){
                imgIds[i] =  startCount + i;
            }

            imgIds[count-1] = -1; //最后一个为删除键
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int i) {
            return imgIds[i];
        }

        public int[] getImgIds() {
            return imgIds;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            FaceImgHolder faceImgHolder;
            if(convertView == null){
                faceImgHolder = new FaceImgHolder();
                convertView = inflater.inflate(R.layout.emoji_msg_layout, null);
                faceImgHolder.faceImg = (ImageView)convertView.findViewById(R.id.face_item_img);
                convertView.setTag(faceImgHolder);
            }else {
                faceImgHolder = (FaceImgHolder)convertView.getTag();
            }

            int faceId = (int)getItem(position);
            if(faceId == -1){
                faceImgHolder.faceImg.setImageResource(R.mipmap.msg_del_face);
            }else {
                String faceIdStr = "face" + ft.format(faceId);
//                int resId = FaceManager.getInstance().getFaceId(faceIdStr);
//                faceImgHolder.faceImg.setImageResource(resId);
                AssetManager assetManager = mContext.getAssets();
                try {
                    InputStream in = assetManager.open(String.format("faceimages/%s.png", faceIdStr));
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    faceImgHolder.faceImg.setImageBitmap(bitmap);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            return convertView;
        }

    }

    static class FaceImgHolder{
        ImageView faceImg;
    }
}
