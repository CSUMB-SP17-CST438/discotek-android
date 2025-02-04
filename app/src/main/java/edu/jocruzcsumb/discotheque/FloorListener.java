package edu.jocruzcsumb.discotheque;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import static edu.jocruzcsumb.discotheque.FloorService.EVENT_FLOOR_JOINED;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_GET_CURRENT_SONG;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_GET_MESSAGE_LIST;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_GET_SONG_LIST;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_GET_USER_LIST;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_LEAVE_FLOOR;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_MESSAGE_ADD;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_MESSAGE_LIST_UPDATE;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_MESSAGE_SEND;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_SONG_LIST_UPDATE;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_USER_ADD;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_USER_LIST_UPDATE;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_USER_REMOVE;
import static edu.jocruzcsumb.discotheque.SeamlessMediaPlayer.EVENT_SONG_STARTED;
import static edu.jocruzcsumb.discotheque.SeamlessMediaPlayer.EVENT_SONG_STOPPED;

/**
 * Created by carsen on 4/20/17.
 */

public abstract class FloorListener extends BroadcastReceiver
{
	private static final String TAG = "FloorListener";
	private final String tag;
	private Context context;

	public FloorListener(IntentFilter intentFilter, Context context, String tag)
	{
		this.tag = tag;
		this.context = context;
		LocalBroadcastManager.getInstance(context)
							 .registerReceiver(this, intentFilter);
		Log.i(tag, TAG + ": started listening");
	}

	public FloorListener(Context context, String tag)
	{
		this(getDefaultFilter(), context, tag);
	}

	public static IntentFilter getDefaultFilter()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(EVENT_FLOOR_JOINED);
		filter.addAction(EVENT_SONG_STARTED);
		filter.addAction(EVENT_SONG_STOPPED);
		filter.addAction(EVENT_SONG_LIST_UPDATE);
		filter.addAction(EVENT_USER_LIST_UPDATE);
		filter.addAction(EVENT_MESSAGE_LIST_UPDATE);
		filter.addAction(EVENT_MESSAGE_ADD);
		filter.addAction(EVENT_USER_ADD);
		filter.addAction(EVENT_USER_REMOVE);
		return filter;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(tag, TAG + ": onRecieve: " + intent.getAction());

		if (intent.getAction()
				  .equals(EVENT_FLOOR_JOINED))
		{
			Floor floor = intent.getParcelableExtra(EVENT_FLOOR_JOINED);
			onFloorJoined(floor);
			if(floor.getSongs() != null) onSongListUpdate(floor.getSongs());
			if(floor.getMessages() != null) onMessageListUpdate(floor.getMessages());
			if(floor.getUsers() != null) onUserListUpdate(floor.getUsers());
		}
		else
		{
			switch (intent.getAction())
			{
				case EVENT_SONG_STARTED:
					Song s = intent.getParcelableExtra(EVENT_SONG_STARTED);
					onSongStarted(s);
					break;
				case EVENT_SONG_STOPPED:
					Song x = intent.getParcelableExtra(EVENT_SONG_STOPPED);
					onSongStopped(x);
					break;
				case EVENT_SONG_LIST_UPDATE:
					ArrayList<Song> songs = intent.getParcelableArrayListExtra(EVENT_SONG_LIST_UPDATE);
					onSongListUpdate(songs);
					break;
				case EVENT_USER_LIST_UPDATE:
					ArrayList<User> users = intent.getParcelableArrayListExtra(EVENT_USER_LIST_UPDATE);
					onUserListUpdate(users);
					break;
				case EVENT_MESSAGE_LIST_UPDATE:
					ArrayList<Message> messages = intent.getParcelableArrayListExtra(EVENT_MESSAGE_LIST_UPDATE);
					onMessageListUpdate(messages);
					break;
				case EVENT_MESSAGE_ADD:
					Message m = intent.getParcelableExtra(EVENT_MESSAGE_ADD);
					onMessageAdded(m);
					break;
				case EVENT_USER_ADD:
					User u = intent.getParcelableExtra(EVENT_USER_ADD);
					onUserAdded(u);
					break;
				case EVENT_USER_REMOVE:
					User r = intent.getParcelableExtra(EVENT_USER_REMOVE);
					onUserRemoved(r);
					break;
				case EVENT_GET_USER_LIST:
					getUsers();
					break;
				case EVENT_GET_SONG_LIST:
					getSongs();
					break;
				case EVENT_GET_MESSAGE_LIST:
					getMessages();
					break;
				case EVENT_MESSAGE_SEND:
					sendMessage((Message) intent.getParcelableExtra(EVENT_MESSAGE_SEND));
					break;
				case EVENT_GET_CURRENT_SONG:
					getCurrentSong();
					break;
				case EVENT_LEAVE_FLOOR:
					leaveFloor();
					break;
			}
		}
	}

	// The floor service uses these
	public void getCurrentSong()
	{
	}

	public void getUsers()
	{
	}

	public void getSongs()
	{
	}

	public void getMessages()
	{
	}

	public void sendMessage(Message m)
	{
	}

	public void leaveFloor()
	{
	}

	// EVENTS are broadcasted here
	public void broadcast(String event, ArrayList<? extends Parcelable> params)
	{
		Intent k = new Intent(event);
		k.putParcelableArrayListExtra(event, params);
		broadcast(k);
	}

	public void broadcast(String event, Parcelable extra)
	{
		Intent k = new Intent(event);
		k.putExtra(event, extra);
		broadcast(k);
	}

	public void broadcast(String event)
	{
		Intent k = new Intent(event);
		broadcast(k);
	}

	private void broadcast(Intent k)
	{
		LocalBroadcastManager.getInstance(context)
							 .sendBroadcast(k);
	}

	public void stop()
	{
		Log.i(tag, TAG + ": stopped listening");
		LocalBroadcastManager.getInstance(context)
							 .unregisterReceiver(this);
	}

	public void onSongStarted(Song s)
	{
	}

	public void onSongStopped(Song s)
	{
	}

	public void onFloorJoined(Floor floor)
	{
	}

	public void onSongListUpdate(ArrayList<Song> songs)
	{
	}

	public void onUserListUpdate(ArrayList<User> users)
	{
	}

	public void onUserAdded(User u)
	{
	}

	public void onUserRemoved(User u)
	{
	}

	public void onMessageListUpdate(ArrayList<Message> messages)
	{
	}

	public void onMessageAdded(Message m)
	{
	}
}
