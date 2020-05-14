package de.bulling.barcodebuddyscanner;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import de.bulling.barcodebuddyscanner.Helper.Secrets;


@AcraCore(buildConfigClass = BuildConfig.class,
		reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraMailSender(mailTo = "support-bb@bulling.mobi")
@AcraDialog(resText = R.string.crashed,
		resCommentPrompt = R.string.crashed_t)
public class CrashReport extends Application {

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		ACRA.init(this);
	}
}
