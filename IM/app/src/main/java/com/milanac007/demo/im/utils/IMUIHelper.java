package com.milanac007.demo.im.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.SearchElement;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.event.LoginEvent;
import com.milanac007.demo.im.event.SocketEvent;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import static com.milanac007.demo.im.event.SocketEvent.Event.CONNECT_MSG_SERVER_FAILED;
import static com.milanac007.demo.im.event.SocketEvent.Event.REQ_MSG_SERVER_ADDRS_FAILED;

public class IMUIHelper {

    // 在视图中，长按用户信息条目弹出的对话框
    public static void handleContactItemLongClick(final UserEntity contact, final Context ctx){
        if(contact == null || ctx == null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle(contact.getMainName());
        String[] items = new String[]{ctx.getString(R.string.check_profile),
                ctx.getString(R.string.start_session)};

        final int userId = contact.getPeerId();
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0 :
//                        IMUIHelper.openUserProfileActivity(ctx, userId);
                        break;
                    case 1 :
//                        IMUIHelper.openChatActivity(ctx,contact.getSessionKey());
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    // 根据event 展示提醒文案
    public static int getLoginErrorTip(LoginEvent event) {
        switch (event.getType()) {
            case LOGIN_AUTH_FAILED:
                return R.string.login_error_general_failed;
            case LOGIN_INNER_FAILED:
                return R.string.login_error_unexpected;
            default :
                return  R.string.login_error_unexpected;
        }
    }

    public static int getSocketErrorTip(SocketEvent event) {
        switch (event.event) {
            case CONNECT_MSG_SERVER_FAILED :
                return R.string.connect_msg_server_failed;
            case REQ_MSG_SERVER_ADDRS_FAILED :
                return R.string.req_msg_server_addrs_failed;
            default :
                return  R.string.login_error_unexpected;
        }
    }

//    // 跳转到聊天页面
//    public static void openChatActivity(Context ctx, String sessionKey) {
//        Intent intent = new Intent(ctx, MessageActivity.class);
//        intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
//        ctx.startActivity(intent);
//    }
//
//
//    //跳转到用户信息页面
//    public static void openUserProfileActivity(Context ctx, int contactId) {
//        Intent intent = new Intent(ctx, UserInfoActivity.class);
//        intent.putExtra(IntentConstant.KEY_PEERID, contactId);
//        ctx.startActivity(intent);
//    }
//
//    public static void  openGroupMemberSelectActivity(Context ctx, String sessionKey) {
//        Intent intent = new Intent(ctx, GroupMemberSelectActivity.class);
//        intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
//        ctx.startActivity(intent);
//    }


    // 对话框回调函数
    public interface dialogCallback{
        public void callback();
    }


	/**
	 * 文字高亮显示，此方法适用于在指定的text前 追加字符串的情况
	 * @param textView
	 * @param text
	 * @param prependStr
	 * @param searchElement
	 */
	public static void setTextHilighted(TextView textView, String text, String prependStr, SearchElement searchElement) {
		String resultStr = prependStr + text;
		textView.setText(resultStr);

		if (textView == null
				|| TextUtils.isEmpty(text)
				|| TextUtils.isEmpty(prependStr)
				|| searchElement ==null) {
			return;
		}

		int changedIndex = prependStr.length();
		int startIndex = searchElement.startIndex + changedIndex;
		int endIndex = searchElement.endIndex + changedIndex;
		if (startIndex < 0 || endIndex > resultStr.length()) {
			return;
		}
		// 开始高亮处理
		int color =  Color.rgb(69, 192, 26);
		textView.setText(resultStr, BufferType.SPANNABLE);
		Spannable span = (Spannable) textView.getText();
		span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}


    // 文字高亮显示
    public static void setTextHilighted(TextView textView, String text,SearchElement searchElement) {
        textView.setText(text);
        if (textView == null
                || TextUtils.isEmpty(text)
                || searchElement ==null) {
            return;
        }

        int startIndex = searchElement.startIndex;
        int endIndex = searchElement.endIndex;
        if (startIndex < 0 || endIndex > text.length()) {
            return;
        }
        // 开始高亮处理
        int color =  Color.rgb(69, 192, 26);
        textView.setText(text, BufferType.SPANNABLE);
        Spannable span = (Spannable) textView.getText();
        span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


	/**
     * 如果图片路径是以  http开头,直接返回
     * 如果不是， 需要集合自己的图像路径生成规律
     * @param avatarUrl
     * @return
     */
    public static String getRealAvatarUrl(String avatarUrl) {
        if (avatarUrl.toLowerCase().contains("http")) {
            return avatarUrl;
        } else if (avatarUrl.trim().isEmpty()) {
            return "";
        } else {
			//TODO
//            return UrlConstant.AVATAR_URL_PREFIX + avatarUrl;
			return "";
        }
    }



    // search helper start
//	public static boolean handleDepartmentSearch(String key, DepartmentEntity department) {
//		if (TextUtils.isEmpty(key) || department == null) {
//			return false;
//		}
//		department.getSearchElement().reset();
//
//		return handleTokenFirstCharsSearch(key, department.getPinyinElement(), department.getSearchElement())
//		|| handleTokenPinyinFullSearch(key, department.getPinyinElement(), department.getSearchElement())
//		|| handleNameSearch(department.getDepartName(), key, department.getSearchElement());
//	}


	public static boolean handleGroupSearch(String key, GroupEntity group) {
		if (TextUtils.isEmpty(key) || group == null) {
			return false;
		}
		group.getSearchElement().reset();

		boolean result = handleTokenFirstCharsSearch(key, group.getPinyinElement(), group.getSearchElement())
		|| handleTokenPinyinFullSearch(key, group.getPinyinElement(), group.getSearchElement())
		|| handleNameSearch(group.getMainName(), key, group.getSearchElement());

		if(!result){
			for(Integer memberId : group.getlistGroupMemberIds()){
				UserEntity userEntity = IMContactManager.instance().findContact(memberId);
				if(userEntity == null)
					continue;

				if(handleContactSearch(key, userEntity)){
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public static boolean handleContactSearch(String key, UserEntity contact) {
		if (TextUtils.isEmpty(key) || contact == null) {
			return false;
		}

		contact.getSearchElement().reset();

		return
				   handleTokenFirstCharsSearch(key, contact.getNickNamePinyinElement(), contact.getSearchElement())
				|| handleTokenPinyinFullSearch(key, contact.getNickNamePinyinElement(), contact.getSearchElement())
				|| handleNameSearch(contact.getNickName(), key, contact.getSearchElement())

				|| handleTokenFirstCharsSearch(key, contact.getPinyinElement(), contact.getSearchElement())
				|| handleTokenPinyinFullSearch(key, contact.getPinyinElement(), contact.getSearchElement())
				|| handleNameSearch(contact.getMainName(), key, contact.getSearchElement())

				|| handleNameSearch(contact.getUserCode(), key, contact.getSearchElement());

	}

	public static boolean handleNameSearch(String name, String key, SearchElement searchElement) {
		int index = name.indexOf(key);
		if (index == -1) {
			return false;
		}

		searchElement.startIndex = index;
		searchElement.endIndex = index + key.length();

		return true;
	}

	public static boolean handleTokenFirstCharsSearch(String key, PinYin.PinYinElement pinYinElement, SearchElement searchElement) {
		return handleNameSearch(pinYinElement.tokenFirstChars, key.toUpperCase(), searchElement);
	}

	public static boolean handleTokenPinyinFullSearch(String key, PinYin.PinYinElement pinYinElement, SearchElement searchElement) {
		if (TextUtils.isEmpty(key)) {
			return false;
		}

		String searchKey = key.toUpperCase();

		//onLoginOut the old search result
		searchElement.reset();

		int tokenCnt = pinYinElement.tokenPinyinList.size();
		int startIndex = -1;
		int endIndex = -1;

		for (int i = 0; i < tokenCnt; ++i) {
			String tokenPinyin = pinYinElement.tokenPinyinList.get(i);

			int tokenPinyinSize = tokenPinyin.length();
			int searchKeySize = searchKey.length();

			int keyCnt = Math.min(searchKeySize, tokenPinyinSize);
			String keyPart = searchKey.substring(0, keyCnt);

			if (tokenPinyin.startsWith(keyPart)) {

				if (startIndex == -1) {
					startIndex = i;
				}

				endIndex = i + 1;
			} else {
				continue;
			}

			if (searchKeySize <= tokenPinyinSize) {
				searchKey = "";
				break;
			}

			searchKey = searchKey.substring(keyCnt, searchKeySize);
		}

		if (!searchKey.isEmpty()) {
			return false;
		}

		if (startIndex >= 0 && endIndex > 0) {
			searchElement.startIndex = startIndex;
			searchElement.endIndex = endIndex;

			return true;
		}

		return false;
	}

    // search helper end



	public static void setViewTouchHightlighted(final View view) {
		if (view == null) {
			return;
		}

		view.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					view.setBackgroundColor(Color.rgb(1, 175, 244));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					view.setBackgroundColor(Color.rgb(255, 255, 255));
				}
				return false;
			}
		});
	}





    // 这个还是蛮有用的,方便以后的替换
	public static int getDefaultAvatarResId(int sessionType) {
		if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
			return R.drawable.tt_default_user_portrait_corner;
		} else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
			return R.drawable.group_default;
		} else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
			return R.drawable.discussion_group_default;
		}

		return R.drawable.tt_default_user_portrait_corner;
	}


	public static void setEntityImageViewAvatarNoDefaultPortrait(ImageView imageView,
			String avatarUrl, int sessionType, int roundPixel) {
		setEntityImageViewAvatarImpl(imageView, avatarUrl, sessionType, false, roundPixel);
	}

	public static void setEntityImageViewAvatarImpl(ImageView imageView,
			String avatarUrl, int sessionType, boolean showDefaultPortrait, int roundPixel) {
		if (avatarUrl == null) {
			avatarUrl = "";
		}

		String fullAvatar = getRealAvatarUrl(avatarUrl);
		int defaultResId = -1;

		if (showDefaultPortrait) {
			defaultResId = getDefaultAvatarResId(sessionType);
		}

		displayImage(imageView, fullAvatar, defaultResId, roundPixel);
	}

	public static void displayImage(ImageView imageView,
			String resourceUri, int defaultResId, int roundPixel) {

		Logger logger = Logger.getLogger();

		logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

		if (resourceUri == null) {
			resourceUri = "";
		}

		boolean showDefaultImage = !(defaultResId <= 0);

		if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
			logger.e("displayimage#, unable to display image");
			return;
		}

		Glide.with(App.getContext())
				.load(resourceUri)
				.dontAnimate()
				.placeholder(R.drawable.msg_pic_fail)
				.error(R.drawable.msg_pic_fail)
				.diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
				.skipMemoryCache(true) //不缓存内存
				.into(imageView);
	}



    public static void displayImageNoOptions(ImageView imageView,
                                    String resourceUri, int defaultResId, int roundPixel) {

        Logger logger = Logger.getLogger();

        logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

        if (resourceUri == null) {
            resourceUri = "";
        }

        boolean showDefaultImage = !(defaultResId <= 0);

        if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
            logger.e("displayimage#, unable to display image");
            return;
        }

		Glide.with(App.getContext())
				.load(resourceUri)
				.dontAnimate()
				.placeholder(R.drawable.msg_pic_fail)
				.error(R.drawable.msg_pic_fail)
				.diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
				.skipMemoryCache(true) //不缓存内存
				.into(imageView);
    }

}
