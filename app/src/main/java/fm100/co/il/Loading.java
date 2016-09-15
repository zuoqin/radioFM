package fm100.co.il;

import android.app.Activity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by leonidangarov on 06/12/2015.
 */
public class Loading extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        dissmisView();
    }

    public void dissmisView() {
        Timer splashTimer = new Timer();
        splashTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, 4000);
    }

    @Override
    public void onBackPressed() {
    }
}
