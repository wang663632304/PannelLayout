/**
 * Copyright 2013 C. A. Fitzgerald
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.riotopsys.pannellayout;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class PannelLayout extends ViewGroup {

	private static final String TAG = PannelLayout.class.getSimpleName();

	private int columns = 1;
	private float rowRatio = .5f;
	private int dividerSize = dp(4);

	private int maxRow = 0;

	public PannelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public PannelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public PannelLayout(Context context) {
		super(context);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PannelLayout);
		columns = a.getInteger(R.styleable.PannelLayout_columns, 1);

		rowRatio = a.getFloat(R.styleable.PannelLayout_row_ratio, .5f);

		dividerSize = a.getDimensionPixelOffset(
				R.styleable.PannelLayout_divider_size, dp(4));

		a.recycle();
	}

	private int dp(int dpValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dpValue, getResources().getDisplayMetrics());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i(TAG, "onMeasure");
		arrangeChildren();

		int width = MeasureSpec.getSize(widthMeasureSpec) - ( getPaddingLeft() + getPaddingRight());
		int height = MeasureSpec.getSize(heightMeasureSpec)  - ( getPaddingTop() + getPaddingBottom());

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int pannelWidthUnit, pannelHeightUnit;
		
		pannelWidthUnit = (width - dividerSize * (columns - 1)) / columns;
		if (heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY){
			pannelHeightUnit = (height - dividerSize * (maxRow)) / (maxRow+1);
		} else {

			pannelHeightUnit = (int) (pannelWidthUnit * rowRatio);
			if (heightMode == MeasureSpec.EXACTLY) {
				// force rows to fit
				pannelHeightUnit = (height - dividerSize * (maxRow)) / (maxRow+1);
			}
			if (heightMode == MeasureSpec.AT_MOST) {
				// force rows to fit
				pannelHeightUnit = Math.min(
						(height - dividerSize * (maxRow)) / (maxRow+1), pannelHeightUnit);
			}
			pannelWidthUnit = (int) (pannelHeightUnit / rowRatio);

		}
		
		pannelWidthUnit = Math.max(pannelWidthUnit, 0); 
		pannelHeightUnit = Math.max(pannelHeightUnit, 0); 
		
		width = pannelWidthUnit * columns + dividerSize * (columns - 1);

		for (int c = 0; c < getChildCount(); c++) {
			View child = getChildAt(c);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			int pannelWidth = pannelWidthUnit * (lp.columnSpan + 1)
					+ lp.columnSpan * dividerSize;
			int pannelHeight = pannelHeightUnit * (lp.rowSpan + 1) + lp.rowSpan
					* dividerSize;

			child.measure(
					MeasureSpec.makeMeasureSpec(pannelWidth, MeasureSpec.EXACTLY), 
					MeasureSpec.makeMeasureSpec(pannelHeight, MeasureSpec.EXACTLY));

		}

		setMeasuredDimension(
				resolveSize(width + getPaddingLeft() + getPaddingRight(), widthMeasureSpec),
				resolveSize((maxRow + 1) * pannelHeightUnit + (maxRow) * dividerSize + getPaddingBottom() + getPaddingTop(), heightMeasureSpec));

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.i(TAG, "onLayout");
		int width = right - left - ( getPaddingLeft() + getPaddingRight());
		int height = bottom - top - ( getPaddingTop() + getPaddingBottom());

		int pannelWidthUnit  = (width  - dividerSize * (columns - 1)) / columns;
		int pannelHeightUnit = (height - dividerSize * (maxRow)) / (maxRow+1);

		for (int c = 0; c < getChildCount(); c++) {
			View child = getChildAt(c);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int childLeft = lp.column * (pannelWidthUnit + dividerSize) + getPaddingLeft();
			int childTop = lp.row * (pannelHeightUnit + dividerSize) + getPaddingTop();
			child.layout(childLeft, childTop,
					childLeft + child.getMeasuredWidth(),
					childTop + child.getMeasuredHeight());

		}

	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	private void arrangeChildren() {
		Log.i(TAG, "arrangeChildren");
		FillTracker tracker = new FillTracker(columns);
		maxRow = 0;
		for (int c = 0; c < getChildCount(); c++) {
			boolean done = false;
			int column, row;
			column = row = 0;

			View child = getChildAt(c);
			
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			
			if ( child.getVisibility() == View.GONE){
				lp.column = -1;
				lp.row = -1;
				continue;
			}
			
			if ( lp.columnSpan >= columns ){
				lp.columnSpan = columns -1;
			}

			while (!done) {
				// jump to next row
				if (column >= columns) {
					row++;
					column = 0;
				}

				boolean free = true;
				// at this row & column will the child fit?
				for (int a = 0; a <= lp.columnSpan; a++) {
					for (int b = 0; b <= lp.rowSpan; b++) {
						free = free && tracker.isFree(column + a, row + b);
					}
				}
				if (free) {
					// claim positions for this child
					for (int a = 0; a <= lp.columnSpan; a++) {
						for (int b = 0; b <= lp.rowSpan; b++) {
							tracker.claim(column + a, row + b);
						}
					}
					Log.i(TAG, String.format("child %d @ col %d, row %d", c,
							column, row));
					lp.column = column;
					lp.row = row;
					done = true;
				}

				// jump to next column
				column++;
			}
			maxRow = Math.max(maxRow, lp.row + lp.rowSpan);
		}
	}

	private static class FillTracker {

		private static class Address {
			public int column, row;

			public Address(int column, int row) {
				this.column = column;
				this.row = row;
			}

			@Override
			public boolean equals(Object o) {
				if (!(o instanceof Address)) {
					return false;
				}
				return ((Address) o).column == column
						&& ((Address) o).row == row;
			}

			@Override
			public int hashCode() {
				return (column * 33) ^ row;
			}
		}

		private int columns;

		private Set<Address> filledAddresses = new HashSet<Address>();

		public FillTracker(int columns) {
			this.columns = columns;
		}

		public boolean isFree(int column, int row) {
			if (column >= columns) {
				return false;
			}
			return !filledAddresses.contains(new Address(column, row));
		}

		public void claim(int column, int row) {
			filledAddresses.add(new Address(column, row));
		}

	}

	public static class LayoutParams extends ViewGroup.LayoutParams {

		public int row;
		public int column;

		private int columnSpan = 0;
		private int rowSpan = 0;

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(int width, int height, int columnSpan, int rowSpan) {
			super(width, height);
			this.columnSpan = columnSpan;
			this.rowSpan = rowSpan;
		}

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			TypedArray a = c.obtainStyledAttributes(attrs,
					R.styleable.PannelLayout_Layout);
			columnSpan = a.getInteger(
					R.styleable.PannelLayout_Layout_layout_columnSpan, 0);
			rowSpan = a.getInteger(R.styleable.PannelLayout_Layout_layout_rowSpan, 0);
			a.recycle();
		}

		/**
		 * {@inheritDoc}
		 */
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

	}

}
