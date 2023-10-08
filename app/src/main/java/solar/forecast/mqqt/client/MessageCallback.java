package solar.forecast.mqqt.client;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MessageCallback {
  void handleMessage(String topic, MqttMessage message);
}