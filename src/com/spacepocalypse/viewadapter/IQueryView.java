package com.spacepocalypse.viewadapter;

import android.widget.BaseAdapter;

public interface IQueryView<D, A extends BaseAdapter> {
	public void setData(D data);
	public A getAdapter();
}
