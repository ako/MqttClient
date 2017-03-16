package mqttclient.impl;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ako on 1/9/2016.
 * Edited by simo101 on 16/03/2017
 */
public class MqttConnector {
    private static Map<String, MqttConnection> mqttHandlers;
    private ILogNode logger;

    public MqttConnector(ILogNode logger) {
        this.logger = logger;
        if (mqttHandlers == null) {
            mqttHandlers = new HashMap();
        }
    }

    public void subscribe(String brokerHost, Long brokerPort, String topicName, String onMessageMicroflow, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password) throws Exception {
        logger.info("MqttConnector.subscribe");       
        MqttConnection connection = getMqttConnection(brokerHost, brokerPort, CA, ClientCertificate, ClientKey, CertificatePassword, username, password);
        try{
      	  connection.subscribe(topicName, onMessageMicroflow);
      }catch(MqttException exception){
      	if(exception.getReasonCode() == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED){
      		connection.reconnect();
      		connection.subscribe(topicName, onMessageMicroflow);
      	}else{
      		logger.error(exception);
      		throw exception;
      	}
      	
      }
        
        
        
    }

    public void publish(String brokerHost, Long brokerPort, String topicName, String message, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password) throws Exception {
    	logger.info("MqttConnector.publish");
    	MqttConnection connection = getMqttConnection(brokerHost, brokerPort, CA, ClientCertificate, ClientKey, CertificatePassword, username, password);
        try{
        	connection.publish(topicName, message);
        }catch(MqttException exception){
        	if(exception.getReasonCode() == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED){
        		connection.reconnect();
        		connection.publish(topicName, message);
        	}else{
        		logger.error(exception);
        		throw exception;
        	}
        	
        }
    }

    private MqttConnection getMqttConnection(String brokerHost, Long brokerPort, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password) throws Exception {
        String key = brokerHost + ":" + brokerPort;
        MqttConnection handler;
        synchronized (mqttHandlers) {
            logger.info("NUmber of objects in mqttHandlers map: " + mqttHandlers.size());

            if (!mqttHandlers.containsKey(key)) {
                logger.info("creating new MqttConnection");
                try {
                    handler = new MqttConnection(logger, brokerHost, brokerPort, CA, ClientCertificate, ClientKey, CertificatePassword, username, password);
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

    public void unsubscribe(String brokerHost, Long brokerPort, String topicName) throws Exception {
        MqttConnection connection = getMqttConnection(brokerHost, brokerPort, null, null, null, null, null, null);
        connection.unsubscribe(topicName);
    }


    private class MqttConnection {
        private ILogNode logger;
        private MqttClient client;
        private HashMap<String, MqttSubscription> subscriptions = new HashMap<>();
        private MqttConnectOptions connOpts;
        private MemoryPersistence persistence;
        private String broker;

        public MqttConnection(ILogNode logger, String brokerHost, Long brokerPort, String CA, String ClientCertificate, String ClientKey, String CertificatePassword, String username, String password) throws Exception {
            logger.info("new MqttConnection");

            this.logger = logger;

            String hostname = InetAddress.getLocalHost().getHostName();
            String xasId = Core.getXASId();
            String clientId = "MxClient_" + xasId + "_" + hostname + "_" + brokerHost + "_" + brokerPort;
            logger.info("new MqttConnection client id " + clientId);

            boolean useSsl = (ClientCertificate != null && !ClientCertificate.equals(""));
            broker = String.format("tcp://%s:%d", brokerHost, brokerPort);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);

            if (username != null && !username.equals("")) {
                connOpts.setUserName(username);
            }
            if (password != null && !password.equals("")) {
                connOpts.setPassword(password.toCharArray());
            }

            if (useSsl) {
                broker = String.format("ssl://%s:%d", brokerHost, brokerPort);
                //connOpts = new MqttConnectOptions();
                connOpts.setConnectionTimeout(60);
                connOpts.setKeepAliveInterval(60);
                connOpts.setCleanSession(true);

                try {
                    String resourcesPath = null;
                    try {
                        resourcesPath = Core.getConfiguration().getResourcesPath().getPath();
                        resourcesPath += File.separator;

                    } catch (Exception e) {
                        //testing mode?
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

            persistence = new MemoryPersistence();

            try {
                this.client = new MqttClient(broker, clientId, persistence);
                logger.info("Connecting to broker: " + broker);
                client.connect(connOpts);
                client.setCallback(new MxMqttCallback(logger, client, subscriptions));
                logger.info("Connected");
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }
        }

        public void finalize() {
            logger.info("finalize MqttConnection");
        }

        public boolean isSubscribed(String topic) {
            return subscriptions.containsKey(topic);

        }

        public void subscribe(String topic, String onMessageMicroflow) throws MqttException {
            logger.info(String.format("MqttConnection.subscribe: %s", client.getClientId()));
            try {
                if(!client.isConnected()){
                    client.reconnect();
                }

                // 0 - at most once
                // 1 - at least once
                // 2 - exactly once

                int subscriptionQos = 1;
                client.subscribe(topic, subscriptionQos);
                subscriptions.put(topic, new MqttSubscription(topic, onMessageMicroflow));
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }

        }

        public void publish(String topic, String message) throws MqttException {
            logger.info(String.format("MqttConnection.publish: %s, %s, %s", topic, message, client.getClientId()));
            try {
                MqttMessage payload = new MqttMessage(message.getBytes());
                int qos = 1;
                payload.setQos(qos);
                client.publish(topic, payload);
                logger.info("Message published");
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }
        }

        public void unsubscribe(String topicName) throws MqttException {
            logger.info(String.format("unsubscribe: %s, %s", topicName, client.getClientId()));
            try {
                client.unsubscribe(topicName);
            } catch (MqttException e) {
                logger.error(e);
                throw e;
            }
        }
        
        public void reconnect() throws Exception{
          	 try {
                   logger.info("Reconnecting to broker: " + broker);
                   client.connect(connOpts);
                   logger.info("Connected");
               } catch (Exception e) {
                   logger.error(e);
                   throw e;
               }
          }
          
          public void disconnect() throws Exception{
         	 try {
                  logger.info("Disconnecting to broker: " + broker);
                  client.disconnect();
                  logger.info("Disconnected");
              } catch (Exception e) {
                  logger.error(e);
                  throw e;
              }
         }
         public boolean isConnected(){
       	  return client.isConnected();
         }
        
        
        
    }
}
