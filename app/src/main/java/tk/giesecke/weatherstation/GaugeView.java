/*******************************************************************************
 * Copyright (c) 2012 Evelina Vrabie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package tk.giesecke.weatherstation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {

	private static final int SIZE = 300;
	private static final float TOP = 0.0f;
	private static final float LEFT = 0.0f;
	private static final float RIGHT = 1.0f;
	private static final float BOTTOM = 1.0f;
	private static final float CENTER = 0.5f;
	private static final boolean SHOW_OUTER_SHADOW = true;
	private static final boolean SHOW_OUTER_BORDER = true;
	private static final boolean SHOW_OUTER_RIM = true;
	private static final boolean SHOW_INNER_RIM = true;
	private static final boolean SHOW_NEEDLE = true;
	private static final boolean SHOW_SCALE = false;
	private static final boolean SHOW_RANGES = true;

	private static final float OUTER_SHADOW_WIDTH = 0.03f;
	private static final float OUTER_BORDER_WIDTH = 0.04f;
	private static final float OUTER_RIM_WIDTH = 0.04f;
	private static final float INNER_RIM_WIDTH = 0.05f;
	private static final float INNER_RIM_BORDER_WIDTH = 0.005f;

	private static final float NEEDLE_WIDTH = 0.035f;
	private static final float NEEDLE_HEIGHT = 0.28f;

	private static final float SCALE_START_VALUE = 0.0f;
	private static final float SCALE_END_VALUE = 100.0f;
	private static final float SCALE_START_ANGLE = 30.0f;
	private static final int SCALE_DIVISIONS = 10;
	private static final int SCALE_SUBDIVISIONS = 5;

	private static final int[] OUTER_SHADOW_COLORS = {Color.argb(40, 255, 254, 187), Color.argb(20, 255, 247, 219),
			Color.argb(5, 255, 255, 255)};
	private static final float[] OUTER_SHADOW_POS = {0.90f, 0.95f, 0.99f};

	private static final int FACE_IMAGE_ID = R.drawable.thermometer;

	// *--------------------------------------------------------------------- *//
	// Customizable properties
	// *--------------------------------------------------------------------- *//

	private boolean mShowOuterShadow;
	private boolean mShowOuterBorder;
	private boolean mShowOuterRim;
	private boolean mShowInnerRim;
	private boolean mShowScale;
	private boolean mShowRanges;
	private boolean mShowNeedle;

	private float mOuterShadowWidth;
	private float mOuterBorderWidth;
	private float mOuterRimWidth;
	private float mInnerRimWidth;
	private float mInnerRimBorderWidth;
	private float mNeedleWidth;
	private float mNeedleHeight;

	private float mScaleStartValue;
	private float mScaleEndValue;
	private float mScaleStartAngle;
	private float mScaleEndAngle;

	private int mDivisions;
	private int mSubdivisions;

	private RectF mOuterShadowRect;
	private RectF mOuterBorderRect;
	private RectF mOuterRimRect;
	private RectF mInnerRimRect;
	private RectF mInnerRimBorderRect;
	private RectF mFaceRect;

	private Bitmap mBackground;
	private Paint mBackgroundPaint;
	private Paint mOuterShadowPaint;
	private Paint mOuterBorderPaint;
	private Paint mOuterRimPaint;
	private Paint mInnerRimPaint;
	private Paint mInnerRimBorderLightPaint;
	private Paint mInnerRimBorderDarkPaint;
	private Paint mFacePaint;
	private Paint mNeedleRightPaint;
	private Paint mNeedleLeftPaint;
	private Paint mNeedleScrewPaint;
	private Paint mNeedleScrewBorderPaint;

	private Path mNeedleRightPath;
	private Path mNeedleLeftPath;

	// *--------------------------------------------------------------------- *//

	// *--------------------------------------------------------------------- *//
	// BeeGee additional properties
	// *--------------------------------------------------------------------- *//

	private int faceImageID;

	// *--------------------------------------------------------------------- *//

	private float mScaleRotation;
	private float mSubdivisionValue;
	private float mSubdivisionAngle;

	private float mTargetValue;
	private float mCurrentValue;

	private float mNeedleVelocity;
	private float mNeedleAcceleration;
	private long mNeedleLastMoved = -1;
	private boolean mNeedleInitialized;

	public GaugeView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		readAttrs(context, attrs, defStyle);
		init();
	}

	public GaugeView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GaugeView(final Context context) {
		this(context, null, 0);
	}

	private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
		mShowOuterShadow = a.getBoolean(R.styleable.GaugeView_showOuterShadow, SHOW_OUTER_SHADOW);
		mShowOuterBorder = a.getBoolean(R.styleable.GaugeView_showOuterBorder, SHOW_OUTER_BORDER);
		mShowOuterRim = a.getBoolean(R.styleable.GaugeView_showOuterRim, SHOW_OUTER_RIM);
		mShowInnerRim = a.getBoolean(R.styleable.GaugeView_showInnerRim, SHOW_INNER_RIM);
		mShowNeedle = a.getBoolean(R.styleable.GaugeView_showNeedle, SHOW_NEEDLE);
		mShowScale = a.getBoolean(R.styleable.GaugeView_showScale, SHOW_SCALE);
		mShowRanges = a.getBoolean(R.styleable.GaugeView_showRanges, SHOW_RANGES);

		mOuterShadowWidth = mShowOuterShadow ? a.getFloat(R.styleable.GaugeView_outerShadowWidth, OUTER_SHADOW_WIDTH) : 0.0f;
		mOuterBorderWidth = mShowOuterBorder ? a.getFloat(R.styleable.GaugeView_outerBorderWidth, OUTER_BORDER_WIDTH) : 0.0f;
		mOuterRimWidth = mShowOuterRim ? a.getFloat(R.styleable.GaugeView_outerRimWidth, OUTER_RIM_WIDTH) : 0.0f;
		mInnerRimWidth = mShowInnerRim ? a.getFloat(R.styleable.GaugeView_innerRimWidth, INNER_RIM_WIDTH) : 0.0f;
		mInnerRimBorderWidth = mShowInnerRim ? a.getFloat(R.styleable.GaugeView_innerRimBorderWidth, INNER_RIM_BORDER_WIDTH) : 0.0f;

		mNeedleWidth = a.getFloat(R.styleable.GaugeView_needleWidth, NEEDLE_WIDTH);
		mNeedleHeight = a.getFloat(R.styleable.GaugeView_needleHeight, NEEDLE_HEIGHT);

		mScaleStartValue = a.getFloat(R.styleable.GaugeView_scaleStartValue, SCALE_START_VALUE);
		mScaleEndValue = a.getFloat(R.styleable.GaugeView_scaleEndValue, SCALE_END_VALUE);
		mScaleStartAngle = a.getFloat(R.styleable.GaugeView_scaleStartAngle, SCALE_START_ANGLE);
		mScaleEndAngle = a.getFloat(R.styleable.GaugeView_scaleEndAngle, 360.0f - mScaleStartAngle);

		mDivisions = a.getInteger(R.styleable.GaugeView_divisions, SCALE_DIVISIONS);
		mSubdivisions = a.getInteger(R.styleable.GaugeView_subdivisions, SCALE_SUBDIVISIONS);

		faceImageID = a.getResourceId(R.styleable.GaugeView_faceImageID, FACE_IMAGE_ID);

		a.recycle();
	}

	@TargetApi(11)
	private void init() {

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		initDrawingRects();
		initDrawingTools();

		// Compute the scale properties
		//if (mShowRanges) {
			initScale();
		//}
	}

	private void initDrawingRects() {
		// The drawing area is a rectangle of width 1 and height 1,
		// where (0,0) is the top left corner of the canvas.
		// Note that on Canvas X axis points to right, while the Y axis points downwards.
		mOuterShadowRect = new RectF(LEFT, TOP, RIGHT, BOTTOM);

		mOuterBorderRect = new RectF(mOuterShadowRect.left + mOuterShadowWidth, mOuterShadowRect.top + mOuterShadowWidth,
				mOuterShadowRect.right - mOuterShadowWidth, mOuterShadowRect.bottom - mOuterShadowWidth);

		mOuterRimRect = new RectF(mOuterBorderRect.left + mOuterBorderWidth, mOuterBorderRect.top + mOuterBorderWidth,
				mOuterBorderRect.right - mOuterBorderWidth, mOuterBorderRect.bottom - mOuterBorderWidth);

		mInnerRimRect = new RectF(mOuterRimRect.left + mOuterRimWidth, mOuterRimRect.top + mOuterRimWidth, mOuterRimRect.right
				- mOuterRimWidth, mOuterRimRect.bottom - mOuterRimWidth);

		mInnerRimBorderRect = new RectF(mInnerRimRect.left + mInnerRimBorderWidth, mInnerRimRect.top + mInnerRimBorderWidth,
				mInnerRimRect.right - mInnerRimBorderWidth, mInnerRimRect.bottom - mInnerRimBorderWidth);

		mFaceRect = new RectF(mInnerRimRect.left + mInnerRimWidth, mInnerRimRect.top + mInnerRimWidth,
				mInnerRimRect.right - mInnerRimWidth, mInnerRimRect.bottom - mInnerRimWidth);

	}

	private void initDrawingTools() {
		mBackgroundPaint = new Paint();
		mBackgroundPaint.setFilterBitmap(true);

		if (mShowOuterShadow) {
			mOuterShadowPaint = getDefaultOuterShadowPaint();
		}
		if (mShowOuterBorder) {
			mOuterBorderPaint = getDefaultOuterBorderPaint();
		}
		if (mShowOuterRim) {
			mOuterRimPaint = getDefaultOuterRimPaint();
		}
		if (mShowInnerRim) {
			mInnerRimPaint = getDefaultInnerRimPaint();
			mInnerRimBorderLightPaint = getDefaultInnerRimBorderLightPaint();
			mInnerRimBorderDarkPaint = getDefaultInnerRimBorderDarkPaint();
		}
		if (mShowNeedle) {
			setDefaultNeedlePaths();
			mNeedleLeftPaint = getDefaultNeedleLeftPaint();
			mNeedleRightPaint = getDefaultNeedleRightPaint();
			mNeedleScrewPaint = getDefaultNeedleScrewPaint();
			mNeedleScrewBorderPaint = getDefaultNeedleScrewBorderPaint();
		}
		mFacePaint = getDefaultFacePaint();
	}

	private Paint getDefaultOuterShadowPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setShader(new RadialGradient(CENTER, CENTER, mOuterShadowRect.width() / 2.0f, OUTER_SHADOW_COLORS, OUTER_SHADOW_POS,
				TileMode.MIRROR));
		return paint;
	}

	private Paint getDefaultOuterBorderPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.argb(245, 0, 0, 0));
		return paint;
	}

	private Paint getDefaultOuterRimPaint() {
		// Use a linear gradient to create the 3D effect
		final LinearGradient verticalGradient = new LinearGradient(mOuterRimRect.left, mOuterRimRect.top, mOuterRimRect.left,
				mOuterRimRect.bottom, Color.rgb(255, 255, 255), Color.rgb(84, 90, 100), TileMode.REPEAT);

		// Use a Bitmap shader for the metallic style
		final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.darkwood);
		final BitmapShader outerRimTile = new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT);
		final Matrix matrix = new Matrix();
		matrix.setScale(1.0f / bitmap.getWidth(), 1.0f / bitmap.getHeight());
		outerRimTile.setLocalMatrix(matrix);

		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setShader(new ComposeShader(verticalGradient, outerRimTile, PorterDuff.Mode.MULTIPLY));
		paint.setFilterBitmap(true);
		return paint;
	}

	private Paint getDefaultInnerRimPaint() {
		// Use a linear gradient to create the 3D effect
		final LinearGradient verticalGradient = new LinearGradient(mOuterRimRect.left, mOuterRimRect.top, mOuterRimRect.left,
				mOuterRimRect.bottom, Color.rgb(255, 255, 255), Color.rgb(84, 90, 100), TileMode.REPEAT);

		// Use a Bitmap shader for the metallic style
		final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.darkerwood);
		final BitmapShader innerRimTile = new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT);
		final Matrix matrix = new Matrix();
		matrix.setScale(1.0f / bitmap.getWidth(), 1.0f / bitmap.getHeight());
		innerRimTile.setLocalMatrix(matrix);

		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setShader(new ComposeShader(verticalGradient, innerRimTile, PorterDuff.Mode.MULTIPLY));
		paint.setFilterBitmap(true);
		return paint;
	}

	private Paint getDefaultInnerRimBorderLightPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.argb(100, 255, 255, 255));
		paint.setColor(getResources().getColor(R.color.my_gold));
		paint.setStrokeWidth(0.005f);
		return paint;
	}

	private Paint getDefaultInnerRimBorderDarkPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.argb(100, 81, 84, 89));
		paint.setColor(getResources().getColor(R.color.my_gold_brown));
		paint.setStrokeWidth(0.005f);
		return paint;
	}

	private Paint getDefaultFacePaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		Bitmap faceTexture = BitmapFactory.decodeResource(getContext().getResources(), faceImageID);
		BitmapShader paperShader = new BitmapShader(faceTexture,
				Shader.TileMode.CLAMP,
				Shader.TileMode.CLAMP);
		Matrix paperMatrix = new Matrix();
		paperMatrix.setScale(1.0f / faceTexture.getWidth(),
				1.0f / faceTexture.getHeight());

		paperShader.setLocalMatrix(paperMatrix);
		paint.setShader(paperShader);
		return paint;

	}

	private void setDefaultNeedlePaths() {
		final float x = 0.5f, y = 0.5f;
		mNeedleLeftPath = new Path();
		mNeedleLeftPath.moveTo(x, y);
		mNeedleLeftPath.lineTo(x - mNeedleWidth, y);
		mNeedleLeftPath.lineTo(x, y - mNeedleHeight);
		mNeedleLeftPath.lineTo(x, y);
		mNeedleLeftPath.lineTo(x - mNeedleWidth, y);

		mNeedleRightPath = new Path();
		mNeedleRightPath.moveTo(x, y);
		mNeedleRightPath.lineTo(x + mNeedleWidth, y);
		mNeedleRightPath.lineTo(x, y - mNeedleHeight);
		mNeedleRightPath.lineTo(x, y);
		mNeedleRightPath.lineTo(x + mNeedleWidth, y);
	}

	private Paint getDefaultNeedleLeftPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.rgb(176, 10, 19));
		paint.setColor(getResources().getColor(R.color.my_gold));
		return paint;
	}

	private Paint getDefaultNeedleRightPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.rgb(252, 18, 30));
		paint.setColor(getResources().getColor(R.color.my_gold_brown));
		return paint;
	}

	private Paint getDefaultNeedleScrewPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setShader(new RadialGradient(0.5f, 0.5f, 0.07f,
				new int[]{Color.rgb(171, 171, 171), Color.WHITE}, new float[]{0.05f,
				0.9f}, TileMode.MIRROR));
		return paint;
	}

	private Paint getDefaultNeedleScrewBorderPaint() {
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.argb(100, 81, 84, 89));
		paint.setColor(getResources().getColor(R.color.my_gold_brown));
		paint.setStrokeWidth(0.005f);
		return paint;
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		final Bundle bundle = (Bundle) state;
		final Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);

		mNeedleInitialized = bundle.getBoolean("needleInitialized");
		mNeedleVelocity = bundle.getFloat("needleVelocity");
		mNeedleAcceleration = bundle.getFloat("needleAcceleration");
		mNeedleLastMoved = bundle.getLong("needleLastMoved");
		mCurrentValue = bundle.getFloat("currentValue");
		mTargetValue = bundle.getFloat("targetValue");
	}

	private void initScale() {
		mScaleRotation = (mScaleStartAngle + 180) % 360;
		float mDivisionValue = (mScaleEndValue - mScaleStartValue) / mDivisions;
		mSubdivisionValue = mDivisionValue / mSubdivisions;
		mSubdivisionAngle = (mScaleEndAngle - mScaleStartAngle) / (mDivisions * mSubdivisions);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();

		final Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putBoolean("needleInitialized", mNeedleInitialized);
		state.putFloat("needleVelocity", mNeedleVelocity);
		state.putFloat("needleAcceleration", mNeedleAcceleration);
		state.putLong("needleLastMoved", mNeedleLastMoved);
		state.putFloat("currentValue", mCurrentValue);
		state.putFloat("targetValue", mTargetValue);
		return state;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		// Loggable.log.debug(String.format("widthMeasureSpec=%s, heightMeasureSpec=%s",
		// View.MeasureSpec.toString(widthMeasureSpec),
		// View.MeasureSpec.toString(heightMeasureSpec)));

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		final int chosenWidth = chooseDimension(widthMode, widthSize);
		final int chosenHeight = chooseDimension(heightMode, heightSize);
		setMeasuredDimension(chosenWidth, chosenHeight);
	}

	private int chooseDimension(final int mode, final int size) {
		switch (mode) {
			case View.MeasureSpec.AT_MOST:
			case View.MeasureSpec.EXACTLY:
				return size;
			case View.MeasureSpec.UNSPECIFIED:
			default:
				return SIZE;
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		drawGauge();
	}

	private void drawGauge() {
		if (null != mBackground) {
			// Let go of the old background
			mBackground.recycle();
		}
		// Create a new background according to the new width and height
		mBackground = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(mBackground);
		final float scale = Math.min(getWidth(), getHeight());
		canvas.scale(scale, scale);
		canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
				, (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

		drawRim(canvas);
		drawFace(canvas);

		//if (mShowRanges) {
			//drawScale(canvas);
		//}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		drawBackground(canvas);

		final float scale = Math.min(getWidth(), getHeight());
		canvas.scale(scale, scale);
		canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
				, (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

		if (mShowNeedle) {
			drawNeedle(canvas);
		}

/*		if (mShowText) {
			drawText(canvas);
		}
*/
		computeCurrentValue();
	}

	private void drawBackground(final Canvas canvas) {
		if (null != mBackground) {
			canvas.drawBitmap(mBackground, 0, 0, mBackgroundPaint);
		}
	}

	private void drawRim(final Canvas canvas) {
		if (mShowOuterShadow) {
			canvas.drawOval(mOuterShadowRect, mOuterShadowPaint);
		}
		if (mShowOuterBorder) {
			canvas.drawOval(mOuterBorderRect, mOuterBorderPaint);
		}
		if (mShowOuterRim) {
			canvas.drawOval(mOuterRimRect, mOuterRimPaint);
		}
		if (mShowInnerRim) {
			canvas.drawOval(mInnerRimRect, mInnerRimPaint);
			canvas.drawOval(mInnerRimRect, mInnerRimBorderLightPaint);
			canvas.drawOval(mInnerRimBorderRect, mInnerRimBorderDarkPaint);
		}
	}

	private void drawFace(final Canvas canvas) {

		// Draw the face gradient
		canvas.drawOval(mFaceRect, mFacePaint);
		// Draw the face border
		//canvas.drawOval(mScaleImageRect, mFaceBorderPaint);
		// Draw the inner face shadow
		//canvas.drawOval(mFaceRect, mFaceShadowPaint);
	}

	private void drawNeedle(final Canvas canvas) {
		if (mNeedleInitialized) {
			final float angle = getAngleForValue(mCurrentValue);
			// Logger.log.info(String.format("value=%f -> angle=%f", mCurrentValue, angle));

			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(angle, 0.5f, 0.5f);

			canvas.drawPath(mNeedleLeftPath, mNeedleLeftPaint);
			canvas.drawPath(mNeedleRightPath, mNeedleRightPaint);

			canvas.restore();

			// Draw the needle screw and its border
			canvas.drawCircle(0.5f, 0.5f, 0.04f, mNeedleScrewPaint);
			canvas.drawCircle(0.5f, 0.5f, 0.04f, mNeedleScrewBorderPaint);
		}
	}

	private float getAngleForValue(final float value) {
		return (mScaleRotation + ((value - mScaleStartValue) / mSubdivisionValue) * mSubdivisionAngle) % 360;
	}

	private void computeCurrentValue() {
		// Logger.log.warn(String.format("velocity=%f, acceleration=%f", mNeedleVelocity,
		// mNeedleAcceleration));

		if (!(Math.abs(mCurrentValue - mTargetValue) > 0.01f)) {
			return;
		}

		if (-1 != mNeedleLastMoved) {
			final float time = (System.currentTimeMillis() - mNeedleLastMoved) / 1000.0f;
			final float direction = Math.signum(mNeedleVelocity);
			if (Math.abs(mNeedleVelocity) < 90.0f) {
				mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
			} else {
				mNeedleAcceleration = 0.0f;
			}

			mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
			mCurrentValue += mNeedleVelocity * time;
			mNeedleVelocity += mNeedleAcceleration * time;

			if ((mTargetValue - mCurrentValue) * direction < 0.01f * direction) {
				mCurrentValue = mTargetValue;
				mNeedleVelocity = 0.0f;
				mNeedleAcceleration = 0.0f;
				mNeedleLastMoved = -1L;
			} else {
				mNeedleLastMoved = System.currentTimeMillis();
			}

			invalidate();

		} else {
			mNeedleLastMoved = System.currentTimeMillis();
			computeCurrentValue();
		}
	}

	public void setTargetValue(final float value) {
		if (mShowScale || mShowRanges) {
			if (value < mScaleStartValue) {
				mTargetValue = mScaleStartValue;
			} else if (value > mScaleEndValue) {
				mTargetValue = mScaleEndValue;
			} else {
				mTargetValue = value;
			}
		} else {
			mTargetValue = value;
		}
		mNeedleInitialized = true;
		invalidate();
	}
}
