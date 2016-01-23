package mqttclient.impl;

/**
 * Created by ako on 1/10/2016.
 */
public class Subscription {
    private String topic;
    private String onMessageMicroflow;

    public Subscription(String topic, String onMessageMicroflow) {
        this.onMessageMicroflow = onMessageMicroflow;
        this.topic = topic;

    }

    public String getTopic() {
        return topic;
    }

    public String getOnMessageMicroflow() {
        return onMessageMicroflow;
    }
}
