package edu.jocruzcsumb.discotheque;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static edu.jocruzcsumb.discotheque.UpdateProfileActivity.USER_TAG;


public class ViewProfileActivity extends AppCompatActivity implements View.OnClickListener
{
	public static final String TAG = "ViewProfileActivity";
	private TextView editUsername;
	private TextView editBio;
	private ImageView image;
	private User user;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_profile);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		editUsername = (TextView) findViewById(R.id.username);
		editBio = (TextView) findViewById(R.id.bio);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		image = (ImageView) findViewById(R.id.profile_picture);
		fab.setOnClickListener(this);
		//pass object from previous activity
		Intent in = getIntent();
		user = (User) in.getParcelableExtra("user");
		if (user != null)
		{
			//if not current user, info is not edible
			//TODO:hide button if not local user
			if(!user.getUserName().equals(LocalUser.getCurrentUser().getUserName())){
				fab.setVisibility(View.GONE);
			}
			getSupportActionBar().setTitle(user.getFirstName() + " " + user.getLastName());
			Picasso.with(this)
				   .load(user.getPhoto())
				   .transform(new CircleTransform())
				   .into(image);
			editUsername.setText(user.getUserName());
			editBio.setText(user.getBio());

		}
	}


	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.fab:
//				String tempfirstName, tempLastName, tempDescription, tempEmail, tempGenres;
//				tempfirstName = editFirstName.getText().toString();
//				tempLastName = editLastName.getText().toString();
//				tempDescription = editDescription.getText().toString();
//				tempEmail = editEmail.getText().toString();
//				tempGenres = editGenres.getText().toString();
//				JSONObject jsonObject = new JSONObject();

//				try
//				{
//					jsonObject.put("firstName", tempfirstName);
//					jsonObject.put("lastName", tempLastName);
//					jsonObject.put("description", tempDescription);
//					jsonObject.put("tempEmail", tempEmail);
//					jsonObject.put("genre", tempGenres);
//				}
//				catch(JSONException e)
//				{
//					e.printStackTrace();
//				}

//				Sockets.getSocket().emit("update user", jsonObject);
				Intent intent = new Intent(ViewProfileActivity.this, UpdateProfileActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(USER_TAG, user);
				startActivity(intent);
				finish();
				Snackbar.make(view, "Profile info saved", Snackbar.LENGTH_LONG)
						.setAction("Action", null)
						.show();
				break;

			//case R.id.friends:
			//Intent guestLogin = new Intent(ViewProfileActivity.this, ViewFriendActivity.class);
			//startActivity(guestLogin);
		}

	}
}
