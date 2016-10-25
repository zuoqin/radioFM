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
        Log.i("100fm", "RemoteControlReceiver ");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.i("100fm", "RemoteControlReceiver " + event.getKeyCode());
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                NotificationBusEvent e = new NotificationBusEvent("play");
                EventBus.getDefault().post(e);
            } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                Log.i("100fm", "KEYCODE_MEDIA_PAUSE");
                NotificationBusEvent e = new NotificationBusEvent("pause");
                EventBus.getDefault().post(e);
            } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                NotificationBusEvent e = new NotificationBusEvent("next");
                EventBus.getDefault().post(e);
            } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                NotificationBusEvent e = new NotificationBusEvent("prev");
                EventBus.getDefault().post(e);
            }
        }
    }
}
