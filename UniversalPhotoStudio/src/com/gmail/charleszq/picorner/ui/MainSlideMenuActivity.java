/**
 * 
 */
package com.gmail.charleszq.picorner.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.gmail.charleszq.picorner.BuildConfig;
import com.gmail.charleszq.picorner.PicornerApplication;
import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.model.MediaObjectCollection;
import com.gmail.charleszq.picorner.msg.MessageBus;
import com.gmail.charleszq.picorner.ui.command.CommandType;
import com.gmail.charleszq.picorner.ui.command.ICommand;
import com.gmail.charleszq.picorner.ui.command.ICommandDoneListener;
import com.gmail.charleszq.picorner.ui.command.PhotoListCommand;
import com.gmail.charleszq.picorner.ui.command.flickr.FlickrIntestringCommand;
import com.gmail.charleszq.picorner.ui.command.ig.InstagramPopularsCommand;
import com.gmail.charleszq.picorner.ui.command.px500.PxEditorsPhotosCommand;
import com.gmail.charleszq.picorner.ui.command.px500.PxFreshTodayPhotosCommand;
import com.gmail.charleszq.picorner.ui.command.px500.PxPopularPhotosCommand;
import com.gmail.charleszq.picorner.ui.command.px500.PxUpcomingPhotosCommand;
import com.gmail.charleszq.picorner.utils.IConstants;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * @author Charles(charleszq@gmail.com)
 * 
 */
public class MainSlideMenuActivity extends SlidingFragmentActivity {

	private static final String TAG = MainSlideMenuActivity.class
			.getSimpleName();
	private Fragment mContent;
	private ICommand<MediaObjectCollection> mCommand;

	// License Check
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkk2BWGUWXSRKCy31ytmFNYD09qq9AHpEfd+jz3/zyi3ykKVbWYdTIS+RZCio3fGAa1pMQHai6TZe1h+qpsR0EyMnlqgB5A23kwu5MI43uelw8JDgCJznXkZv7n3NJcG2uUNqMCz/VbGHukXXQkynx7PD2RDJLF9GQXIX2O/BA5iy9CvKLaIP++SfjTd/KS78KWfRTMqJCVqqIDadznMKHwH2ThJSCWHwdfrJG4TksEumiIZzbJmA3SFVt47qHZse0rpQhXlJ7Cob1gK/EsmkRkGcGrEGh+DeAFf70E5Nj7tY+yrw0bwBQtEPKYar27WZUP76GjW4ujgxXIaB1B9JbwIDAQAB"; //$NON-NLS-1$
	// Generate your own 20 random bytes, and put them here.
	private static final byte[] SALT = new byte[] { -46, 79, 83, -128, -103,
			-57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64,
			89 };
	private LicenseCheckerCallback mLicenseCheckerCallback;
	private LicenseChecker mChecker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		customizeSlideMenu();

		// set the Above View
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent"); //$NON-NLS-1$
		if (mContent == null) {
			mContent = new PhotoGridFragment();
		}

		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new MainMenuFragment()).commit();

		// secondary menu
		getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_two);
		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame_two, new SecondaryMenuFragment())
				.commit();

		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	/**
	 * When first time this activity starts, load default photo list, now it's
	 * flickr interesting photos.
	 */
	void loadDefaultPhotoList() {
		MessageBus.reset();
		mCommand = getDefaultCommand();
		final ProgressDialog dialog = ProgressDialog.show(this,
				"", getString(R.string.loading_photos)); //$NON-NLS-1$
		dialog.setCancelable(true);
		mCommand.setCommndDoneListener(new ICommandDoneListener<MediaObjectCollection>() {

			@Override
			public void onCommandDone(ICommand<MediaObjectCollection> command,
					MediaObjectCollection t) {
				MainSlideMenuActivity.this.onCommandDone(command, t);
				if (dialog != null && dialog.isShowing()) {
					try {
						dialog.dismiss();
					} catch (Exception ex) {

					}
				}
			}
		});
		mCommand.execute();
	}

	private PhotoListCommand getDefaultCommand() {
		SharedPreferences sp = this.getSharedPreferences(
				IConstants.DEF_PREF_NAME, Context.MODE_APPEND);
		String defaultCommandString = sp.getString(
				IConstants.PREF_DEFAULT_PHOTO_LIST, "1"); //$NON-NLS-1$
		switch (Integer.parseInt(defaultCommandString)) {
		case 1:
			return new PxPopularPhotosCommand(this);
		case 2:
			return new PxEditorsPhotosCommand(this);
		case 3:
			return new PxUpcomingPhotosCommand(this);
		case 4:
			return new PxFreshTodayPhotosCommand(this);
		case 5:
			return new FlickrIntestringCommand(this);
		default:
			return new InstagramPopularsCommand(this);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	private void customizeSlideMenu() {
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBackgroundColor(getResources().getColor(
				R.color.menu_frame_bg_color));
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);

		sm.setMode(SlidingMenu.LEFT_RIGHT);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getSlidingMenu().showMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent); //$NON-NLS-1$
	}

	void onCommandDone(ICommand<?> command, Object result) {
		CommandType type = command.getCommandType();
		switch (type) {
		case PHOTO_LIST_CMD:
			if (mContent instanceof PhotoGridFragment) {
				MediaObjectCollection col = (MediaObjectCollection) result;
				if (col == null || col.getPhotos().isEmpty()) {
					String msg = getString(R.string.msg_no_photo_returned);
					Toast.makeText(this,
							String.format(msg, command.getDescription()),
							Toast.LENGTH_SHORT).show();
					return;
				}
				((PhotoGridFragment) mContent).populatePhotoList(
						(MediaObjectCollection) result, command);
			} else {
				Log.w(TAG, "Not photo grid fragment?"); //$NON-NLS-1$
			}
			break;
		default:
			break;
		}
	}

	void closeMenu() {
		this.getSlidingMenu().toggle();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mChecker != null) {
			mChecker.onDestroy();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!((PicornerApplication) getApplication()).isLicensed()) {
			if (!BuildConfig.DEBUG) {
				checkLicense();
			}
		}
	}

	private void checkLicense() {
		if (mLicenseCheckerCallback == null) {
			// Library calls this when it's done.
			mLicenseCheckerCallback = new MyLicenseCheckerCallback();
		}

		if (mChecker == null) {
			// Construct the LicenseChecker with a policy.
			// Try to use more data here. ANDROID_ID is a single point of
			// attack.
			String deviceId = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);
			mChecker = new LicenseChecker(this, new ServerManagedPolicy(this,
					new AESObfuscator(SALT, getPackageName(), deviceId)),
					BASE64_PUBLIC_KEY);
		}

		mChecker.checkAccess(mLicenseCheckerCallback);
	}

	private void onInvalidLicense() {
		new AlertDialog.Builder(MainSlideMenuActivity.this)
				.setTitle(R.string.unlicensed_dialog_title)
				.setMessage(R.string.unlicensed_dialog_body)
				.setPositiveButton(R.string.buy_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Uri uri = Uri
										.parse("https://play.google.com/store/apps/details?id=" + PicornerApplication.class.getPackage().getName()); //$NON-NLS-1$
								Intent marketIntent = new Intent(
										Intent.ACTION_VIEW, uri);
								startActivity(marketIntent);
							}
						})
				.setNegativeButton(R.string.quit_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).create().show();
	}

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		public void allow() {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should allow user access.
			((PicornerApplication) getApplication()).setLicensedTrue();
		}

		public void dontAllow() {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should not allow access. In most cases, the app should assume
			// the user has access unless it encounters this. If it does,
			// the app should inform the user of their unlicensed ways
			// and then either shut down the app or limit the user to a
			// restricted set of features.
			// In this example, we show a dialog that takes the user to Market.
			onInvalidLicense();
		}

		public void applicationError(ApplicationErrorCode errorCode) {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			if (!ApplicationErrorCode.NOT_MARKET_MANAGED.equals(errorCode)) {
				// This is a polite way of saying the developer made a mistake
				// while setting up or calling the license checker library.
				// Please examine the error code and fix the error.
				String result = String.format(
						getString(R.string.application_error), errorCode);
				Log.w(TAG, result);
			}
		}
	}

}
