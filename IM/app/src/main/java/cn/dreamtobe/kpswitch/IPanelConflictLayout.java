
package cn.dreamtobe.kpswitch;

/**
 * The interface used for the panel's container layout and it used in the case of non-full-screen theme window.
 */
public interface IPanelConflictLayout {
    boolean isKeyboardShowing();

    /**
     * @return The real status of Visible or not
     */
    boolean isVisible();

    /**
     * Keyboard->Panel
     *
     * @see cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil#showPanel(android.view.View)
     */
    void handleShow();


    /**
     * Panel->Keyboard
     *
     * @see cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil#showKeyboard
     */
    void handleHide();


    /**\
     * @param isIgnoreRecommendHeight Ignore guaranteeing the panel height equal to the keyboard
     *                                height.
     * @attr ref cn.dreamtobe.kpswitch.R.styleable#KPSwitchPanelLayout_ignore_recommend_height
     * @see cn.dreamtobe.kpswitch.handler.KPSwitchPanelLayoutHandler#resetToRecommendPanelHeight
     * @see cn.dreamtobe.kpswitch.util.KeyboardUtil#getValidPanelHeight(android.content.Context)
     */
    @SuppressWarnings("JavaDoc")
    void setIgnoreRecommendHeight(boolean isIgnoreRecommendHeight);
}
