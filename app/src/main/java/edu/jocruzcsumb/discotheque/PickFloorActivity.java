package edu.jocruzcsumb.discotheque;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PickFloorActivity extends AppCompatActivity implements View.OnClickListener
{
	private static final String TAG = "PickFloorActivity";
	private JSONArray jsonObject;
	private ArrayList<Floor> floorList;
	private FloorAdapter floorAdapter;
	private RecyclerView recyclerView;
    private Button logout;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_room);
		recyclerView = (RecyclerView) findViewById(R.id.room_listview);
		logout = (Button) findViewById(R.id.signout);
		logout.setOnClickListener(this);
		recyclerView.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(this);
    	recyclerView.setLayoutManager(llm);

		new Thread(new Runnable() {
			@Override
			public void run() {
				floorList = new ArrayList<Floor>();
				Sockets.SocketWaiter waiter = new Sockets.SocketWaiter("get floors", "floor list");
				JSONObject jsonObject2 = new JSONObject();
				try{
					jsonObject2.put("pls","pls");

				}catch(JSONException e){
					e.printStackTrace();
				}

				jsonObject = waiter.getArray(jsonObject2);
				Log.d(TAG, jsonObject.toString());
				if(jsonObject != null) {
					try {
						floorList = Floor.parse(jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					floorAdapter = new FloorAdapter(PickFloorActivity.this, floorList, new RecyclerViewListener() {
						@Override
						public void onItemClick(View v, int position) {
							Floor floor = floorList.get(position);
							Log.d(TAG, String.valueOf(floor.getId()));
							Intent k = new Intent(PickFloorActivity.this, FloorActivity.class);
							k.putExtra(Floor.TAG, floor.getId());
							startActivity(k);
						}
					});

					runOnUiThread(new Runnable() {
						@Override
						public void run(){
							recyclerView.setAdapter(floorAdapter);
						}
					});

				}
				else{
					Log.d(TAG, "Floor list is empty");
				}

			}
		}).start();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_pick_floor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.signout:
				LocalUser.logout(PickFloorActivity.this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{

//			case R.id.TEMP_go_to_room:
//				//go to activity
//				Intent k = new Intent(PickFloorActivity.this, FloorActivity.class);
//
//				k.putExtra(Floor.TAG, 2);
//
//				startActivity(k);
//
//				break;

			case R.id.signout:
				LocalUser.logout(PickFloorActivity.this);
				break;

		}
	}


	public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder>
	{

		ArrayList<Floor> floorList;
		Context mContext;
		RecyclerViewListener listener;

		FloorAdapter(Context mContext, ArrayList<Floor> floorList, RecyclerViewListener listener)
		{
			this.floorList = floorList;
			this.mContext = mContext;
			this.listener = listener;
		}

		public int getItemCount()
		{
			return floorList.size();
		}

		@Override
		public FloorViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
		{
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_floor, viewGroup, false);
			final FloorViewHolder svh = new FloorViewHolder(v);
			v.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					listener.onItemClick(v, svh.getAdapterPosition());
				}
			});

			return svh;


		}

		@Override
		public void onBindViewHolder(FloorViewHolder FloorViewHolder, int i)
		{
			FloorViewHolder.floorName.setText(floorList.get(i).getName());
			FloorViewHolder.themeImage.setImageResource(R.drawable.ic_launcher);

		}

		@Override
		public void onAttachedToRecyclerView(RecyclerView recyclerView)
		{
			super.onAttachedToRecyclerView(recyclerView);

		}

		public class FloorViewHolder extends RecyclerView.ViewHolder
		{
			CardView cv;
			TextView floorName;
			TextView genre;
			ImageView themeImage;

			FloorViewHolder(View itemView)
			{
				super(itemView);
				cv = (CardView) itemView.findViewById(R.id.floor_cardview);
				floorName = (TextView) itemView.findViewById(R.id.floorname);
				genre = (TextView) itemView.findViewById(R.id.genre);
				themeImage = (ImageView) itemView.findViewById(R.id.theme);
			}
		}
	}

}
