/**
 * 
 */
package com.gmail.charleszq.picorner.ui.command.ig;

import org.jinstagram.auth.model.Token;

import android.app.Activity;
import android.content.Context;

import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.PicornerApplication;
import com.gmail.charleszq.picorner.model.Author;
import com.gmail.charleszq.picorner.service.IPhotoService;
import com.gmail.charleszq.picorner.service.ig.InstagramUserPhotosService;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public class InstagramUserPhotosCommand extends AbstractInstagramPhotoListCommand {

	private Author mUser;

	/**
	 * @param context
	 */
	public InstagramUserPhotosCommand(Context context, Author igUser) {
		super(context);
		this.mUser = igUser;
	}

	@Override
	public int getIconResourceId() {
		return R.drawable.ic_action_instagram_my_photos;
	}

	@Override
	public String getLabel() {
		return mContext.getString(R.string.ig_my_photos);
	}

	@Override
	public Object getAdapter(Class<?> adapterClass) {
		if (adapterClass == IPhotoService.class) {
			if (mCurrentPhotoService == null) {
				PicornerApplication app = (PicornerApplication) ((Activity) mContext)
						.getApplication();
				Token token = app.getInstagramAuthToken();
				mCurrentPhotoService = new InstagramUserPhotosService(token,
						Long.parseLong(mUser.getUserId()));
			}
			return mCurrentPhotoService;
		}
		return super.getAdapter(adapterClass);
	}
	
	@Override
	public String getDescription() {
		String s = mContext.getString(R.string.cd_ig_user_photos);
		return String.format(s, mUser.getUserName());
	}
}
