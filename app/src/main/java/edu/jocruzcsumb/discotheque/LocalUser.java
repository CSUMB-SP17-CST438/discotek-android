package edu.jocruzcsumb.discotheque;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.AccessToken;
import com.facebook.LoginStatusCallback;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.fail;

// For a user who has logged in on this local device
public class LocalUser extends User
{
	public static final String GOOGLE_TOKEN_KEY = "google_t";
	public static final String FACEBOOK_TOKEN_KEY = "fb_t";
	public static final String SOUNDCLOUD_TOKEN_KEY = "soundcloud_t";
	public static final String JSON_ID_TAG = "member_id";
	private static final String JSON_EMAIL_TAG = "email";
	private static final String JSON_TOKEN_TAG = "token";
	private static final String JSON_LOGIN_TYPE_TAG = "login_type";
	private static final String TAG = "LocalUser";
	private static final String GOOGLE_KEY = "google";
	private static final String FACEBOOK_KEY = "fb";
	private static final String SOUNDCLOUD_KEY = "soundcloud";
	private static final String AUTH_TYPE_KEY = "auth_type";
	private static final String AUTH_TOKEN_KEY = "auth_token";
	//Singleton CurrentUser
	private static LocalUser currentUser = null;
	private static SharedPreferences preferences = null;

	private int id;
	private String email = null;
	private LoginType loginType = null;
	private String token = null;

	public LocalUser(LoginType loginType, String email, String token, int id, User u)
	{
		super(u.getUserName(), u.getFirstName(), u.getLastName(), u.getPhoto(), u.getBio());
		this.id = id;
		this.loginType = loginType;
		this.email = email;
		this.token = token;
	}

	protected static LocalUser parse(JSONObject jsonLocalUser) throws JSONException
	{
		//TODO: get user info from JSON
		return new LocalUser(
				parseLoginType(jsonLocalUser.getString(JSON_LOGIN_TYPE_TAG)),
				jsonLocalUser.getString(JSON_EMAIL_TAG),
				jsonLocalUser.getString(JSON_TOKEN_TAG),
				jsonLocalUser.getInt(JSON_ID_TAG),
				User.parse(jsonLocalUser));
	}

	private static void signIn(Activity context)
	{
		Intent k = new Intent(context, PickFloorActivity.class);
		k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(k);
		context.finish();
	}

	private static LoginType parseLoginType(String l)
	{
		switch (l)
		{
			case GOOGLE_KEY:
				return LoginType.GOOGLE;
			case FACEBOOK_KEY:
				return LoginType.FACEBOOK;
			case SOUNDCLOUD_KEY:
				return LoginType.SOUNDCLOUD;
			default:
				return null;
		}
	}

	//Checks for leftover auth to log in to discotheque server
	public static boolean silentLogin(final MainActivity a, GoogleApiClient googleApiClient)
	{
		Log.i(TAG, "silentLogin");
		initPrefs(a);
		String t = preferences.getString(AUTH_TYPE_KEY, null);
		if (t == null)
		{
			return false;
		}

		final CountDownLatch x = new CountDownLatch(1);
		final SpecialResultCallback cb = new SpecialResultCallback(x);
		;
		LoginType type = parseLoginType(t);
		switch (type)
		{
			case GOOGLE:
				OptionalPendingResult<GoogleSignInResult> pendingResult =
						Auth.GoogleSignInApi.silentSignIn(googleApiClient);
				if (pendingResult.isDone())
				{
//					Log.d(TAG, "pendingResult.isDone() = true");
					GoogleSignInResult r = pendingResult.get();
					if (r.isSuccess())
					{
						// There's an immediate result available.
						GoogleSignInAccount gacc = r.getSignInAccount();
						if (gacc == null)
						{
							Log.e(TAG, "SilentSignIn: Google sign in result was null.");
							googleSignOut(googleApiClient);
							return false;
						}
						Log.i(TAG, "SilentSignIn Google attempting dtk login");
						boolean b = socketLogin(LoginType.GOOGLE, gacc.getIdToken());
						if(!b)
						{
							googleSignOut(googleApiClient);
						}
						return b;
					}
					else
					{
						return false;
					}
				}
				else
				{
					// There's no immediate result ready
					pendingResult.setResultCallback(cb);
				}

				try
				{
					x.await();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					googleSignOut(googleApiClient);
					return false;
				}
				GoogleSignInAccount gacc = null;
				if (cb.result != null)
				{
					gacc = cb.result.getSignInAccount();
					if (gacc != null)
					{
						Log.i(TAG, "SilentSignIn Google attempting dtk login");
						boolean b = socketLogin(LoginType.GOOGLE, gacc.getIdToken());
						if(!b)
						{
							googleSignOut(googleApiClient);
						}
						return b;
					}
					else
					{
						Log.wtf(TAG, "SilentSignIn: Google account was null.");
						googleSignOut(googleApiClient);
						return false;
					}
				}
				else
				{
					Log.e(TAG, "SilentSignIn: Google sign in result was null.");
					googleSignOut(googleApiClient);
					return false;
				}
			case FACEBOOK:
				a.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LoginManager.getInstance()
									.retrieveLoginStatus(a, cb);
					}
				});

				try
				{
					x.await();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					LoginManager.getInstance().logOut();
					return false;
				}
				if (cb.facebookToken != null)
				{
					Log.i(TAG, "SilentSignIn Facebook attempting dtk login");
					boolean b = socketLogin(LoginType.GOOGLE, cb.facebookToken);
					if(!b)
					{
						LoginManager.getInstance().logOut();
					}
					return b;
				}
				else
				{
					Log.wtf(TAG, "SilentSignIn: Facebook token was null.");
					LoginManager.getInstance().logOut();
					return false;
				}
			case SOUNDCLOUD:
				return false;
		}
		return false;
	}

	//Logs out of discotheque server and allows user to choose new login infos at MainActivity
	public static void logout(Activity context)
	{
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(AUTH_TYPE_KEY, null);
		editor.putString(AUTH_TOKEN_KEY, null);
		editor.apply();
		editor.commit();

		//TODO: should probably clear entire activity stack
		Intent k = new Intent(context, MainActivity.class);
		k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		k.putExtra("signout", true);
		context.startActivity(k);
		context.finish();
	}


	public static final String GOOGLE_AUTH_TAG = "Google auth";

	public static void googleSignOut(final GoogleApiClient googleApiClient)
	{
		Log.i(GOOGLE_AUTH_TAG, "googleSignOut");
		if (!googleApiClient.isConnected())
		{
			googleApiClient.connect();
			googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
			{
				@Override
				public void onConnected(@Nullable Bundle bundle)
				{
					if (googleApiClient.isConnected())
					{
						Auth.GoogleSignInApi.signOut(googleApiClient)
											.setResultCallback(new ResultCallback<Status>()
											{
												@Override
												public void onResult(@NonNull Status status)
												{
													if (status.isSuccess())
													{
														Log.i(GOOGLE_AUTH_TAG, "User Logged out");
													}
												}
											});
					}
					else
					{
						Log.e(GOOGLE_AUTH_TAG, "signout failed, trying again");
						googleSignOut(googleApiClient);
					}
				}

				@Override
				public void onConnectionSuspended(int i)
				{

				}
			});
		}
		else
		{
			doGoogleSignOut(googleApiClient);
		}
	}

	private static void doGoogleSignOut(final GoogleApiClient googleApiClient)
	{
		Auth.GoogleSignInApi.signOut(googleApiClient)
							.setResultCallback(new ResultCallback<Status>()
							{
								@Override
								public void onResult(@NonNull Status status)
								{
									Log.i(GOOGLE_AUTH_TAG, "googleSignOut onResult: " + status.getStatusMessage());
								}
							});
	}

	public static boolean isLoggedIn()
	{
		return currentUser != null;
	}


	public static LocalUser getCurrentUser()
	{
		if (currentUser == null)
		{
			Log.wtf(TAG, "Called getCuntUser when no user was logged in");
		}
		return currentUser;
	}

	//Should only be called after server vefifies the user
	private static void setCurrentUser(LocalUser user)
	{
		if (user.loginType == null || user.token == null)
		{
			Log.wtf(TAG, "setCuntUser requires a user with a LoginType and token");
		}
		String x = null;
		switch (user.getLoginType())
		{
			case GOOGLE:
				x = GOOGLE_KEY;
				break;
			case FACEBOOK:
				x = FACEBOOK_KEY;
				break;
			case SOUNDCLOUD:
				x = SOUNDCLOUD_KEY;
				break;
		}
		if (x == null)
		{
			Log.wtf(TAG, "setCuntUser requires a user with a LoginType and token");
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(AUTH_TYPE_KEY, x);
		editor.putString(AUTH_TOKEN_KEY, user.token);
		editor.apply();
		editor.commit();
		currentUser = user;
	}

	private static void initPrefs(Context context)
	{
		preferences = context.getSharedPreferences(context.getString(R.string.shared_prefs_file), Context.MODE_PRIVATE);
	}

	// This is a long operation
	// Returns true only if setCurrentUser was called
	public static boolean socketLogin(LoginType loginType, String token)
	{
		//We will emit 'login' and wait for 'login status'
		Sockets.SocketWaiter loginWaiter = new Sockets.SocketWaiter("login", "login status");
		JSONObject obj = new JSONObject();
		try
		{
			obj.put(LocalUser.getTokenJSONKey(loginType), token);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return false;
		}
		// This line of code will send the login message
		// and wait until it recieves login status back
		obj = loginWaiter.getObj(obj);

		if (obj == null)
		{
			Log.e(TAG, "login returned null (timeout or other error)");
			return false;
		}
		else
		{
			int a = 0;
			try
			{
				a = obj.getInt("authorized");
				if (a == 1)
				{
					LocalUser u = new LocalUser(
							loginType,
							obj.getString(JSON_EMAIL_TAG),
							token,
							obj.getInt(JSON_ID_TAG),
							User.parse(obj.getJSONObject("user"))
					);
					LocalUser.setCurrentUser(u);
					//Sockets.persist();
					return true;
				}
				else
				{
					return false;
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}

	public static String getTokenJSONKey(LoginType loginType)
	{
		switch (loginType)
		{
			case GOOGLE:
				return GOOGLE_TOKEN_KEY;
			case FACEBOOK:
				return FACEBOOK_TOKEN_KEY;
			case SOUNDCLOUD:
				return SOUNDCLOUD_TOKEN_KEY;
			default:
				return null;
		}
	}

	public LoginType getLoginType()
	{
		return this.loginType;
	}

	public String getEmail()
	{
		return email;
	}

	public int getId()
	{
		return id;
	}

	public enum LoginType
	{
		GOOGLE,
		FACEBOOK,
		SOUNDCLOUD
	}

	public static class SpecialResultCallback implements ResultCallback<GoogleSignInResult>, LoginStatusCallback
	{
		public GoogleSignInResult result = null;
		public String facebookToken = null;
		CountDownLatch latch = null;

		public SpecialResultCallback(CountDownLatch latch)
		{
			this.latch = latch;
		}

		@Override
		public void onResult(@NonNull GoogleSignInResult result)
		{
			Log.i(TAG, "Google Silent login onResult");
			if (result.isSuccess())
			{
				Log.i(TAG, "result.isSuccess()");
				this.result = result;
				latch.countDown();
			}
			else
			{
				latch.countDown();
			}
		}

		@Override
		public void onCompleted(AccessToken accessToken)
		{
			facebookToken = accessToken.getToken();
			latch.countDown();
			Log.i(TAG, "SilentSignIn Facebook onCompleted");
		}

		@Override
		public void onFailure()
		{
			Log.wtf(TAG, "SilentSignIn Facebook onFailure");
		}

		@Override
		public void onError(Exception exception)
		{
			Log.e(TAG, "SilentSignIn Facebook onError");
		}

	}
}
