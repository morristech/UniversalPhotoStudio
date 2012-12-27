/**
 * 
 */

package com.gmail.charleszq.picorner;

import java.io.File;

import org.jinstagram.auth.model.Token;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.gmail.charleszq.picorner.utils.IConstants;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

/**
 * Represents the main application.
 * 
 * @author charles
 */
public class PicornerApplication extends Application {
	
	private static final String FIRST_TIME_KEY = "first.time"; //$NON-NLS-1$

	@Override
	public void onCreate() {
		super.onCreate();
		enableHttpResponseCache();
	}

	private void enableHttpResponseCache() {
		try {
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			File httpCacheDir = new File(getCacheDir(), "http"); //$NON-NLS-1$
			android.net.http.HttpResponseCache.install(httpCacheDir,
					httpCacheSize);
		} catch (Exception httpResponseCacheNotAvailable) {
		}
	}

	public String getFlickrToken() {
		String token = getSharedPreferenceValue(IConstants.FLICKR_TOKEN, null);
		return token;
	}

	public OAuth loadSavedOAuth() {
		String userId = getFlickrUserId();
		String userName = getFlickrUserName();
		String token = getFlickrToken();
		String tokenSecret = getFlickrTokenSecret();
		if (userId == null || token == null || tokenSecret == null) {
			return null;
		}
		OAuth oauth = new com.googlecode.flickrjandroid.oauth.OAuth();
		oauth.setToken(new OAuthToken(token, tokenSecret));
		User user = new User();
		user.setId(userId);
		user.setRealName(userName);
		oauth.setUser(user);
		RequestContext.getRequestContext().setOAuth(oauth);
		return oauth;
	}

	public void saveFlickrAuthToken(OAuth oauth) {
		SharedPreferences sp = getSharedPreferences(IConstants.DEF_PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		String oauthToken = null;
		String tokenSecret = null;
		String userId = null;
		String userName = null;
		if (oauth != null) {
			oauthToken = oauth.getToken().getOauthToken();
			tokenSecret = oauth.getToken().getOauthTokenSecret();
			userId = oauth.getUser().getId();
			userName = oauth.getUser().getUsername();
		}
		editor.putString(IConstants.FLICKR_TOKEN, oauthToken);
		editor.putString(IConstants.FLICKR_TOKEN_SECRENT, tokenSecret);
		editor.putString(IConstants.FLICKR_USER_ID, userId);
		editor.putString(IConstants.FLICKR_USER_NAME, userName);
		editor.commit();
	}

	public void saveFlickrTokenSecret(String tokenSecrent) {
		SharedPreferences sp = getSharedPreferences(IConstants.DEF_PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(IConstants.FLICKR_TOKEN_SECRENT, tokenSecrent);
		editor.commit();
	}

	public String getFlickrTokenSecret() {
		return getSharedPreferenceValue(IConstants.FLICKR_TOKEN_SECRENT, null);
	}

	public String getFlickrUserName() {
		return getSharedPreferenceValue(IConstants.FLICKR_USER_NAME, null);
	}

	public String getFlickrUserId() {
		return getSharedPreferenceValue(IConstants.FLICKR_USER_ID, null);
	}

	/**
	 * Used to check if user log into px500 or not, currently.
	 * 
	 * @return
	 */
	public String getPx500UserId() {
		return null;
	}

	/**
	 * Returns the saved value in the shared preferences.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	private String getSharedPreferenceValue(String key, String defaultValue) {
		SharedPreferences sp = getSharedPreferences(IConstants.DEF_PREF_NAME,
				Context.MODE_APPEND);
		String value = sp.getString(key, defaultValue);
		return value;
	}

	public String getInstagramUserId() {
		return getSharedPreferenceValue(IConstants.IG_USER_ID, null);
	}

	public String getInstagramUserBuddyIconUrl() {
		return getSharedPreferenceValue(IConstants.IG_USER_BUDDY_ICON_URL, null);
	}

	public Token getInstagramAuthToken() {
		String token = getSharedPreferenceValue(IConstants.IG_AUTH_TOKEN, null);
		if (token == null) {
			return null;
		}
		String secret = getSharedPreferenceValue(
				IConstants.IG_AUTH_TOKEN_SECRET, null);
		String rawResponse = getSharedPreferenceValue(
				IConstants.IG_AUTH_TOKEN_RAW_RES, null);
		Token t = new Token(token, secret, rawResponse);
		return t;
	}

	public void saveInstagramAuthToken(long userId, String token,
			String secret, String rawResponse, String url) {
		SharedPreferences sp = getSharedPreferences(IConstants.DEF_PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(IConstants.IG_USER_ID, String.valueOf(userId));
		editor.putString(IConstants.IG_AUTH_TOKEN, token);
		editor.putString(IConstants.IG_AUTH_TOKEN_SECRET, secret);
		editor.putString(IConstants.IG_AUTH_TOKEN_RAW_RES, rawResponse);
		editor.putString(IConstants.IG_USER_BUDDY_ICON_URL, url);
		editor.commit();
	}
	
	public boolean isFirstTime() {
		return getSharedPreferenceValue(FIRST_TIME_KEY, null) == null;
	}
	
	public void setFirstTimeFalse() {
		SharedPreferences sp = getSharedPreferences(IConstants.DEF_PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(FIRST_TIME_KEY, Boolean.TRUE.toString());
		editor.commit();
	}
}
