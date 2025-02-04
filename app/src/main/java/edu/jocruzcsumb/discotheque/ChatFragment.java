package edu.jocruzcsumb.discotheque;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static edu.jocruzcsumb.discotheque.FloorService.EVENT_FLOOR_JOINED;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_MESSAGE_ADD;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_MESSAGE_LIST_UPDATE;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_MESSAGE_SEND;

/**
 * Chat area fragment yo
 */
public class ChatFragment extends FloorFragment implements View.OnClickListener
{
	private static final String TAG = "ChatFragment";
	private ChatFragment.ChatAdapter chatAdapter;
	private RecyclerView recyclerView;
	private EditText chatField;
	private LinearLayoutManager llm;

	public static ChatFragment newInstance()
	{
		ChatFragment fragment = new ChatFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		start(TAG);
		View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
		rootView.findViewById(R.id.send_button)
				.setOnClickListener(this);
		chatField = (EditText) rootView.findViewById(R.id.chat_edit_text);
		recyclerView = (RecyclerView) rootView.findViewById(R.id.rv2);
		recyclerView.setHasFixedSize(true);
		llm = new LinearLayoutManager(this.getActivity());
		llm.setStackFromEnd(true); //scrolls to the bottom
		recyclerView.setLayoutManager(llm);

		if (findFloor())
		{
			updateListUI(floor.getMessages());

		}
		return rootView;
	}

	@Override
	public void onMessageListUpdate(ArrayList<Message> messages)
	{
		updateListUI(messages);
	}

	@Override
	public void onMessageAdded(Message m)
	{
		updateListUI(floor.getMessages());
	}

	private void updateListUI(final ArrayList<Message> messages)
	{
		Log.i(TAG, "updateListUI");
		Activity a = getActivity();
		if (a == null)
		{
			return;
		}
		chatAdapter = new ChatAdapter(a, messages);
		a.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recyclerView.setAdapter(chatAdapter);

			}
		});

	}

	public void onClick(View v)
	{
		String text = chatField.getText()
							   .toString();
		if (!findFloor())
		{
			Log.wtf(TAG, "NO FLOOR ON CLICK");
			return;
		}
		Message m = new Message(0, LocalUser.getCurrentUser(), text, floor.getId(), 0);
		Intent k = new Intent(EVENT_MESSAGE_SEND);
		k.putExtra(EVENT_MESSAGE_SEND, m);
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
							 .sendBroadcast(k);

		//TODO: Create an object that shows a little loading things, and hide it when the message sent successfully
		chatField.setText("");
	}

	@Override
	public IntentFilter getFilter()
	{
		IntentFilter f = new IntentFilter();
		f.addAction(EVENT_FLOOR_JOINED);
		f.addAction(EVENT_MESSAGE_LIST_UPDATE);
		f.addAction(EVENT_MESSAGE_ADD);
		return f;
	}

	public class ChatAdapter extends RecyclerView.Adapter<ChatFragment.ChatAdapter.ChatViewHolder>
	{
		Context mContext;
		ArrayList<Message> messages = null;

		ChatAdapter(Context mContext, ArrayList<Message> messages)
		{
			this.messages = messages;
			this.mContext = mContext;
		}

		public int getItemCount()
		{
			return messages.size();
		}

		@Override
		public ChatFragment.ChatAdapter.ChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
		{
			View v = LayoutInflater.from(viewGroup.getContext())
								   .inflate(R.layout.list_chat, viewGroup, false);
			ChatFragment.ChatAdapter.ChatViewHolder svh = new ChatFragment.ChatAdapter.ChatViewHolder(v);
			return svh;
		}

		@Override
		public void onBindViewHolder(ChatFragment.ChatAdapter.ChatViewHolder chatViewHolder, int i)
		{
			Date date = new Date(messages.get(i).getPubTime()); // 'epoch' in long

			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			String dateString = formatter.format(date);

			formatter = new SimpleDateFormat("hh:mm a"); //The "a" is the AM/PM marker
			String time = formatter.format(date);
			chatViewHolder.name.setText(messages.get(i)
												.getAuthor()
												.getFirstName() + " " + messages.get(i)
																				.getAuthor()
																				.getLastName());
			chatViewHolder.message.setText(messages.get(i)
												   .getText());
			chatViewHolder.time.setText(dateString + "\n " +  time);
			//if (!userChatList.get(i).getPhoto().equals("null"))
			// {
			Picasso.with(mContext) //TODO check resize
				   .load(messages.get(i)
								 .getAuthor()
								 .getPhoto())
				   .resize(200, 200)
				   .centerInside()
				   .transform(new CircleTransform())
				   .into(chatViewHolder.image);
			//}
			//else
			//{
			//chatViewHolder.image.setImageResource(R.drawable.ic_launcher);
			//}
		}

		@Override
		public void onAttachedToRecyclerView(RecyclerView recyclerView)
		{
			super.onAttachedToRecyclerView(recyclerView);
		}

		public class ChatViewHolder extends RecyclerView.ViewHolder
		{
			CardView cv;
			TextView message;
			TextView name;
			TextView time;
			ImageView image;

			ChatViewHolder(View itemView)
			{
				super(itemView);
				cv = (CardView) itemView.findViewById(R.id.chatCardView);
				name = (TextView) itemView.findViewById(R.id.sender);
				message = (TextView) itemView.findViewById(R.id.chat);
				image = (ImageView) itemView.findViewById(R.id.chatPhoto);
				time = (TextView) itemView.findViewById(R.id.pubTime);
			}
		}
	}
}
