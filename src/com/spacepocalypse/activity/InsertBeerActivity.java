package com.spacepocalypse.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedBeer;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.http.B4AWebClient;
import com.spacepocalypse.http.NotInitializedException;

public class InsertBeerActivity extends Activity {
	private static String TAG = "InsertBeerActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insert_layout);

		Button saveBtn = (Button)findViewById(R.id.insertLayout_saveButton);
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MappedBeer toInsert = new MappedBeer();

				EditText abvEdit = (EditText)findViewById(R.id.insertLayout_abv_edit);
				try {
					toInsert.setAbv(Double.valueOf(abvEdit.getText().toString()).floatValue());
				} catch (NumberFormatException e) {
					Toast.makeText(InsertBeerActivity.this,
							"Abv is not a decimal number!",
							Toast.LENGTH_LONG).show();
					return;
				}
				EditText nameEdit = (EditText)findViewById(R.id.insertLayout_name_edit);
				toInsert.setName(nameEdit.getText().toString());

				EditText descriptEdit = (EditText)findViewById(R.id.insertLayout_descript_edit);
				toInsert.setDescript(descriptEdit.getText().toString());

				try {
					B4AWebClient.insertMappedBeer(toInsert);
					setResult(RESULT_OK);
					finish();
				} catch (NotInitializedException e) {
					Log.e(TAG, "B4AWebClient not initialized!");
				} catch (JSONException e) {
					Log.e(TAG, "JSON Exception!");
				}
			}
		});
	}
}
