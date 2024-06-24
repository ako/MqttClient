package mqttclient.impl;

import mqttclient.proxies.qos;

/**
 * Created by ako on 4/26/2016.
 */
public class MqttSubscription {
    private String topic;
    private String onMessageMicroflow;
	private qos QoS;

    public MqttSubscription(String topic, String onMessageMicroflow, qos QoS) {
        this.onMessageMicroflow = onMessageMicroflow;
        this.topic = topic;
        this.QoS = QoS;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getOnMessageMicroflow() {
        return this.onMessageMicroflow;
    }

	public qos getQoS() {
		return this.QoS;
	}
}
