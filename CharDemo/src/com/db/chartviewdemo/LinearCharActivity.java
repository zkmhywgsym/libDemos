package com.db.chartviewdemo;

import java.text.DecimalFormat;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;
import com.db.chart.view.animation.style.DashAnimation;
import com.db.williamchartdemo.R;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

@SuppressLint("NewApi")
public class LinearCharActivity extends Activity {
	private static LineChartView mLineChart;
	private TextView mLineTooltip;
	private Paint mLineGridPaint;
	private final static int LINE_MAX = 10;
	private final static int LINE_MIN = -10;
	private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
	private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();
	private final static float[][] lineValues = { {-5f, 6f, 2f, 9f, 0f, 1f, 5f},
		{-9f, -2f, -4f, -3f, -7f, -5f, -3f}};
	private final static String[] lineLabels = {"", "ANT", "GNU", "OWL", "APE", "JAY", ""};
	private static float mCurrOverlapFactor;
	private static int[] mCurrOverlapOrder;
	private static float mOldOverlapFactor;
	private static int[] mOldOverlapOrder;
	private static BaseEasingMethod mCurrEasing;
	private static BaseEasingMethod mOldEasing;
	private static float mCurrStartX;
	private static float mCurrStartY;
	private static float mOldStartX;
	private static float mOldStartY;
	private static int mCurrAlpha;
	private static int mOldAlpha;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_linear_char_layout);
		initLineChart();
		initValue();
		updateLineChart();
	}

	private void initLineChart() {

		mLineChart = (LineChartView) findViewById(R.id.linechart);
		mLineChart.setOnClickListener(lineClickListener);
		mLineChart.setOnEntryClickListener(lineEntryListener);

		mLineGridPaint = new Paint();
		mLineGridPaint
				.setColor(this.getResources().getColor(R.color.line_grid));
		mLineGridPaint
				.setPathEffect(new DashPathEffect(new float[] { 5, 5 }, 0));
		mLineGridPaint.setStyle(Paint.Style.STROKE);
		mLineGridPaint.setAntiAlias(true);
		mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
	}
	private void initValue(){
		mCurrOverlapFactor = 1;
		mCurrEasing = new QuintEaseOut();
		mCurrStartX = -1;
		mCurrStartY = 0;	
		mCurrAlpha = -1;
		
		mOldOverlapFactor = 1;
		mOldEasing = new QuintEaseOut();
		mOldStartX = -1;
		mOldStartY = 0;	
		mOldAlpha = -1;
	}

	private final OnClickListener lineClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mLineTooltip != null)
				dismissLineTooltip(-1, -1, null);
		}
	};
	private final OnEntryClickListener lineEntryListener = new OnEntryClickListener(){
		@Override
		public void onClick(int setIndex, int entryIndex, Rect rect) {

			if(mLineTooltip == null)
				showLineTooltip(setIndex, entryIndex, rect);
			else
				dismissLineTooltip(setIndex, entryIndex, rect);
		}
	};

	private void dismissLineTooltip(final int setIndex, final int entryIndex,
			final Rect rect) {

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mLineTooltip.animate().setDuration(100).scaleX(0).scaleY(0)
					.alpha(0).setInterpolator(exitInterpolator)
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							mLineChart.removeView(mLineTooltip);
							mLineTooltip = null;
							if (entryIndex != -1)
								showLineTooltip(setIndex, entryIndex, rect);
						}
					});
		} else {
			mLineChart.dismissTooltip(mLineTooltip);
			mLineTooltip = null;
			if (entryIndex != -1)
				showLineTooltip(setIndex, entryIndex, rect);
		}
	}

	private void showLineTooltip(int setIndex, int entryIndex, Rect rect) {

		mLineTooltip = (TextView) getLayoutInflater().inflate(
				R.layout.circular_tooltip, null);
		mLineTooltip.setText(Integer
				.toString((int) lineValues[setIndex][entryIndex]));

		LayoutParams layoutParams = new LayoutParams(
				(int) Tools.fromDpToPx(35), (int) Tools.fromDpToPx(35));
		layoutParams.leftMargin = rect.centerX() - layoutParams.width / 2;
		layoutParams.topMargin = rect.centerY() - layoutParams.height / 2;
		mLineTooltip.setLayoutParams(layoutParams);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			mLineTooltip.setPivotX(layoutParams.width / 2);
			mLineTooltip.setPivotY(layoutParams.height / 2);
			mLineTooltip.setAlpha(0);
			mLineTooltip.setScaleX(0);
			mLineTooltip.setScaleY(0);
			mLineTooltip.animate().setDuration(150).alpha(1).scaleX(1)
					.scaleY(1).rotation(360).setInterpolator(enterInterpolator);
		}

		mLineChart.showTooltip(mLineTooltip);
	}
private void updateLineChart(){
		
		mLineChart.reset();
		
		LineSet dataSet = new LineSet();
		dataSet.addPoints(lineLabels, lineValues[0]);
		dataSet.setDots(true)
			.setDotsColor(this.getResources().getColor(R.color.line_bg))
			.setDotsRadius(Tools.fromDpToPx(5))
			.setDotsStrokeThickness(Tools.fromDpToPx(2))
			.setDotsStrokeColor(this.getResources().getColor(R.color.line))
			.setLineColor(this.getResources().getColor(R.color.line))
			.setLineThickness(Tools.fromDpToPx(3))
			.beginAt(1).endAt(lineLabels.length - 1);
		mLineChart.addData(dataSet);
		
		dataSet = new LineSet();
		dataSet.addPoints(lineLabels, lineValues[1]);
		dataSet.setLineColor(this.getResources().getColor(R.color.line))
			.setLineThickness(Tools.fromDpToPx(3))
			.setSmooth(true)
			.setDashed(true);
		mLineChart.addData(dataSet);
		
		mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
			.setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
			.setXAxis(false)
			.setXLabels(XController.LabelPosition.OUTSIDE)
			.setYAxis(false)
			.setYLabels(YController.LabelPosition.OUTSIDE)
			.setAxisBorderValues(LINE_MIN, LINE_MAX, 5)
			.setLabelsFormat(new DecimalFormat("##'u'"))
			.show(getAnimation(true))
			//.show()
			;
		
		mLineChart.animateSet(1, new DashAnimation());
	}
//获取动画
	private Animation getAnimation(boolean newAnim){
		if(newAnim)
			return new Animation()
					.setAlpha(mCurrAlpha)
					.setEasing(mCurrEasing)
					.setOverlap(mCurrOverlapFactor, mCurrOverlapOrder)
					.setStartPoint(mCurrStartX, mCurrStartY);
		else
			return new Animation()
					.setAlpha(mOldAlpha)
					.setEasing(mOldEasing)
					.setOverlap(mOldOverlapFactor, mOldOverlapOrder)
					.setStartPoint(mOldStartX, mOldStartY);
	}
}
