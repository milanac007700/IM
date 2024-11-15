package com.milanac007.demo.im.ui;

/**
 * Created by zqguo on 2016/9/14.
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.milanac007.demo.im.R;


/**圆形的自定义的ImageView*/
public class CircleImageView extends GlideImageView {

    //图片的显示比例的类型
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    //每个像素所占字节数是8位，这样的图片清晰度高，但是比较占用内存，使用时要注意
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLOR_DRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final int DEFAULT_CORNER_RADIAN = 0;

    private Context mContext;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();
    private final RectF mRoundRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();
    private final Paint mMaskPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;//位图渲染器
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius; //drawable的半径
    private float mBorderRadius;

    private boolean mReady;
    private boolean mSetupPending;

    private boolean mIsImg;
    private Paint mTextPaint;
    private String mText = "";
    private float mTextPercent;
    private int mTextColor;
    private boolean isAddLeaveMask = false;
    /**
     * 画带圆角的图片时的圆角弧度
     */
    private int mCornerRadian = 3;

    public CircleImageView(Context context) {
        super(context);

        init(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_custom_border_width, DEFAULT_BORDER_WIDTH);
        mBorderColor = a.getColor(R.styleable.CircleImageView_custom_border_color, DEFAULT_BORDER_COLOR);
        mIsImg = a.getBoolean(R.styleable.CircleImageView_is_img, true);
        mTextPercent = a.getFloat(R.styleable.CircleImageView_text_percent, 50);
        mTextColor = a.getColor(R.styleable.CircleImageView_text_color, Color.WHITE);
        mCornerRadian = a.getDimensionPixelSize(R.styleable.CircleImageView_corner_radian, DEFAULT_CORNER_RADIAN);
        a.recycle();
        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        init(context);
    }

    private void init(Context context) {

        mContext = context;
        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsImg) {
            if (getDrawable() == null) {
                return;
            }
            if (mCornerRadian == DEFAULT_CORNER_RADIAN) {
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
                if (mBorderWidth != 0) {
                    canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
                }
                this.setBackgroundResource(R.color.transparent);
            } else { //画圆角弧度
                canvas.drawRoundRect(mRoundRect, mCornerRadian, mCornerRadian, mBitmapPaint);
            }
        } else {
            int width = getWidth();
            float centre = width/ 2;
            float textSize =  (float) (width*mTextPercent*0.01);
            mTextPaint.setTextSize(textSize);
            float textWidth = mTextPaint.measureText(mText);
            if (textWidth / 2 > centre) {
                textWidth = width -2 ;
            }
            FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            float baseline = (width + fontMetrics.bottom - fontMetrics.top ) / 2  - fontMetrics.descent;
            canvas.drawText(mText, centre - textWidth / 2, (float)(baseline - width*0.04), mTextPaint); //baseline - with*0.04 是一个误差偏移量
        }

        if (isAddLeaveMask){
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mMaskPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }

        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        mIsImg = true;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mIsImg = true;
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mIsImg = true;
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mIsImg = true;
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }




    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);//设置画笔锯齿
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        mBorderRect.set(0, 0, getWidth(), getHeight());
        mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);

        mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth, mBorderRect.height() - mBorderWidth);
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

        mRoundRect.set(new RectF(0, 0, getWidth(), getHeight()));

        updateShaderMatrix();

        setupMaskPaint();

        invalidate();
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
    private void setupMaskPaint(){
        //maskPaint
        if (mMaskPaint != null) {
            Bitmap bitmap = getBitmapFromDrawable(getResources().getDrawable(R.mipmap.dismission));
            BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            float scale;
            float dx = 0;
            float dy = 0;

            Matrix matrix = new Matrix();

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            matrix.set(null);

            if (width * mDrawableRect.height() > mDrawableRect.width() * height) {
                scale = mDrawableRect.height() / (float) height;
                dx = (mDrawableRect.width() - width * scale) * 0.5f;
            } else {
                scale = mDrawableRect.width() / (float) width;
                dy = (mDrawableRect.height() - height * scale) * 0.5f;
            }

            matrix.setScale(scale, scale);
            matrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);

            bitmapShader.setLocalMatrix(matrix);

            mMaskPaint.setAntiAlias(true);//设置画笔锯齿
            mMaskPaint.setShader(bitmapShader);
        }
    }

    public void setText(String text){
        this.mIsImg = false;
        mText = text;
        invalidate();
    }

    public void setTextPercent(float size){
        this.mTextPercent = size;
    }

    public void setTextColor(int color){
        this.mTextColor = color;
    }

    /**设置男女的背景：
     * @param resid   男 : R.drawable.default_head_icon_bg_male;    女：R.drawable.default_head_icon_bg_female
     */
    public void setGenderBackground(int resid){
        this.setBackgroundResource(resid);
    }

    public void setAddLeaveMask(boolean flag){
        isAddLeaveMask = flag;
    }

}