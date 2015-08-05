package com.example.tabswitchdemo.tabswitch;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.example.tabswitchdemo.R;
import com.example.tabswitchdemo.SinglePageShow;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.graphics.PorterDuff;

public class TabSwitchRotateCtrl extends View implements OnGestureListener,
		AnimatorUpdateListener {

	private int mCurrentIndex = 0;

	static private String TAG = "TabSwitch";

	private GestureDetector mGestureDetector;

	// private int mOffsetX = 200;

	private float mAngle = 0;
	private int mAngleSpace = 10;
	private int mOverlapStep = 20;

	private float mCenterX = 0;
	private float mCenterY = 0;

	private int mBitmapOffsetY;
	private int mTitleOffsetY;

	private int mBottomLineHeight = 2;
	private int mBottomBarHeight = 20;
	private int mBottomShadowHeight = 3;

	private Bitmap mBottomShadow;
	private Bitmap mShadow;

	private Bitmap mClose;

	private List<Data> mData = new ArrayList<Data>();

	private Observer mObserver;
	
	public enum State {
		State_Normal,
		State_Removing_One,
		State_Removing_All,
		State_Rotating,
		State_Combine,
	};
	
	private State mState;
	private int mRemovingIndex = -2;
	private int mMovingupDistance = 0;
	
	private int mCloseTipArrowY = 0;
	
	private int mGapAngle = 0;
	private int mCombingStartIndex = 0;
	private int mCombingEndIndex = 0;
	
	private float mViewportRatio = 0;
	
	public void setState(State state) {
		mState = state;
	}

	public TabSwitchRotateCtrl(Context context, AttributeSet attrs) {
		super(context, attrs);

		mGestureDetector = new GestureDetector(context, this);
		mState = State.State_Normal;
		
		mCloseTipArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mThumbnailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mBitmapArrow = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.multi_window_close_tips_arrow);
	}

	public void setObserver(Observer observer) {
		mObserver = observer;
	}

	interface Observer {
		void onSwitch(int index);
		void onClose(int index);
		void onCloseAll();
		void onViewSizeChanged();
	}

	class Data {
		Bitmap bmp;
		String title;
		String path;
		String des;
		Matrix matrix;
	}

	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed &&  null != mObserver)
			mObserver.onViewSizeChanged();
	}
	
	public void setData(List<Bitmap> bmps, List<String> paths,List<String> des,List<String> titles, int index, float viewportRatio) {
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}

		mData.clear();

		if (-1 != index)
			mCurrentIndex = index;
		mViewportRatio = viewportRatio;

		updateBmps(bmps,paths, des, titles);
		
	}
	public List<Data> getData()
	{
		return this.mData;
	}
	private void updateBmps(List<Bitmap> bmps,  List<String> paths, List<String> des,List<String> titles) {
		int view_width = getWidth();
		int view_height = getHeight();
		
		mData.clear();
		
		// rcNine define the shadow ring
		DisplayMetrics dm = getResources().getDisplayMetrics();
		RectF rcNine = new RectF(15 * dm.density, 10 * dm.density, 15 * dm.density, 20 * dm.density);

		// thumbnail width, 1/2 of view width
		int width = (int) (view_width / 2.0);
		int height = (int) (width * mViewportRatio);

		// the start y of thumbnail area
		mBitmapOffsetY = (int) (view_height - height - rcNine.top);
		mTitleOffsetY = mBitmapOffsetY - dp2px(getContext(), 20);

		// rotate center
		mCenterX = view_width / 2;
		double radius = ((double)width / 4) / Math.sin(Math.toRadians((double)mAngleSpace / 4));
		mCenterY = (float)radius + mBitmapOffsetY + (float)height / 2;

		mBottomLineHeight = dp2px(getContext(), 1);
		mBottomBarHeight = dp2px(getContext(), 6);
		mBottomShadowHeight = dp2px(getContext(), 3);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;

		mBottomShadow = BitmapFactory.decodeResource(getResources(),
				R.drawable.multi_tab_bottom_shadow);

		mShadow = BitmapFactory.decodeResource(getResources(),
				R.drawable.multi_tab_shadow);

		mClose = BitmapFactory.decodeResource(getResources(),
				R.drawable.multi_tab_close);

		for (int i = 0; i < bmps.size(); i++) {
			Data data = new Data();
			Bitmap bmp0 = bmps.get(i);
			data.title = titles.get(i);
			data.path = paths.get(i);
			data.des = des.get(i);
			Bitmap newbmp = Bitmap.createBitmap(
					(int) (width + rcNine.left + rcNine.right), (int) (height
							+ rcNine.top + rcNine.bottom), Config.ARGB_8888);

			Canvas cv = new Canvas(newbmp);

			// draw shadow first
			RectF rectf = new RectF(0, 0, newbmp.getWidth(), newbmp.getHeight());
			NinePatch np = new NinePatch(mShadow, mShadow.getNinePatchChunk(),
					null);
			np.draw(cv, rectf);
			
			// draw thumbnail or white
			if (bmp0 != null) {
				RectF dst = new RectF(rcNine.left, rcNine.top,
						(width + rcNine.left), (height + rcNine.top));
				
				int srcWidth = bmp0.getWidth();
				int srcHeight = bmp0.getHeight();
				float destAspectRatio = dst.height() / dst.width();
				float srcAspectRatio = (float)bmp0.getHeight() / (float)bmp0.getWidth();
				if (destAspectRatio != srcAspectRatio) {
					if (destAspectRatio > srcAspectRatio) {
						Paint paint = new Paint();
						paint.setColor(Color.WHITE);
						cv.drawRect(dst, paint);
						
						int newDestHeight = (int)(dst.width() * srcAspectRatio);
						dst = new RectF(rcNine.left, rcNine.top,
								(width + rcNine.left), (newDestHeight + rcNine.top));
					} else {
						srcHeight = (int) ((float)srcWidth * destAspectRatio);
					}
				}
				Rect src = new Rect(0, 0, srcWidth, srcHeight);
				
				

				cv.drawBitmap(bmp0, src, dst, null);
			} else {
				Paint paint = new Paint();
				paint.setColor(Color.WHITE);
				RectF rc = new RectF(rcNine.left, rcNine.top,
						rcNine.left + width, rcNine.top + height);
				cv.drawRect(rc, paint);
			}

			// draw close button
//			cv.drawBitmap(mClose, rcNine.left + width - mClose.getWidth(),
//					rcNine.top, null);

			data.bmp = newbmp;

			mData.add(data);
		}

		setCurrentIndex(mCurrentIndex);

		this.mMovingupDistance = 0;
		this.mRemovingIndex = -2;
		cancelAllAnim(true, true);
	}

	public float getAngle() {
		return mAngle;
	}

	public void setAngle(float angle) {
		mAngle = angle;

		updateMatric();

		invalidate();
	}
	
	public int getMovingupdistance() {
		return mMovingupDistance;
	}
	public void setMovingupdistance(int distance) {
		mMovingupDistance = distance;

		updateMatric();

		invalidate();
	}
	public int getCloseTipPostion() {
		return mCloseTipArrowY;
	}
	public void setCloseTipPosition(int y) {
		mCloseTipArrowY = y;

		invalidate();
	}
	public int getGapAngle() {
		return mGapAngle;
	}
	public void setGapAngle(int angle) {
		mGapAngle = angle;

		updateMatric();
		
		invalidate();
	}

	public void setCurrentIndex(int index) {
		mCurrentIndex = index;
		float angle = -mAngleSpace * index;
		setAngle(angle);
	}
	public int getCurrentIndex()
	{
		return this.mCurrentIndex;
	}
	private Paint mCloseTipArrowPaint;
	private Paint mTitlePaint;
	private Paint mThumbnailPaint;
	private Paint mBackgroundPaint;
	private Bitmap mBitmapArrow;
	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		if (mData.size() == 0)
			return;

		// paint thumbnail and title
		PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0,
				Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		canvas.setDrawFilter(pfd);
		
		// close all tip arrow
		if (mState == State.State_Removing_All) {
			float X =  getWidth() / 2 - mBitmapArrow.getWidth() / 2;
			int arrowAlpha = 255;
			if (mCloseTipArrowY < 2 * mBitmapArrow.getHeight()) {
				arrowAlpha = mCloseTipArrowY * 255 / (2 * mBitmapArrow.getHeight());
			}

			mCloseTipArrowPaint.setAlpha(arrowAlpha);
			canvas.drawBitmap(mBitmapArrow, X, mCloseTipArrowY - mBitmapArrow.getHeight(), mCloseTipArrowPaint);
		}

		float textsize = dp2px(getContext(), 18);
		mTitlePaint.setTextSize(textsize);
		mTitlePaint.setColor(Color.WHITE);
		
		mThumbnailPaint.setAlpha(255);
		int DisappearingAlpha = 255;
		if (mState == State.State_Removing_All ||
				mState == State.State_Removing_One) {
			DisappearingAlpha = (this.getHeight() - mMovingupDistance) * 255 / this.getHeight();
		}
		if (mState == State.State_Removing_All) {
			mThumbnailPaint.setAlpha(DisappearingAlpha);
			mTitlePaint.setAlpha(DisappearingAlpha);
		}

		for (int i = 0; i < mData.size(); i++) {
			// under state except State_Removing_All, need draw 3 at most
			if (mState != State.State_Removing_All) {
				float angle = mAngle + mAngleSpace * i;
				if (Math.abs(angle) > mAngleSpace * 2)
					continue;
			}
			// draw thumbnail, use matrix to translate and rotate
			Data data = mData.get(i);
			if (mState == State.State_Removing_One && i == mRemovingIndex) {
				mThumbnailPaint.setAlpha(DisappearingAlpha);
				canvas.drawBitmap(data.bmp, data.matrix, mThumbnailPaint);
				mThumbnailPaint.setAlpha(255);
			} else {
				canvas.drawBitmap(data.bmp, data.matrix, mThumbnailPaint);
			}

			String title = data.title;
			if (mState == State.State_Removing_All) {
				title = getContext().getResources().getString(R.string.slide_up_to_close_all_tab);
			} else {
				try {
					title = truncateString(title, 28, "...");
				} catch (UnsupportedEncodingException e) {
					Log.d(TAG, "truncate title error", e);
				}
			}

			if (mState != State.State_Removing_All) {
				// draw title
				float textW = (int) mTitlePaint.measureText(title);
				float angle = mAngle + mAngleSpace * i;
				float left = (float) (Math.tan(Math.toRadians(angle)) * getWidth() * 8)
					+ getWidth() / 2 - textW / 2;
				if (mState == State.State_Removing_One && i == mRemovingIndex) {
					mTitlePaint.setAlpha(DisappearingAlpha);
					canvas.drawText(title, left, mTitleOffsetY - mMovingupDistance, mTitlePaint);
					mTitlePaint.setAlpha(255);
				} else {
					canvas.drawText(title, left, mTitleOffsetY, mTitlePaint);
				}
			} else if (i == mCurrentIndex) {	// for State_Removing_All, draw title once
				// draw title
				float textW = (int) mTitlePaint.measureText(title);
				float left =  getWidth() / 2 - textW / 2;
				canvas.drawText(title, left, mTitleOffsetY - mMovingupDistance, mTitlePaint);
			}
		}

		if (mData.size() > 1) {
			// bottom bar
			mBackgroundPaint.setColor(getResources().getColor(R.color.tab_switch_bottom_bar_background));
			Rect r1 = new Rect(0, getHeight() - mBottomBarHeight, getWidth(),
					getHeight());
			canvas.drawRect(r1, mBackgroundPaint);

			// bottom shadow
			r1.set(0, 0, mBottomShadow.getWidth(),
					mBottomShadow.getHeight());
			Rect r2 = new Rect(0, getHeight() - mBottomBarHeight - mBottomShadowHeight,
					getWidth(), getHeight() - mBottomBarHeight);
			canvas.drawBitmap(mBottomShadow, r1, r2, null);

			// indicator line background
			mBackgroundPaint.setColor(getResources().getColor(R.color.tab_switch_bottom_bar_background));
			r1.set(0, getHeight() - mBottomLineHeight, getWidth(),
					getHeight());
			canvas.drawRect(r1, mBackgroundPaint);

			// the white indicator
			int w = getWidth() / mData.size();
			mBackgroundPaint.setColor(Color.WHITE);
			int x = (int) (-getWidth() * mAngle / (mAngleSpace * mData.size()));
			r1.set(x, getHeight() - mBottomLineHeight, x + w,
					getHeight());
			canvas.drawRect(r1, mBackgroundPaint);
		} else {
			// bottom shadow
			Rect r1 = new Rect(0, 0, mBottomShadow.getWidth(),
					mBottomShadow.getHeight());
			Rect r2 = new Rect(0, getHeight() - mBottomShadowHeight,
					getWidth(), getHeight());
			canvas.drawBitmap(mBottomShadow, r1, r2, null);
		}
	}

	// update everyone's matrix
	public void updateMatric() {
		for (int i = 0; i < mData.size(); i++) {
			Data data = mData.get(i);
			data.matrix = new Matrix();

			if (mState == State.State_Normal || mState == State.State_Rotating ||
					(mState == State.State_Removing_One && mRemovingIndex != i) ||
					(mState == State.State_Combine && !(i >= this.mCombingStartIndex && i <= this.mCombingEndIndex))) {
				float angle = mAngle + mAngleSpace * i;
				data.matrix.setTranslate((getWidth() - data.bmp.getWidth()) / 2,
						mBitmapOffsetY);
				data.matrix.postRotate(angle, mCenterX, mCenterY);
			} else if (mState == State.State_Removing_All){
				int startX = (getWidth() - data.bmp.getWidth()) / 2 + (int)(mAngle / mAngleSpace) * dp2px(getContext(), mOverlapStep);
				int xPos = startX + dp2px(getContext(), mOverlapStep) * i;
				data.matrix.setTranslate(xPos, mBitmapOffsetY - mMovingupDistance);
			} else if (mState == State.State_Removing_One && mRemovingIndex == i) {
				float angle = mAngle + mAngleSpace * i;
				data.matrix.setTranslate((getWidth() - data.bmp.getWidth()) / 2,
						mBitmapOffsetY - mMovingupDistance);
				data.matrix.postRotate(angle, mCenterX, mCenterY);
			} else if (mState == State.State_Combine && i >= this.mCombingStartIndex && i <= this.mCombingEndIndex) {
				float angle = mAngle + mAngleSpace * i + mGapAngle;
				data.matrix.setTranslate((getWidth() - data.bmp.getWidth()) / 2,
						mBitmapOffsetY);
				data.matrix.postRotate(angle, mCenterX, mCenterY);
			}
		}
	}

	private ValueAnimator mRestoreAnim;
	private ValueAnimator mFlingAnim;
	private ValueAnimator mMovingUpAnim;
	private ValueAnimator mMovingDownAnim;
	private ValueAnimator mCloseTipAnim;
	private ValueAnimator mCombiningAnim;

	private void restorePos() {
		// under removing all, could not scroll
		if (mState == State.State_Removing_All)
			return;

		int index = 0;

		// find the picture with smallest angle
		float minangle = 360;
		for (int i = 0; i < mData.size(); i++) {
			float angle = mAngle + mAngleSpace * i;
			if (Math.abs(angle) < Math.abs(minangle)) {
				index = i;
				minangle = angle;
			}
		}
		mCurrentIndex = index;

		if (mRestoreAnim != null)
			mRestoreAnim.cancel();
		
		if (0 == minangle) {
			invalidate();
			return;
		}

		// rotate the selected one back to middle
		float dest = (float) (-mAngleSpace * index);
		animRestore(dest);
	}
	
	private boolean isAnimating() {
		if (mFlingAnim != null && mFlingAnim.isRunning() ||
				mRestoreAnim != null && mRestoreAnim.isRunning() ||
				mMovingUpAnim != null && mMovingUpAnim.isRunning() ||
				mMovingDownAnim != null && mMovingDownAnim.isRunning())
			return true;
		else
			return false;
	}

	private void cancelAllAnim(boolean cancleRotateAnim, boolean cancelMovingUpDownAnim) {
		if (cancleRotateAnim) {
			if (mRestoreAnim != null && mRestoreAnim.isRunning())
				mRestoreAnim.cancel();
			if (mFlingAnim != null && mFlingAnim.isRunning())
				mFlingAnim.cancel();
		}
		
		if (cancelMovingUpDownAnim) {
			if (mMovingUpAnim != null && mMovingUpAnim.isRunning())
				mMovingUpAnim.cancel();
			if (mMovingDownAnim != null && mMovingDownAnim.isRunning())
				mMovingDownAnim.cancel();
		}
	}

	// rotate selected picture back to center position animation
	private void animRestore(float dest) {
		cancelAllAnim(true, false);

		mRestoreAnim = ObjectAnimator.ofFloat(this, "angle", mAngle, dest);
		mRestoreAnim.setInterpolator(new OvershootInterpolator());
		mRestoreAnim.setDuration(500);
		mRestoreAnim.addUpdateListener(this);
		mRestoreAnim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mState = State.State_Normal;
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mRestoreAnim.start();
	}

	// rotate animation
	private void animFling(float dest, long duration) {
		cancelAllAnim(true, false);

		mFlingAnim = ObjectAnimator.ofFloat(this, "angle", mAngle, dest);
		mFlingAnim.setInterpolator(new DecelerateInterpolator());
		mFlingAnim.setDuration(duration);
		mFlingAnim.addUpdateListener(this);
		mFlingAnim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				restorePos();
				mState = State.State_Normal;
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mFlingAnim.start();
	}
	
	//向上移动
	public void animMovingUp() {
		cancelAllAnim(false, true);

		mMovingUpAnim = ObjectAnimator.ofInt(this, "movingupdistance", mMovingupDistance, this.getHeight());
		mMovingUpAnim.setInterpolator(new AccelerateInterpolator());
		mMovingUpAnim.setDuration(200 * (this.getHeight() - mMovingupDistance) / this.getHeight());
		mMovingUpAnim.addUpdateListener(this);
		mMovingUpAnim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override//删除动画
			public void onAnimationEnd(Animator animation) {
				if (mState == State.State_Removing_All) {
					mState = State.State_Normal;
					mMovingupDistance = 0;

					Toast.makeText(getContext(), "亲，你不是超人，一下子不能选择这么多的", Toast.LENGTH_SHORT).show();
					setCurrentIndex(getCurrentIndex());
					/*if (null != mObserver)
						mObserver.onCloseAll();*/
				} else if (mState == State.State_Removing_One) {
					mState = State.State_Normal;
					mMovingupDistance = 0;

					//if (null != mObserver)
					//	mObserver.onClose(mRemovingIndex);
//					closeOneTab(mRemovingIndex);
					Intent intent = new Intent(getContext(), SinglePageShow.class);
					intent.putExtra("cur_index", getCurrentIndex());
					intent.putExtra("icon_path", mData.get(getCurrentIndex()).path);
					intent.putExtra("cur_des", mData.get(getCurrentIndex()).des);
					getContext().startActivity(intent);
//					setCurrentIndex(getCurrentIndex());	恢复原图
					
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mMovingUpAnim.start();
	}
	
	
	private void animMovingDown() {
		cancelAllAnim(false, true);

		mMovingDownAnim = ObjectAnimator.ofInt(this, "movingupdistance", mMovingupDistance, 0);
		mMovingDownAnim.setInterpolator(new AccelerateInterpolator());
		mMovingDownAnim.setDuration(100 * mMovingupDistance / (this.mBitmapOffsetY / 2));
		mMovingDownAnim.addUpdateListener(this);
		mMovingDownAnim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mState == State.State_Removing_One) {
					mState = State.State_Normal;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mMovingDownAnim.start();
	}
	
	private void animCloseTipArrow() {
		mCloseTipAnim = ObjectAnimator.ofInt(this, "CloseTipPosition", mTitleOffsetY, 0);
		mCloseTipAnim.setDuration(2000);
		mCloseTipAnim.setRepeatMode(ValueAnimator.RESTART);
		mCloseTipAnim.setRepeatCount(ValueAnimator.INFINITE);
		mCloseTipAnim.addUpdateListener(this);
		mCloseTipAnim.start();
	}
	
	private void animCombining(int direction) {
		mCombiningAnim = ObjectAnimator.ofInt(this, "GapAngle", mAngleSpace * direction, 0);
		mCombiningAnim.setDuration(200);
		mCombiningAnim.addUpdateListener(this);
		mCombiningAnim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mState == State.State_Combine) {
					mState = State.State_Normal;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mCombiningAnim.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean needMoveUp = false;
		boolean needMoveDown = false;
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mRestoreAnim != null && mRestoreAnim.isRunning())
				mRestoreAnim.cancel();
			break;

		case MotionEvent.ACTION_UP:
			if (mState == State.State_Rotating) {
				restorePos();
				mState = State.State_Normal;
			}
			
			if (!isAnimating()) {
				if (mState == State.State_Removing_One ||
						mState == State.State_Removing_All) {
					if (mMovingupDistance >= this.mBitmapOffsetY / 2) {
						needMoveUp = true;
					} else if (mMovingupDistance > 0) {
						needMoveDown = true;
					}
				}
			}

			break;

		case MotionEvent.ACTION_MOVE:

			break;

		case MotionEvent.ACTION_CANCEL:
			if (mState == State.State_Rotating) {
				restorePos();
				mState = State.State_Normal;
			}
			
			if (!isAnimating()) {
				if (mState == State.State_Removing_One ||
						mState == State.State_Removing_All) {
					if (mMovingupDistance >= this.mBitmapOffsetY / 2) {
						needMoveUp = true;
					} else if (mMovingupDistance > 0) {
						needMoveDown = true;
					}
				}
			}

			break;
		}
		boolean processed = mGestureDetector.onTouchEvent(event);
		if (!processed) {
			if (needMoveUp) {
					animMovingUp();
				} else if (needMoveDown) {
					animMovingDown();
				}
		}
		
		return processed;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	//点击图片
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		int x = (int) e.getX();
		int y = (int) e.getY();

		// above the picture area
		if (y < mBitmapOffsetY)
			return false;

		int centerX = getWidth() / 2;
		Bitmap bmp = mData.get(mCurrentIndex).bmp;
		int left = (centerX - bmp.getWidth() / 2);
		int right = (centerX + bmp.getWidth() / 2);
		int close_width = mClose.getWidth();
		
		// 删除所有
		if (mState == State.State_Removing_All) {
			int rightMost = right + (mData.size() - 1 - mCurrentIndex) * dp2px(getContext(), mOverlapStep);
			if (mObserver != null) {
				Rect rcClose = new Rect(rightMost - close_width, mBitmapOffsetY,
						rightMost, mBitmapOffsetY + close_width);
				if (rcClose.contains(x, y)) {	// hit close button
					if (null != mObserver)
						mObserver.onCloseAll();
					return false;
				}
			}

			// return to normal state
			mState = State.State_Normal;
			updateMatric();
			invalidate();
			
			return false;
		}

		Intent intent = new Intent(getContext(), SinglePageShow.class);
		int cur_index = getCurrentIndex();
		// in the middle picture area
		if (x > left && x < right) {
			if (mObserver != null) {
				Rect rcClose = new Rect(right - close_width, mBitmapOffsetY,
						right, mBitmapOffsetY + close_width);
	
					mObserver.onSwitch(mCurrentIndex);
					setCurrentIndex(mCurrentIndex);
					

			}
		} else if (x < left) {	// left side of middle picture, always considered hit left one
			if (mObserver != null && mCurrentIndex - 1 >= 0) {
				Rect rcClose = new Rect(left - close_width, mBitmapOffsetY,
						left, mBitmapOffsetY + close_width);
			
					mObserver.onSwitch(mCurrentIndex - 1);
					cur_index = cur_index - 1;
					setCurrentIndex(cur_index);
					
				
			}
		} else if (x > right) {	// right side of middle picture, always considered hit right one
			if (mObserver != null && mCurrentIndex + 1 < mData.size()) {
				mObserver.onSwitch(mCurrentIndex + 1);
				setCurrentIndex(mCurrentIndex+1);
				
				cur_index = cur_index + 1;
				
			}
		}

		intent.putExtra("cur_index", getCurrentIndex());
		intent.putExtra("icon_path", mData.get(getCurrentIndex()).path);
		intent.putExtra("cur_des", mData.get(getCurrentIndex()).des);
		getContext().startActivity(intent);
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// if in normal state, may trigger state transition
		if (mState == State.State_Normal) {
			// start rotate vertical
			if (Math.abs(e2.getX() - e1.getX()) > Math.abs(e2.getY() - e1.getY())) {
				mState = State.State_Rotating;
			} else {
				// if move up, and hit success, start to removing one
				if (distanceY > 0) {
					int hitIndex = hitIndexTest(e1.getX(), e1.getY());
					if (-1 != hitIndex) {
						mRemovingIndex = hitIndex;
						mState = State.State_Removing_One;
					}
				}
			}
		}

		if (mState == State.State_Rotating) {
			float deltx = -distanceX;
			float angle = (float) Math.toDegrees(Math.atan(deltx
					/ (mCenterY - e2.getY())));
			mAngle += angle;

			updateMatric();
			invalidate();

			return true;
		}
		
		if (mState == State.State_Removing_One) {
			mMovingupDistance = (int)(e1.getY() - e2.getY());
			if (mMovingupDistance < 0)
				mMovingupDistance = 0;
			updateMatric();
			invalidate();
			return true;
		}

		if (mState == State.State_Removing_All) {
			mRemovingIndex = -1;	// all
			mMovingupDistance = (int)(e1.getY() - e2.getY());
			if (mMovingupDistance < 0)
				mMovingupDistance = 0;
			updateMatric();
			invalidate();
			return true;
		}

		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		cancelAllAnim(true, true);
		
		if (mState != State.State_Normal)
			return;

		int x = (int) e.getX();
		int y = (int) e.getY();

		// above the picture area
		if (y < mBitmapOffsetY)
			return;

		int centerX = getWidth() / 2;
		Bitmap bmp = mData.get(mCurrentIndex).bmp;
		int left = (centerX - bmp.getWidth() / 2);
		int right = (centerX + bmp.getWidth() / 2);
		// in the middle picture area
		if (x > left && x < right) {
			mState = State.State_Removing_All;
			
			animCloseTipArrow();
			
			mRemovingIndex = -1;
			mMovingupDistance = 0;

			updateMatric();
			invalidate();
		}
		return;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (mState == State.State_Removing_One ||
				mState == State.State_Removing_All) {
			if (velocityY < 0) {	// up
				animMovingUp();
			} else if (velocityY > 0){ // down
				animMovingDown();
			} else {	// no Y speed, depends on position
				if (this.mMovingupDistance >= this.mBitmapOffsetY / 2) {
					animMovingUp();
				} else {
					animMovingDown();
				}
			}

			return true;
		}

		// under normal state, try moving up
		if (mState == State.State_Normal && Math.abs(velocityY) > Math.abs(velocityX) && e2.getY() < e1.getY()) {
			mRemovingIndex = -2;
			int hitIndex = hitIndexTest(e1.getX(), e1.getY());
			if (-1  != hitIndex) {
				mState = State.State_Removing_One;
				
				mRemovingIndex = hitIndex;
				mMovingupDistance = (int)(e1.getY() - e2.getY());
				animMovingUp();
			}
		} else {	// rotating
			mState = State.State_Rotating;

			// calculate angle rotated per speed
			float speed = velocityX;
	
			// unit pixels/second
			int titem = 2000;
			if (speed > 0 && speed < titem)
				speed = titem;
	
			if (speed < 0 && speed > -titem)
				speed = -titem;
	
			float angle = mAngleSpace * (speed / titem);
	
			if (Math.abs(speed) >= 3000) {
				angle = angle * (speed / 3000);
				if (speed < 0)
					angle = -angle;
			}
	
			// limit the angle
			float angle_max = 0;
			if (angle > 0) {	// at most, go to the first one
				angle_max = (float) ((mCurrentIndex + 0.5) * mAngleSpace);
	
				if (angle > angle_max)
					angle = angle_max;
			} else {	// at most, go to the last one
				angle_max = (float) (-(mData.size() - mCurrentIndex - 0.5) * mAngleSpace);
				
				if (angle < angle_max)
					angle = angle_max;
			}
	
			// duration
			long duration = 200;
			if (Math.abs(angle) > mAngleSpace)
				duration = (long) (Math.abs(angle) * 200 / mAngleSpace);

			// go
			animFling(mAngle + angle, duration);
	
			Log.d(TAG, String.format("onFling x=%f angle=%f max=%f", velocityX,	angle, angle_max));
		}

		return true;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		// Log.d(TAG,String.format("onAnimationUpdate %f", mAngle));
	}

	public boolean processKeyBack() {
		if (mState == State.State_Removing_All) {
			mState = State.State_Normal;
			updateMatric();
			invalidate();
			return true;
		}
		
		return false;
	}
	
	private int hitIndexTest(float x, float y) {
		int result = -1;

		// above the picture area
		if (y < mBitmapOffsetY)
			return -1;

		int centerX = getWidth() / 2;
		Bitmap bmp = mData.get(mCurrentIndex).bmp;
		int left = (centerX - bmp.getWidth() / 2);
		int right = (centerX + bmp.getWidth() / 2);
		if (x > left && x < right) {
			result = mCurrentIndex;
		} else if (x < left) {
			if (mCurrentIndex - 1 >= 0) {
				result = mCurrentIndex - 1;
			}
		} else if (x > right) {	
			if (mCurrentIndex + 1 < mData.size()) {
				result = mCurrentIndex + 1;
			}
		}
		
		return result;
	}
	
	private void closeOneTab(int index) {
		if (mObserver != null) {
			mObserver.onClose(index);
		}
		
		if (mData.size() <= 1)
			return;
		
		boolean atEdge = false;
		if (index == 0 || index == mData.size() - 1)
			atEdge = true;

		// adjust CurrentIndex, Angle, combine area
		// to reflect the state after mData.remove(index);
		int direction = 1;
		if (index >= mCurrentIndex) {	// combine from right
			mCombingStartIndex = index;
			mCombingEndIndex = mData.size() - 2;
			if (index == mCurrentIndex && mCurrentIndex == 0)
				mAngle += mAngleSpace;
			
			direction = 1;
		} else {	// combine from left
			mCombingStartIndex = 0;
			mCombingEndIndex = index - 1;
			mCurrentIndex -= 1;
			mAngle += mAngleSpace;
			direction = -1;
		}

		mData.remove(index);

		// 
		if (atEdge) {
			this.restorePos();
		} else {
			mState = State.State_Combine;
			mGapAngle = direction * mAngleSpace;
			animCombining(direction);
		}
	}
	
	private String truncateString(String input, int byteLen, String hint)
		throws UnsupportedEncodingException
	{
		input = input.replaceAll("[^\u4E00-\u9FA5\u3000-\u303F\uFF00-\uFFEF\u0000-\u007F\u201c-\u201d]", " ");
		
		int counterOfDoubleByte = 0;
		byte[] bytes = input.getBytes("GBK");
		if(bytes.length <= byteLen)  
		     return input;
		   for(int i = 0; i < byteLen; i++){
		     if(bytes[i] < 0)
		       counterOfDoubleByte++;
		   }
		   
		   if(counterOfDoubleByte % 2 == 0)
		     return new String(bytes, 0, byteLen, "GBK") + hint;
		   else 
		     return new String(bytes, 0, byteLen - 1, "GBK") + hint;
	}
	
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
