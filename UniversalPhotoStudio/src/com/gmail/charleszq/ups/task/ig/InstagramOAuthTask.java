/**
 * 
 */
package com.gmail.charleszq.ups.task.ig;

import org.jinstagram.Instagram;
import org.jinstagram.auth.InstagramAuthService;
import org.jinstagram.auth.model.Token;
import org.jinstagram.auth.model.Verifier;
import org.jinstagram.auth.oauth.InstagramService;
import org.jinstagram.entity.users.basicinfo.UserInfo;

import android.app.Activity;
import android.content.Context;

import com.gmail.charleszq.ups.UPSApplication;
import com.gmail.charleszq.ups.task.AbstractContextAwareTask;
import com.gmail.charleszq.ups.utils.IConstants;

/**
 * @author charleszq@gmail.com
 * 
 */
public class InstagramOAuthTask extends
		AbstractContextAwareTask<String, Integer, Token> {

	public InstagramOAuthTask(Context ctx) {
		super(ctx);
	}

	@Override
	protected Token doInBackground(String... params) {
		String code = params[0];
		InstagramService service = new InstagramAuthService()
				.apiKey(IConstants.INSTAGRAM_CLIENT_ID)
				.apiSecret(IConstants.INSTAGRAM_CLIENT_SECRET)
				.callback(IConstants.IG_CALL_BACK_STR).build();
		Verifier v = new Verifier(code);
		try {
			Token token = service.getAccessToken(null, v);
			if( token != null ) {
				Instagram ig = new Instagram(token);
				UserInfo user = ig.getCurrentUserInfo();
				long userId = user.getData().getId();
				
				UPSApplication app = (UPSApplication) ((Activity)mContext).getApplication();
				app.saveInstagramAuthToken(userId, token.getToken(), token.getSecret(), token.getRawResponse());
			}
			return token;
		} catch (Exception e) {
			logger.error("Instagram oauth failed: " + e.getMessage()); //$NON-NLS-1$
			return null;
		}
	}

}