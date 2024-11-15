package com.milanac007.demo.im.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.Utils;

/**
 * Created by milanac007 on 2017/1/5.
 */
public class CustomConfirmDialog extends Dialog {

    public CustomConfirmDialog(Context context) {
        super(context);
    }

    public CustomConfirmDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{
        private Context context;
        private String title;
        private String message;
        private Drawable drawable;
        private String positiveBtnText;
        private String negativeBtnText;
        private View contentView;
        private OnClickListener positiveBtnClickListener;
        private OnClickListener negativeBtnClickListener;

        public Builder(Context context){
            this.context = context;
        }

        public Builder setTitle(String title){
            this.title = title;
            return this;
        }

        public Builder setTitle(int title){
            this.title = (String)context.getText(title);
            return this;
        }

        public Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public Builder setMessage(int message){
            this.message = (String)context.getText(message);
            return this;
        }

        public Builder setLeftDrawable(Drawable drawable){
            this.drawable = drawable;
            return this;
        }

        public Builder setPositiveBtn(String positiveBtnText, OnClickListener listener){
            this.positiveBtnText = positiveBtnText;
            this.positiveBtnClickListener = listener;
            return this;
        }

        public Builder setPositiveBtn(int positiveBtnText, OnClickListener listener){
            this.positiveBtnText = (String)context.getText(positiveBtnText);
            this.positiveBtnClickListener = listener;
            return this;
        }

        public Builder setNegativeBtn(String negativeBtnText, OnClickListener listener){
            this.negativeBtnText = negativeBtnText;
            this.negativeBtnClickListener = listener;
            return this;
        }

        public Builder setNegativeBtn(int negativeBtnText, OnClickListener listener){
            this.negativeBtnText = (String)context.getText(negativeBtnText);
            this.negativeBtnClickListener = listener;
            return this;
        }

        public CustomConfirmDialog create(){
            LayoutInflater inflater = LayoutInflater.from(context);
            try {
                final CustomConfirmDialog dialog = new CustomConfirmDialog(context, R.style.Dialog);


            View layout = inflater.inflate(R.layout.confirm_dialog_layout3, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // set the dialog title
            if(TextUtils.isEmpty(title)) {
                layout.findViewById(R.id.confirm_dialog_title).setVisibility(View.GONE);
            }else {
                ((TextView) layout.findViewById(R.id.confirm_dialog_title)).setText(title);
            }

            // set the confirm button
            if (positiveBtnText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveBtnText);
                if (positiveBtnClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveBtnClickListener.onClick(dialog, BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }

            // set the negative button
            if (negativeBtnText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeBtnText);
                if (negativeBtnClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeBtnClickListener.onClick(dialog, BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }

            View btn_divider = layout.findViewById(R.id.btn_divider);
            if (positiveBtnText != null && negativeBtnText != null) {
                btn_divider.setVisibility(View.VISIBLE);
            }else {
                btn_divider.setVisibility(View.GONE);
            }

            // set the content message
            if (message != null) {
                TextView contentView = (TextView) layout.findViewById(R.id.confirm_dialog_message);
                contentView.setText(message);
                if(!message.contains("\n")) {
                    contentView.setGravity(Gravity.CENTER);
                }else {
                    contentView.setGravity(Gravity.LEFT| Gravity.CENTER_VERTICAL);
                }

                if(drawable != null){
                    TextView messageView = ((TextView) layout.findViewById(R.id.confirm_dialog_message));
                    drawable.setBounds( 0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    messageView.setCompoundDrawablePadding(Utils.dp2dx(5));
                    messageView.setCompoundDrawables(drawable, null, null, null);
                }
            }else if(contentView !=null){
                // if no message set
                // add the contentView to the dialog body
                LinearLayout contentLayout = (LinearLayout)layout.findViewById(R.id.confirm_dialog_content);
                contentLayout.removeAllViews();
                contentLayout.addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }else {
                layout.findViewById(R.id.confirm_dialog_content).setVisibility(View.GONE);
            }
            dialog.setContentView(layout);
            return dialog;
            }catch (NoSuchFieldError e){
                e.printStackTrace();
                return null;
            }
        }

        public CustomConfirmDialog create(int layoutId){
            LayoutInflater inflater = LayoutInflater.from(context);
            final CustomConfirmDialog dialog = new CustomConfirmDialog(context, R.style.Dialog);
            View layout = inflater.inflate(layoutId, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // set the dialog title
            if(TextUtils.isEmpty(title)) {
                layout.findViewById(R.id.confirm_dialog_title).setVisibility(View.GONE);
            }else {
                ((TextView) layout.findViewById(R.id.confirm_dialog_title)).setText(title);
            }

            // set the confirm button
            if (positiveBtnText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveBtnText);
                if (positiveBtnClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveBtnClickListener.onClick(dialog, BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }

            // set the negative button
            if (negativeBtnText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeBtnText);
                if (negativeBtnClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeBtnClickListener.onClick(dialog, BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                if(layout.findViewById(R.id.negativeButton) != null) {
                    layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
                }
            }

            dialog.setContentView(layout);
            return dialog;
        }

    }
}
