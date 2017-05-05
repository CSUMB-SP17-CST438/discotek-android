package edu.jocruzcsumb.discotheque;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;


/**
 * Created by Admin on 4/18/2017.
 */

public class UpdateProfileActivity extends AppCompatActivity implements View.OnClickListener, Emitter.Listener, AdapterView.OnItemSelectedListener
{

	public static final String EVENT_UPDATE_PROFILE = "update profile";
	public static final String EVENT_PROFILE_UPDATED = "profile updated";
	public static final String EVENT_PROFILE_ID = "member_id";
	public static final String USER_TAG = "user";
	public static final String BIO_TAG = "bio";
	private static final String TAG = "UpdateProfileActivity";
	private static final String USERNAME_TAG = "username";
	//private  EditText editUsername;
	private EditText editBio;
	private Button saveButton;
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_profile);
		//editUsername = (EditText) findViewById(R.id.username);
		editBio = (EditText) findViewById(R.id.updateBio);
		saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(this);
		Intent in = getIntent();
		user = (User) in.getParcelableExtra("user");
		editBio.setText(user.getBio());
//		Spinner spinner1 = (Spinner) findViewById(R.id.profileGenre);
//		Spinner spinner2 = (Spinner) findViewById(R.id.profileGenre2);
//		Spinner spinner3 = (Spinner) findViewById(R.id.profileGenre3);
//
//		spinner1.setOnItemSelectedListener(this);
//		spinner2.setOnItemSelectedListener(this);
//		spinner3.setOnItemSelectedListener(this);
//
//		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//				R.array.profileGenre, android.R.layout.simple_spinner_item);
//
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//		spinner1.setAdapter(adapter);
//		spinner2.setAdapter(adapter);
//		spinner3.setAdapter(adapter);
		Sockets.getSocket()
			   .on(EVENT_PROFILE_UPDATED, this);
	}

	@Override
	public void onClick(View v)
	{
		String bio = editBio.getText()
							.toString();
		//String username = editUsername.getText()
		//		                      .toString();
		Log.d(TAG, bio);
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put(EVENT_PROFILE_ID, LocalUser.getCurrentUser()
													  .getId());
			jsonObject.put(BIO_TAG, bio);
			//jsonObject.put(USERNAME_TAG, username);
		}
		catch (JSONException e)
		{
			e.getStackTrace();
		}

		Sockets.getSocket()
			   .emit(EVENT_UPDATE_PROFILE, jsonObject);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(UpdateProfileActivity.this, ViewProfileActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(USER_TAG, user);
		startActivity(intent);
		finish();

	}

	@Override
	public void call(Object... args)
	{
		Log.d(TAG, "received object: " + args[0]);
		JSONObject jsonObject = (JSONObject) args[0];
        try{
			user = User.parse(jsonObject);
        }
        catch(JSONException e){
            e.getStackTrace();
        }


		Intent intent = new Intent(UpdateProfileActivity.this, ViewProfileActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(USER_TAG, user);
		startActivity(intent);
		finish();

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		parent.getItemAtPosition(pos);

		//do something with the selected genre

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		//Don't think we need to mess with this?
	}
}
