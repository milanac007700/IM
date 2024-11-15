package cn.dreamtobe.kpswitch.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import cn.dreamtobe.kpswitch.IPanelConflictLayout;
import cn.dreamtobe.kpswitch.IPanelHeightTarget;
import cn.dreamtobe.kpswitch.handler.KPSwitchPanelLayoutHandler;

/**
 * Created by Jacksgong on 3/30/16.
 * <p/>
 * The panel container frame layout.
 * Resolve the layout-conflict from switching the keyboard and the Panel.
 * <p/>
 * @see KPSwitchPanelLinearLayout
 * @see KPSwitchPanelLayoutHandler
 */
public class KPSwitchPanelFrameLayout extends FrameLayout implements IPanelHeightTarget,
        IPanelConflictLayout {

    private KPSwitchPanelLayoutHandler panelLayoutHandler;

    public KPSwitchPanelFrameLayout(Context context) {
        super(context);
        init(null);
    }

    public KPSwitchPanelFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public KPSwitchPanelFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KPSwitchPanelFrameLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                    int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(final AttributeSet attrs) {
        panelLayoutHandler = new KPSwitchPanelLayoutHandler(this, attrs);
        post(new Runnable() {
            @Override
            public void run() {
                View rootView = (View)getParent();
                if(rootView instanceof KPSwitchRootLinearLayout) {
                    KPSwitchRootLinearLayout mTargetRootView = (KPSwitchRootLinearLayout) rootView;
                    listview = getListview(mTargetRootView);
                }
            }
        });
    }

    private PullToRefreshListView.InternalListView listview;
    private PullToRefreshListView.InternalListView getListview(final View view) {
        if (listview != null) {
            return listview;
        }

        if(view instanceof PullToRefreshListView.InternalListView) {
            listview = (PullToRefreshListView.InternalListView)view;
            return listview;
        }

        if(view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            for (int i=0; i< viewGroup.getChildCount(); i++) {
                PullToRefreshListView.InternalListView v = getListview(viewGroup.getChildAt(i));
                if(v != null) {
                    listview = v;
                    return listview;
                }
            }
        }

        return null;
    }

    @Override
    public void setVisibility(int visibility) {
        if (panelLayoutHandler.filterSetVisibility(visibility)) {
            return;
        }
        System.out.println("### PanelLayout: super.setVisibility(): " + visibility);
        super.setVisibility(visibility);
        if(visibility == VISIBLE) {
            if(listview != null) {
//                listview.handlePresssed(true);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("### PanelLayout: onMeasure() ");
        final int[] processedMeasureWHSpec = panelLayoutHandler.processOnMeasure(widthMeasureSpec,
                heightMeasureSpec);

        super.onMeasure(processedMeasureWHSpec[0], processedMeasureWHSpec[1]);
    }

    @Override
    public boolean isKeyboardShowing() {
        return panelLayoutHandler.isKeyboardShowing();
    }

    @Override
    public boolean isVisible() {
        return panelLayoutHandler.isVisible();
    }

    @Override
    public void handleShow() {
        System.out.println("### PanelLayout: handleShow(): super.setVisibility(View.VISIBLE);");
        super.setVisibility(View.VISIBLE);
    }

    @Override
    public void handleHide() {
        panelLayoutHandler.handleHide();
    }

    @Override
    public void setIgnoreRecommendHeight(boolean isIgnoreRecommendHeight) {
        panelLayoutHandler.setIgnoreRecommendHeight(isIgnoreRecommendHeight);
    }

    @Override
    public void refreshHeight(int panelHeight) {
        panelLayoutHandler.resetToRecommendPanelHeight(panelHeight);
    }

    @Override
    public void onKeyboardShowing(boolean showing) {
        panelLayoutHandler.setIsKeyboardShowing(showing);
    }

}
