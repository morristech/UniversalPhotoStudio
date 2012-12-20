/**
 * 
 */
package com.gmail.charleszq.ups.ui.command.ig;

import org.jinstagram.auth.model.Token;

import android.app.Activity;
import android.content.Context;

import com.gmail.charleszq.ups.UPSApplication;
import com.gmail.charleszq.ups.model.Author;
import com.gmail.charleszq.ups.service.IPhotoService;
import com.gmail.charleszq.ups.service.ig.InstagramUserPhotosService;
import com.gmail.charleszq.ups.ui.command.PhotoListCommand;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public class InstagramUserPhotosCommand extends PhotoListCommand {

	private Author mUser;

	/**
	 * @param context
	 */
	public InstagramUserPhotosCommand(Context context, Author igUser) {
		super(context);
		this.mUser = igUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gmail.charleszq.ups.ui.command.ICommand#getIconResourceId()
	 */
	@Override
	public int getIconResourceId() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gmail.charleszq.ups.ui.command.ICommand#getLabel()
	 */
	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public Object getAdapter(Class<?> adapterClass) {
		if (adapterClass == IPhotoService.class) {
			UPSApplication app = (UPSApplication) ((Activity) mContext)
					.getApplication();
			Token token = app.getInstagramAuthToken();
			return new InstagramUserPhotosService(token, Long.parseLong(mUser
					.getUserId()));
		}
		if (adapterClass == Integer.class) {
			return 50; 
			//TODO test load more data with this command
		}
		return super.getAdapter(adapterClass);
	}
}
