package fm100.co.il.busEvents;

/************************************************
 * the bus evenet which transfer a String from the NotificationBroadcast
 ************************************************/

public class NotificationBusEvent {
    private String notificationBusMsg;

    public NotificationBusEvent(String notificationBusMsg) {
        this.notificationBusMsg = notificationBusMsg;

    }

    public String getNotificationBusMsg() {
        return notificationBusMsg;
    }
}
