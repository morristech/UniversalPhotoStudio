/**
 * 
 */
package com.gmail.charleszq.picorner.ui.command.px500;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.github.yuyang226.j500px.users.User;
import com.gmail.charleszq.picorner.PicornerApplication;
import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.model.Author;
import com.gmail.charleszq.picorner.task.IGeneralTaskDoneListener;
import com.gmail.charleszq.picorner.task.px500.PxFetchUserProfileTask;
import com.gmail.charleszq.picorner.ui.command.PhotoListCommand;
import com.gmail.charleszq.picorner.utils.IConstants;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public abstract class AbstractPx500PhotoListCommand extends PhotoListCommand {

	/**
	 * @param context
	 */
	public AbstractPx500PhotoListCommand(Context context) {
		super(context);
	}

	@Override
	public Object getAdapter(Class<?> adapterClass) {
		if (adapterClass == Integer.class) {
			return IConstants.DEF_500PX_PAGE_SIZE;
		}
		return super.getAdapter(adapterClass);
	}

	protected String getAuthToken() {
		PicornerApplication app = (PicornerApplication) ((Activity) mContext)
				.getApplication();
		return app.getPx500OauthToken();
	}

	protected String getAuthTokenSecret() {
		PicornerApplication app = (PicornerApplication) ((Activity) mContext)
				.getApplication();
		return app.getPx500OauthTokenSecret();
	}

	protected String getUserId() {
		PicornerApplication app = (PicornerApplication) ((Activity) mContext)
				.getApplication();
		Author a = app.getPxUserProfile();
		if (a != null) {
			return a.getUserId();
		} else {
			return null;
		}
	}

	/**
	 * If a sub-class needs the 500px login user id information but it's not
	 * saved yet, the sub-class needs to call this first.
	 * 
	 * @param params
	 */
	protected void fetchUserProfile(final Object... params) {
		PxFetchUserProfileTask task = new PxFetchUserProfileTask(mContext);
		task.addTaskDoneListener(new IGeneralTaskDoneListener<User>() {

			@Override
			public void onTaskDone(User result) {
				if (result == null) {
					// error
					Toast.makeText(
							mContext,
							mContext.getString(R.string.msg_px_error_fetch_user_profile),
							Toast.LENGTH_SHORT).show();
				} else {
					execute(params);
				}
			}
		});
		task.execute();
	}

}
