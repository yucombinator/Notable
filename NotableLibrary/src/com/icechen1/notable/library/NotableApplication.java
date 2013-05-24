
package com.icechen1.notable.library;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "YOUR_FORM_KEY")
public class NotableApplication
    extends Application
{


    @Override
    public void onCreate() {
     //   ACRA.init(this);
        super.onCreate();
    }

}
