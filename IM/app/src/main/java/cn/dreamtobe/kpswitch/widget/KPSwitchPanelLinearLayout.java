package cn.dreamtobe.kpswitch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import cn.dreamtobe.kpswitch.IPanelConflictLayout;
import cn.dreamtobe.kpswitch.IPanelHeightTarget;
import cn.dreamtobe.kpswitch.handler.KPSwitchPanelLayoutHandler;

/**
 * The panel container linear layout.
 * Resolve the layout-conflict from switching the keyboard and the Panel.
 * @see KPSwitchPanelLayoutHandler
 */
public class KPSwitchPanelLinearLayout extends LinearLayout implements IPanelHeightTarget,
        IPanelConflictLayout {

    private KPSwitchPanelLayoutHandler panelLayoutHandler;

    

    public KPSwitchPanelLinearLayout(Context context) {
        super(context);
        init(null);
    }

    public KPSwitchPanelLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public KPSwitchPanelLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


  private void init(final AttributeSet attrs) {
      panelLayoutHandler = new KPSwitchPanelLayoutHandler(this, attrs);
  }
	
    @Override
    public void refreshHeight(int panelHeight) {
        panelLayoutHandler.resetToRecommendPanelHeight(panelHeight);
    }

    @Override
    public void onKeyboardShowing(boolean showing) {
        panelLayoutHandler.setIsKeyboardShowing(showing);
    }
	
	@Override
    public boolean isKeyboardShowing() {
        return panelLayoutHandler.isKeyboardShowing();
    }
	
	
    @Override
    public void setVisibility(int visibility) {
        if(panelLayoutHandler.filterSetVisibility(visibility)) {
            return;
        }

        super.setVisibility(visibility);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int[] processedMeasureWHSpec = panelLayoutHandler.processOnMeasure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(processedMeasureWHSpec[0], processedMeasureWHSpec[1]);
    }

    @Override
    public boolean isVisible() {
        return panelLayoutHandler.isVisible();
    }

    @Override
    public void handleShow() {
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

}
