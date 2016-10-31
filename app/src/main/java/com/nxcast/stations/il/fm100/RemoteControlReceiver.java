package com.nxcast.stations.il.fm100;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.nxcast.stations.il.fm100.busEvents.NotificationBusEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by leonidangarov on 24/10/2016.
 */

public class RemoteControlReceiver extends BroadcastReceiver {
    public RemoteControlReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.i("100fm", "RemoteControlReceiver " + KeyEvent.KEYCODE_HEADSETHOOK);

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event != null) {
                int code = event.getKeyCode();
                NotificationBusEvent e = null;

                if (KeyEvent.KEYCODE_HEADSETHOOK == code ) {
                    e = new NotificationBusEvent("headsethook");
                } else if (KeyEvent.KEYCODE_MEDIA_PLAY == code) {
                    e = new NotificationBusEvent("play");
                } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == code) {
                    e = new NotificationBusEvent("pause");
                } else if (KeyEvent.KEYCODE_MEDIA_NEXT == code) {
                    e = new NotificationBusEvent("next");
                } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == code) {
                    e = new NotificationBusEvent("prev");
                }

                if( e != null ) {
                    EventBus.getDefault().post(e);
                }
            }
        }
    }
}
