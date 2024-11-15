package cn.dreamtobe.kpswitch.util;


import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import cn.dreamtobe.kpswitch.handler.KPSwitchPanelLayoutHandler;
import cn.dreamtobe.kpswitch.handler.KPSwitchRootLayoutHandler;

/**
 * <p/>
 * This util will help you control your panel and keyboard easily and exactly with
 * non-layout-conflict.
 * <p/>
 * This util just support the application layer encapsulation, more detail for how to resolve
 * the layout-conflict please Ref {@link KPSwitchRootLayoutHandler}、
 * {@link KPSwitchPanelLayoutHandler}
 
 *  @see KPSwitchRootLayoutHandler
 *  @see KPSwitchPanelLayoutHandler
 
 */
public class KPSwitchConflictUtil {

    // whether current activity is in multi window mode
    private static boolean mIsInMultiWindowMode = false;

    public static void attach(final View panelLayout, /* Nullable */final View switchPanelKeyboardBtn, /* Nullable */final View focusView) {
        attach(panelLayout, switchPanelKeyboardBtn, focusView, null);
    }


    /**
     * Attach the action of {@code switchPanelKeyboardBtn} and the {@code focusView} to
     * non-layout-conflict.
     * <p/>
     * You do not have to use this method to attach non-layout-conflict, in other words, you can
     * attach the action by yourself with invoke methods manually: {@link #showPanel(View)}、
     * {@link #showKeyboard(View, View)}、{@link #hidePanelAndKeyboard(View)}, and in the case of
     * don't invoke this method to attach, and if your activity with the fullscreen-theme, please
     * ensure your panel layout is {@link View#INVISIBLE} before the keyboard is going to show.
     *
     * @param panelLayout            the layout of panel.
     * @param switchPanelKeyboardBtn the view will be used to trigger switching between the panel
     *                               and the keyboard.
     * @param focusView              the view will be focused or lose the focus.
     * @param switchClickListener    the click listener is used to listening the click event for
     *                               {@code switchPanelKeyboardBtn}.
     */
    public static void attach(final View panelLayout,
                                /* Nullable */final View switchPanelKeyboardBtn,
                                /* Nullable */final View focusView,
                               /* Nullable */final SwitchClickListener switchClickListener) {
        final Activity activity = (Activity) panelLayout.getContext();

        if(switchPanelKeyboardBtn != null) {
            switchPanelKeyboardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean switchToPanel = switchPanelAndKeyboard(panelLayout, focusView);
                    if(switchClickListener != null) {
                        switchClickListener.onClickSwitch(v, switchToPanel);
                    }
                }
            });
        }

        if(isHandleByPlaceholder(activity)) {
            focusView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        /*
                         * Show the fake empty keyboard-same-height panel to fix the conflict when
                         * keyboard going to show.
                         * @see KPSwitchConflictUtil#showKeyboard(View, View)
                         */
                        panelLayout.setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
        }
    }

    /**
     * To show the panel(hide the keyboard automatically if the keyboard is showing) with
     * non-layout-conflict.
     *
     * @param panelLayout the layout of panel.
     * @see KPSwitchPanelLayoutHandler
     */
    public static void showPanel(final View panelLayout) {
        final Activity activity = (Activity)panelLayout.getContext();
        panelLayout.setVisibility(View.VISIBLE);
        if(activity.getCurrentFocus() != null) {
            KeyboardUtil.hideKeyboard(activity.getCurrentFocus());
        }
    }

    public static void showPanel(final View panelLayout, final View focusView) {
        System.out.println("### showPanel()");
        panelLayout.setVisibility(View.VISIBLE);
        if(focusView != null) {
            KeyboardUtil.hideKeyboard(focusView);
        }
    }

    /**
     * To show the keyboard(hide the panel automatically if the panel is showing) with
     * non-layout-conflict.
     *
     * @param panelLayout the layout of panel.
     * @param focusView   the view will be focused.
     */
    public static void showKeyboard(final View panelLayout, final View focusView) {
        final Activity activity =  (Activity)panelLayout.getContext();
        KeyboardUtil.showKeyboard(focusView);
        System.out.println("### showKeyboard()");
        if(isHandleByPlaceholder(activity)) {
            panelLayout.setVisibility(View.INVISIBLE);
        }else if (mIsInMultiWindowMode) { //TODO !!!
            panelLayout.setVisibility(View.GONE);
        }
    }


//    public static boolean switchPanelAndKeyboard(final View panelLayout, final View focusView) {
//        boolean switchToPanel = panelLayout.getVisibility() != View.VISIBLE;
//        if(switchToPanel) {
//            showPanel(panelLayout);
//        }else {
//            showKeyboard(panelLayout, focusView);
//        }
//
//        return switchToPanel;
//    }

    /**
     * If the keyboard is showing, then going to show the {@code panelLayout},
     * and hide the keyboard with non-layout-conflict.
     * <p/>
     * If the panel is showing, then going to show the keyboard,
     * and hide the {@code panelLayout} with non-layout-conflict.
     * <p/>
     * If the panel and the keyboard are both hiding. then going to show the {@code panelLayout}
     * with non-layout-conflict.
     *
     * @param panelLayout the layout of panel.
     * @param focusView   the view will be focused or lose the focus.
     * @return If true, switch to showing {@code panelLayout}; If false, switch to showing Keyboard.
     */
    public static boolean switchPanelAndKeyboard(final View panelLayout, final View focusView) {
        boolean switchToPanel = panelLayout.getVisibility() != View.VISIBLE;
        if(switchToPanel) {
            showPanel(panelLayout, focusView);
        }else {
            showKeyboard(panelLayout, focusView);
        }

        return switchToPanel;
    }

    /**
     * Hide the panel and the keyboard.
     * @param panelLayout the layout of panel.
     */
    public static void hidePanelAndKeyboard(final View panelLayout) {
        final Activity activity = (Activity) panelLayout.getContext();

        final View focusView = activity.getCurrentFocus();
        if(focusView != null) {
            KeyboardUtil.hideKeyboard(focusView);
        }

        panelLayout.setVisibility(View.GONE);
    }

    public static void hidePanelAndKeyboard(final View panelLayout, EditText editText) {
        if(editText != null) {
            KeyboardUtil.hideKeyboard(editText);
        }
        panelLayout.setVisibility(View.GONE);
    }

    /**
     * This listener is used to listening the click event for a view which is received the click
     * event to switch between Panel and Keyboard.
     *
     * @see #attach(View, View, View, SwitchClickListener)
     */
    public interface SwitchClickListener {
        /**
         * @param v The view that was clicked.
         * @param switchToPanel If true, switch to showing Panel; If false, switch to showing
         *                      Keyboard.
         */
        void onClickSwitch(View v, boolean switchToPanel);
    }

    /**
     * @param isFullScreen        Whether in fullscreen theme.
     * @param isTranslucentStatus Whether in translucent status theme.
     * @param isFitsSystem        Whether the root view(the child of the content view) is in
     *                            {@code getFitSystemWindow()} equal true.
     * @return Whether handle the conflict by show panel placeholder, otherwise, handle by delay the
     * visible or gone of panel.
     */
    public static boolean isHandleByPlaceholder(boolean isFullScreen, boolean isTranslucentStatus,
                                                boolean isFitsSystem) {
        return isFullScreen || (isTranslucentStatus && !isFitsSystem);
    }

    static boolean isHandleByPlaceholder(final Activity activity) {
        return isHandleByPlaceholder(ViewUtil.isFullScreen(activity),
                ViewUtil.isTranslucentStatus(activity), ViewUtil.isFitsSystemWindows(activity));
    }

}
