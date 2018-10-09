package com.crm.finance.base;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.crm.finance.R;
import com.crm.finance.util.LogInputUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;


/**
 * Backfire的PopupWindow基类，提供了默认构造方法。 通常在构造方法里只储存当前的Activity，
 * 在ShowWindowAtLocation(int x, int y)中载入界面资源，并显示在指定位置。
 * 
 * @Created by Dipa on 2015/4/27.
 * 
 */
public abstract class BasePopupWindow {

    private static final String TAG = BasePopupWindow.class.getSimpleName();
    // activity
	protected Activity mActivity;
	private PopupWindow mPopupWindow;
	protected boolean isConsumeKeyEvent = false; // 默认不相应返回按键
	protected OnKeyListener keyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (isConsumeKeyEvent) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					//closeWindow();
					return true;
				}
			}
			return false;
		}
	};

	public BasePopupWindow(Activity activity) {
		this.mActivity = activity;
	}

	public final void createPopupWindow(View v) {
		createPopupWindow(v, true);
	}

	public final void createPopupWindow(View v, boolean flag) {
		if (flag) {
			createPopupWindow(v, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
		} else {
			createPopupWindow(v, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}
	}

	public final void createPopupWindow(View v, int widthType, int heightType) {
		this.mPopupWindow = new PopupWindow(v, widthType, heightType, false);
		// if(isConsumeKeyEvent){
		// this.mPopupWindow.setBackgroundDrawable(new BitmapDrawable());//
		// 需要设置一下此参数，点击外边可消失
		// this.mPopupWindow.setOutsideTouchable(true);// 设置点击窗口外边窗口消失
		// }

        // 设置可以获得焦点
        this.mPopupWindow.setFocusable(true);
        // 设置弹窗内可点击
        this.mPopupWindow.setTouchable(true);
        this.mPopupWindow.setOutsideTouchable(true);// 可点击窗口外边
        this.mPopupWindow.setBackgroundDrawable(new BitmapDrawable());//


		this.mPopupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				popupWindowWillDissmiss();
			}
		});
		fixPopupWindow(this.mPopupWindow);
		// mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
		mPopupWindow.setAnimationStyle(R.style.AnimationFade);
	}
	protected PopupWindow getPopupWindow(){
		return mPopupWindow;
	}


	public void setAnimationStype(int id) {
		if (id > 0)
			mPopupWindow.setAnimationStyle(id);
	}

	protected abstract boolean popupWindowWillShow();

	protected abstract void popupWindowDidShow();

	protected abstract void popupWindowWillDissmiss();

	protected final void ShowWindowAtLocation(int gravity, int x, int y) {
		if (this.popupWindowWillShow()) {
			// 这里取的是左上角
			mPopupWindow.showAtLocation(mActivity.getWindow().getDecorView(),
					gravity, x, y);
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	private void addUIListener() {
		if (this.popupWindowWillShow()) {
			mPopupWindow.setFocusable(true);
			// 这里取的是左上角
			mPopupWindow.update();
			if (this.isConsumeKeyEvent()) {
				this.mPopupWindow.getContentView().setFocusable(true);
				this.mPopupWindow.getContentView()
						.setFocusableInTouchMode(true);
				this.mPopupWindow.getContentView()
						.setOnKeyListener(keyListener);
			}
			this.popupWindowDidShow();
		}
	}

	private void fixPopupWindow(final PopupWindow window) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			try {
				final Field fAnchor = PopupWindow.class
						.getDeclaredField("mAnchor");
				fAnchor.setAccessible(true);
				Field listener = PopupWindow.class
						.getDeclaredField("mOnScrollChangedListener");
				listener.setAccessible(true);
				final ViewTreeObserver.OnScrollChangedListener originalListener = (ViewTreeObserver.OnScrollChangedListener) listener
						.get(window);
				ViewTreeObserver.OnScrollChangedListener newListener = new ViewTreeObserver.OnScrollChangedListener() {
					@Override
					public void onScrollChanged() {
						try {
							WeakReference<View> mAnchor = (WeakReference<View>) fAnchor
									.get(window);
							if (mAnchor == null || mAnchor.get() == null) {
								return;
							} else {
								originalListener.onScrollChanged();
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				};
				listener.set(window, newListener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public final void ShowWindowAtLocation(int x, int y) {
		if (!this.mActivity.isFinishing() && this.mPopupWindow != null) {
			this.ShowWindowAtLocation(Gravity.NO_GRAVITY, x, y);
		} else {
            LogInputUtil.e("Error: ", "ShowWindowAtLocation出错");
		}
	}


	public final void ShowWindowAtBottom() {
		if (!this.mActivity.isFinishing() && this.mPopupWindow != null) {
			this.ShowWindowAtLocation(Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL, 0, 0);
		} else {
			LogInputUtil.e("Error: ", "ShowWindowAtLocation出错");
		}
	}


	public final void showWindowOverride(View view) {
		if (this.popupWindowWillShow()) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			LogInputUtil.i(TAG, "left:" + location[0] + "----right:" + location[1]);
			mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
					location[1]);
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	public final void showWindowAtUp(View view) {
		if (this.popupWindowWillShow()) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			LogInputUtil.i(TAG, "left:" + location[0] + "----right:" + location[1]);
			mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
					location[1] - mPopupWindow.getHeight());
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	public final void showWindowAtDown(View view) {
		if (this.popupWindowWillShow()) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			LogInputUtil.i(TAG, "left:" + location[0] + "----right:" + location[1]);
			mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
					location[1] + view.getHeight());
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	public final void showWindowAtDownRight(View view) {
		if (this.popupWindowWillShow()) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			LogInputUtil.i(TAG, "left:" + location[0] + "----right:" + location[1]);
			mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
					+ view.getWidth(), location[1] + view.getHeight());
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	public final void showWindowAtLeft(View view) {
		if (this.popupWindowWillShow()) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			LogInputUtil.i(TAG, "left:" + location[0] + "----right:" + location[1]);
			mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
					- mPopupWindow.getWidth(), location[1]);
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	public final void showWindowAtRight(View view) {
		if (this.popupWindowWillShow()) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			LogInputUtil.i(TAG, "left:" + location[0] + "----right:" + location[1]);
			mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
					+ view.getWidth(), location[1]);
			this.mPopupWindow.setFocusable(true); // 设置此参数获得焦点，否则无法点击
		}
		addUIListener();
	}

	public void setOnkeyListener(OnKeyListener listener) {
		if (listener != null)
			this.keyListener = listener;
	}

	public void showWindowInCenter() {
		if (!this.mActivity.isFinishing() && this.mPopupWindow != null) {
			this.ShowWindowAtLocation(Gravity.CENTER, 0, 0);
		}
	}

	public void showInCenter(){
		showWindowInCenter();
	}

	public synchronized void closeWindow() {
		try {
			if (null != mPopupWindow && mPopupWindow.isShowing()) {
				mPopupWindow.setFocusable(false);
				mPopupWindow.dismiss();
			}
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}

	public boolean isShowing() {
		boolean result = false;
		if (null != mPopupWindow) {
			result = mPopupWindow.isShowing();
		}
		return result;
	}

	public Activity getmActivity() {
		return mActivity;
	}

	public void setmActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}

	protected final boolean isConsumeKeyEvent() {
		return isConsumeKeyEvent;
	}

	public final void setConsumeKeyEvent(boolean isConsumeKeyEvent) {
		this.isConsumeKeyEvent = isConsumeKeyEvent;
	}

}
