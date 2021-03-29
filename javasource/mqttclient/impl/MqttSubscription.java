package mqttclient.impl;

/**
 * Created by ako on 4/26/2016.
 */
public class MqttSubscription {
    private String topic;
    private String onMessageMicroflow;

    public MqttSubscription(String topic, String onMessageMicroflow) {
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
