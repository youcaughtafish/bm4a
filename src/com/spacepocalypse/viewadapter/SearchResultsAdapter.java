package com.spacepocalypse.viewadapter;

import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedBeer;

public class SearchResultsAdapter extends BaseAdapter {
	private List<MappedBeer> searchArrayList;
	private LayoutInflater layoutInflater;

	public SearchResultsAdapter(Context context,
			List<MappedBeer> results) {
		searchArrayList = results;
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return searchArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return searchArrayList.get(position);
	}
	
	/**
	 * Model data structure setter passthrough
	 */
	public void setItem(int index, MappedBeer beer) {
		if (searchArrayList != null && index < searchArrayList.size()) {
			searchArrayList.set(index, beer);
			notifyDataSetChanged();
		}
	}

	@Override
	public long getItemId(int position) {
		// the index can be the id
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.custom_row_view, null);
			TextView nameTextView = (TextView)convertView.findViewById(R.id.name);
			nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, convertView.getResources().getInteger(R.integer.searchResultsNameFontSize));
			nameTextView.setTextColor(convertView.getResources().getColor(R.color.nameDisplayColor));
			holder = new ViewHolder();
			holder.txtName = nameTextView;
			holder.txtAbv = (TextView) convertView.findViewById(R.id.abv);
			holder.txtDescript = (TextView) convertView.findViewById(R.id.descript);
			holder.txtBrewery = (TextView) convertView.findViewById(R.id.brewery);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.txtBrewery.setText(searchArrayList.get(position).getBrewery().getName());
		holder.txtAbv.setText(String.valueOf(searchArrayList.get(position).getAbv()));
		holder.txtDescript.setText(searchArrayList.get(position).getDescript());

		return convertView;
	}

	private class ViewHolder {
		TextView txtName;
		TextView txtBrewery;
		TextView txtAbv;
		TextView txtDescript;
	}
}