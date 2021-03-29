package mqttclient.impl;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

/**
 * Created by ako on 1/9/2016.
 */
public class MqttConnector {
    private static Map<String, MqttConnection> mqttHandlers = new HashMap<String, MqttConnection>();
    protected static ILogNode logger = Core.getLogger("MqttConnector");

    private MqttConnector() { }

    
    public static void subscribe(String brokerHost, Long brokerPort, String brokerOrganisation, String topicName, String onMessageMicroflow, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password, mqttclient.proxies.qos QoS, long timeout) throws Exception {
        MqttConnection connection = getMqttConnection(brokerHost, brokerPort, brokerOrganisation, CA, ClientCertificate, ClientKey, CertificatePassword, username, password, timeout);
        connection.subscribe(topicName, onMessageMicroflow, QoS);
    }

    public static void unsubscribe(String brokerHost, Long brokerPort, String topicName) throws Exception {
        MqttConnection connection = getMqttConnection(brokerHost, brokerPort, null, null, null, null, null, null, null,0);
        connection.unsubscribe(topicName);
    }
    
    public static void publish(String brokerHost, Long brokerPort, String brokerOrganisation, String topicName, String message, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password, mqttclient.proxies.qos QoS, long timeout) throws Exception {
        MqttConnection connection = getMqttConnection(brokerHost, brokerPort, brokerOrganisation, CA, ClientCertificate, ClientKey, CertificatePassword, username, password, timeout);
        connection.publish(topicName, message, QoS);
    }

    private static MqttConnection getMqttConnection(String brokerHost, Long brokerPort, String brokerOrganisation, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password, long timeout) throws Exception {
        String key = brokerHost + ":" + brokerPort;
        MqttConnection handler;
        synchronized (mqttHandlers) {
            logger.info("Number of objects in mqttHandlers map: " + mqttHandlers.size());

            if (!mqttHandlers.containsKey(key)) {
                logger.info("creating new MqttConnection");
                try {
                    handler = new MqttConnection(brokerHost, brokerPort, brokerOrganisation, CA, ClientCertificate, ClientKey, CertificatePassword, username, password, timeout);
                    mqttHandlers.put(key, handler);
                } catch (Exception e) {
                    logger.error(e);
                    throw e;
                }

            } else {
                logger.info("Found existing MqttConnection");
                handler = mqttHandlers.get(key);
            }
            logger.info("Number of objects in mqttHandlers map: " + mqttHandlers.size());
        }

        return handler;
    }



    private static class MqttConnection {
        private MqttClient client;
        private HashMap<String, MqttSubscription> subscriptions = new HashMap<>();
        private String broker;
        private String clientId;

        public MqttConnection(String brokerHost,  Long brokerPort, String brokerOrganisation, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password, long connectionTimeout) throws Exception {
            String hostname = InetAddress.getLocalHost().getHostName();
            String xasId = Core.getXASId();
            
            boolean useSsl = (ClientCertificate != null && !ClientCertificate.equals(""));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            if(connectionTimeout != 0)
            	connOpts.setConnectionTimeout(Math.toIntExact(connectionTimeout));
            else
            	connOpts.setConnectionTimeout(60);
            
            connOpts.setKeepAliveInterval(60);
            
            if(brokerOrganisation != null && !brokerOrganisation.equals("")){
            	this.broker = String.format("tcp://%1s.%2s:%d",brokerOrganisation, brokerHost, brokerPort);
            	this.clientId = "a:" + brokerOrganisation + ":" + xasId;
            }            
            else{
            	this.broker = String.format("tcp://%s:%d", brokerHost, brokerPort);
                this.clientId = "MxClient_" + xasId + "_" + hostname + "_" + brokerHost + "_" + brokerPort;
            }
            logger.info("new MqttConnection client id " + this.clientId);



            if (username != null && !username.equals("")) {
                connOpts.setUserName(username);
            }
            if (password != null && !password.equals("")) {
                connOpts.setPassword(password.toCharArray());
            }

            if (useSsl) {
                this.broker = String.format("ssl://%s:%d", brokerHost, brokerPort);
                connOpts.setCleanSession(true);

                try {
                    String resourcesPath = null;
                    try {
                        resourcesPath = Core.getConfiguration().getResourcesPath().getPath();
                        resourcesPath += File.separator;

                    } catch (Exception e) {
                        resourcesPath = "";
                    }
                    connOpts.setSocketFactory(SslUtil.getSslSocketFactory(
                            resourcesPath + CA,
                            resourcesPath + ClientCertificate,
                            resourcesPath + ClientKey,
                            CertificatePassword
                    ));
                } catch (Exception e) {
                    logger.error(e);
                    throw e;
                }
            }

            MemoryPersistence persistence = new MemoryPersistence();

            try {
                this.client = new MqttClient(this.broker, this.clientId, persistence);
                logger.info("Connecting to broker: " + this.broker);
                this.client.connect(connOpts);
                this.client.setCallback(new MxMqttCallback(this.client, this.subscriptions));
                logger.info("Connected");
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }
        }

//        public boolean isSubscribed(String topic) {
//            return this.subscriptions.containsKey(topic);
//
//        }

        public void subscribe(String topic, String onMessageMicroflow, mqttclient.proxies.qos QoS) throws MqttException {
            logger.info(String.format("MqttConnection.subscribe: %s", this.client.getClientId()));
            try {
                if(!this.client.isConnected()){
                    this.client.reconnect();
                }
                int subscriptionQos = 0;
                if(QoS.equals(mqttclient.proxies.qos.At_Most_Once_0)){
                	subscriptionQos = 0;
                }else if(QoS.equals(mqttclient.proxies.qos.At_Least_Once_1)){
                	subscriptionQos= 1;
                }else if(QoS.equals(mqttclient.proxies.qos.Exactly_Once_2)){
                	subscriptionQos= 2;
                }

                this.client.subscribe(topic, subscriptionQos);
                this.subscriptions.put(topic, new MqttSubscription(topic, onMessageMicroflow));
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }

        }
        public void unsubscribe(String topicName) throws MqttException {
            logger.info(String.format("unsubscribe: %s, %s", topicName, this.client.getClientId()));
            try {
                this.client.unsubscribe(topicName);
            } catch (MqttException e) {
                logger.error(e);
                throw e;
            }
        }

        public void publish(String topic, String message,mqttclient.proxies.qos QoS) throws MqttException {
            logger.info(String.format("MqttConnection.publish: %s, %s, %s", topic, message, this.client.getClientId()));
            try {
                MqttMessage payload = new MqttMessage(message.getBytes());
                int subscriptionQos = 0;
                if(QoS.equals(mqttclient.proxies.qos.At_Most_Once_0)){
                	subscriptionQos = 0;
                }else if(QoS.equals(mqttclient.proxies.qos.At_Least_Once_1)){
                	subscriptionQos= 1;
                }else if(QoS.equals(mqttclient.proxies.qos.Exactly_Once_2)){
                	subscriptionQos= 2;
                }
                payload.setQos(subscriptionQos);
                this.client.publish(topic, payload);
                logger.info("Message published");
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }
        }
    }
}
