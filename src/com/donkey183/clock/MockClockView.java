package com.donkey183.clock;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.genius.col.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MockClockView extends View implements OnTouchListener {

	private static final String TAG = "MyClockView";

	private Bitmap mClockBitmap; // 时钟底盘
	private Bitmap mHourBitmap; // 时针
	private Bitmap mMinuteBitmap; // 分针
	private Bitmap mSecondBitmap; // 秒针
	private Bitmap mTmpBitmap;
	private Bitmap change;
	private int mDialWidth;
	private int mDialHeight;

	// 时钟的位置（相对于视图）
	private int clockX = 0, clockY = 0;

	// 时钟中心点位置（相对于视图）
	private int clockCenterX = 0, clockCenterY = 0;

	// 指针指向12点钟方向时指针向下的偏移量
	private int mHourOffsetY = 0, mMinuteOffsetY = 0;

	// 时针位置（相对于时钟中心点）
	private int mHourPosX = 0, mHourPosY = 0;

	// 分针位置（相对于时钟中心点）
	private int mMinutePosX = 0, mMinutePosY = 0;

	// 是否初始化完毕
	private boolean bInitComplete = false;

	// 时钟当前时间
	private MyTime mCurTime;

	private boolean fromEvent;

	public MockClockView(Context context) {
		this(context, null);
	}

	public MockClockView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MockClockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources r = getContext().getResources();
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.analogClock, defStyle, 0);
		mClockBitmap = ((BitmapDrawable) a
				.getDrawable(R.styleable.analogClock_dial)).getBitmap();
		mHourBitmap = ((BitmapDrawable) a
				.getDrawable(R.styleable.analogClock_hand_hour)).getBitmap();
		mMinuteBitmap = ((BitmapDrawable) a
				.getDrawable(R.styleable.analogClock_hand_minute)).getBitmap();
		mSecondBitmap = ((BitmapDrawable) a
				.getDrawable(R.styleable.analogClock_hand_second)).getBitmap();
		mTmpBitmap = ((BitmapDrawable) a
				.getDrawable(R.styleable.analogClock_hand_tmp)).getBitmap();
		change = mMinuteBitmap;
		a.recycle();
		BitmapDrawable bm = new BitmapDrawable(mClockBitmap);
		mDialWidth = bm.getIntrinsicWidth();
		mDialHeight = bm.getIntrinsicHeight();
		mCurTime = new MyTime();
		mCurTime.initBySystem();
		setOnTouchListener(this);
		setPointOffset();
		calcPointPosition();

		calcCenter();

		bInitComplete = true;
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				mCurTime.mSecondDegree += 6;
				mCurTime.mMinuteDegree = mCurTime.mMinuteDegree % 360 + 2;
				mCurTime.mHourDegree += 1 / 120;
				postInvalidate();
				handler.postDelayed(this, 1000);

			}

		};
		handler.postDelayed(runnable, 1000);// 打开定时器，执行操作
	}

	/**
	 * @param hourOffset
	 * @param minuteOffset
	 * 
	 *            设置指针12点钟方向时向下的偏移量
	 */
	public void setPointOffset() {
		mHourOffsetY = 20;
		mMinuteOffsetY = 20;
		calcPointPosition();
	}

	public void calcCenter() {
		if (mClockBitmap != null) {
			clockCenterX = clockX + mClockBitmap.getWidth() / 2;
			clockCenterY = clockY + mClockBitmap.getHeight() / 2;
		}
	}

	/**
	 * 计算指针位置
	 */
	public void calcPointPosition() {
		if (mHourBitmap != null) {
			int w = mHourBitmap.getWidth();
			int h = mHourBitmap.getHeight();

			mHourPosX = -w / 2;
			mHourPosY = -h + mHourOffsetY;

		}

		if (mMinuteBitmap != null) {
			int w = mMinuteBitmap.getWidth();
			int h = mMinuteBitmap.getHeight();

			mMinutePosX = -w / 2;
			mMinutePosY = -h + mMinuteOffsetY;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawColor(Color.WHITE);

		if (!bInitComplete) {
			return;
		}

		drawClock(canvas);

		drawHour(canvas);

		drawMinute(canvas);
		drawSecond(canvas);

	}


	public void drawClock(Canvas canvas) {
		if (mClockBitmap == null) {
			drawClockWithoutBitmap(canvas);
		} else {

			canvas.drawBitmap(mClockBitmap, clockX, clockY, null);
		}

	}

	private void drawClockWithoutBitmap(Canvas canvas) {
		Paint mPaint = new Paint();
		mPaint.setColor(Color.parseColor("#3399ff"));
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mPaint.setFakeBoldText(true);
		mPaint.setAntiAlias(true);
		canvas.drawCircle(clockCenterX, clockCenterY,110, mPaint);
		canvas.save();
		canvas.restore();

	}


	private void drawHour(Canvas canvas) {
		if (mHourBitmap == null) {
			return;
		}

		canvas.save();

		canvas.translate(clockCenterX, clockCenterY);

		canvas.rotate(mCurTime.mHourDegree);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		canvas.drawBitmap(mHourBitmap, mHourPosX, mHourPosY, paint);

		canvas.restore();
	}

	public void drawMinute(Canvas canvas) {
		if (mMinuteBitmap == null) {
			return;
		}

		canvas.save();

		canvas.translate(clockCenterX, clockCenterY);

		canvas.rotate(mCurTime.mMinuteDegree);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(mMinuteBitmap, mMinutePosX, mMinutePosY, paint);

		canvas.restore();
	}

	public void drawSecond(Canvas canvas) {
		if (mSecondBitmap == null) {
			return;
		}

		canvas.save();

		canvas.translate(clockCenterX, clockCenterY);

		canvas.rotate(mCurTime.mSecondDegree);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(mSecondBitmap, mMinutePosX, mMinutePosY, paint);

		canvas.restore();
	}


	class MyTime {

		int mHour = 0;
		int mMinute = 0;
		int mSecond = 0;

		int mHourDegree = 0; // 时针偏移量（相对于Y轴正半轴顺时针夹角，参考坐标系原点为时钟中心点，Y轴向上）
		int mMinuteDegree = 0; // 分针偏移量（相对于Y轴正半轴顺时针夹角，参考坐标系原点为时钟中心点，Y轴向上）
		int mSecondDegree = 0; // 分针偏移量（相对于Y轴正半轴顺时针夹角，参考坐标系原点为时钟中心点，Y轴向上）
		int mPreDegree = 0; // 上次分针偏移量

		private Calendar mCalendar;

		/**
		 * 根据系统时间更新相关变量
		 */
		public void initBySystem() {
			long time = System.currentTimeMillis();
			mCalendar = Calendar.getInstance();
			mCalendar.setTimeInMillis(time);

			mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
			mMinute = mCalendar.get(Calendar.MINUTE);
			mSecond = mCalendar.get(Calendar.SECOND);

			calcDegreeByTime();

		}

		/**
		 * 根据mHour，mMinute计算指针偏移量
		 */
		public void calcDegreeByTime() {
			mSecondDegree = mSecond * 6;
			mMinuteDegree = mMinute * 6 + mSecondDegree / 60;
			mPreDegree = mMinuteDegree;
			mHourDegree = (mHour % 12) * 30 + mMinuteDegree / 12;
		}

		/**
		 * @param bFlag
		 *            是否校正指针角度（ACTION_UP 时要校正）
		 * 
		 *            根据变化后的mMinuteDegree更新表示时间
		 */
		public void calcTime(boolean bFlag) {
			if (mMinuteDegree >= 360) {
				mMinuteDegree -= 360;
			}

			if (mMinuteDegree < 0) {
				mMinuteDegree += 360;
			}

			mMinute = (int) ((mMinuteDegree / 360.0) * 60);

			if (deasil()) {
				if (mMinuteDegree < mPreDegree) {
					mHour += 1;
					mHour %= 24;
				}
			} else {
				if (mMinuteDegree > mPreDegree) {
					mHour -= 1;
					if (mHour < 0) {
						mHour += 24;
					}
				}
			}

			mHourDegree = (mHour % 12) * 30 + mMinuteDegree / 12;

			mPreDegree = mMinuteDegree;

			Log.i(TAG, "mHourDegree = " + mHourDegree + ", mMinuteDegree = "
					+ mMinuteDegree);

			if (bFlag) {
				calcDegreeByTime();
			}

		}

		/**
		 * @return ACTION_MOVE时判断是否为顺时针旋转
		 */
		public boolean deasil() {
			if (mMinuteDegree >= mPreDegree) {
				if (mMinuteDegree - mPreDegree < 180) {
					return true;
				}
				return false;
			} else {
				if (mPreDegree - mMinuteDegree > 180) {
					return true;
				}

				return false;
			}
		}

	}

	/**
	 * 是否校正指针角度（ACTION_UP 时要校正）
	 * 
	 * 根据事件坐标更新表示时间
	 */
	public void calcDegree(int x, int y, boolean flag) {
		int rx = x - clockCenterX;
		int ry = -(y - clockCenterY);

		Point point = new Point(rx, ry);

		mCurTime.mMinuteDegree = DegreeUtil.GetRadianByPos(point);
		mCurTime.calcTime(flag);
	}

	@Override
	public boolean onTouch(View v, final MotionEvent event) {
		boolean isChecked = isChecked((int) event.getX(), (int) event.getY());
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (isChecked) {
				mMinuteBitmap = mTmpBitmap;
				calcDegree((int) event.getX(), (int) event.getY(), false);
				postInvalidate();
			}

			break;
		case MotionEvent.ACTION_MOVE:
			calcDegree((int) event.getX(), (int) event.getY(), false);
			postInvalidate();

			break;
		case MotionEvent.ACTION_UP:

			mMinuteBitmap = change;
			if (isChecked) {

				calcDegree((int) event.getX(), (int) event.getY(), true);
				postInvalidate();
			}

			break;
		}

		return true;
	}

	private boolean isChecked(int x, int y) {
		int rx = x - clockCenterX;
		int ry = -(y - clockCenterY);
		int minuteDegree = mCurTime.mMinuteDegree;
		int angle = DegreeUtil.GetRadianByPos(new Point(rx, ry));
		if (angle <= minuteDegree && angle + 10 >= minuteDegree
				|| angle >= minuteDegree && angle - 10 <= minuteDegree) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		// 根据提供的测量值(格式)提取大小值(这个大小也就是我们通常所说的大小)
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		// 高度与宽度类似
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		float hScale = 1.0f;// 缩放值
		float vScale = 1.0f;

		if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
			hScale = (float) widthSize / (float) mDialWidth;// 如果父元素提供的宽度比图片宽度小，就需要压缩一下子元素的宽度
		}

		if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
			vScale = (float) heightSize / (float) mDialHeight;// 同上
		}

		float scale = Math.min(hScale, vScale);// 取最小的压缩值，值越小，压缩越厉害
		// 最后保存一下，这个函数一定要调用
		setMeasuredDimension(
				resolveSizeAndState((int) (mDialWidth * scale),
						widthMeasureSpec, 0),
				resolveSizeAndState((int) (mDialHeight * scale),
						heightMeasureSpec, 0));
	}

	public Date getTimezoneDate(Date date, String dstTimeZoneId) {

		if (date == null || "".equals(date))
			return null;

		if (dstTimeZoneId == null || "".equals(dstTimeZoneId))
			return null;
		try {
			int diffTime = getDiffTimeZoneRawOffset(dstTimeZoneId);
			Date d = date;
			long nowTime = d.getTime();
			long newNowTime = nowTime - diffTime;
			d = new Date(newNowTime);
			return d;
		} catch (Exception e) {
			return null;
		}
	}

	private static int getDiffTimeZoneRawOffset(String timeZoneId) {
		return TimeZone.getDefault().getRawOffset()
				- TimeZone.getTimeZone(timeZoneId).getRawOffset();
	}

	public void setTime(Date date, String dstTimeZoneId) {
		Date d = getTimezoneDate(date, dstTimeZoneId);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		mCurTime.mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mCurTime.mMinute = calendar.get(Calendar.MINUTE);
		mCurTime.mSecond = calendar.get(Calendar.SECOND);
		mCurTime.calcDegreeByTime();
		postInvalidate();
	}

}
