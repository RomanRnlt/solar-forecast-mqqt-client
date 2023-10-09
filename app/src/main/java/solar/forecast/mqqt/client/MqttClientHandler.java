package solar.forecast.mqqt.client;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class MqttClientHandler {

  private MqttClient client;
  private CountDownLatch latch;
  private MessageCallback messageCallback;

  public MqttClientHandler(String broker, String clientId) throws MqttException {
    MemoryPersistence persistence = new MemoryPersistence();
    this.client = new MqttClient(broker, clientId, persistence);
  }

  public void connectAndSubscribe(String topic, MessageCallback callback) throws MqttException {
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);

    System.out.println("Connecting to broker: " + client.getServerURI());
    client.connect(connOpts);
    System.out.println("Connected");

    latch = new CountDownLatch(1);

    // Subscribe to the response from the server
    client.subscribe(topic, (t, responseMessage) -> {
      String receivedResponse = new String(responseMessage.getPayload());
      System.out.println("Received response from server: " + receivedResponse);
      outputController(receivedResponse);
      // Release the latch to allow the program to exit
      latch.countDown();

      // Call the custom callback
      if (messageCallback != null) {
        messageCallback.handleMessage(t, responseMessage);
      }
    });

    this.messageCallback = callback;
  }

  public void publishMessage(String topic, String message) throws MqttException {
    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
    mqttMessage.setQos(2);

    System.out.println("Publishing message to server: " + message);
    client.publish(topic, mqttMessage);
  }

  public void waitForResponse(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
    if (!latch.await(timeout, unit)) {
      System.out.println("No response received from the server in the last " + timeout + " " + unit.toString());
    }
  }

  public void disconnect() {
    try {
      client.disconnect();
      System.out.println("Disconnected");
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public static void outputController(String jsonResponse) {
    String outputDir = "/Users/roman/Downloads";

    try {
      JSONObject jsonData = new JSONObject(jsonResponse);

      createCharts(jsonData, outputDir, "watts");
      createCharts(jsonData, outputDir, "watt_hours_period");
      createCharts(jsonData, outputDir, "watt_hours");
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
  }

  private static JSONObject readFile(String filePath) throws IOException {
    FileReader fileReader = new FileReader(filePath);
    JSONTokener jsonTokener = new JSONTokener(fileReader);
    return new JSONObject(jsonTokener);
  }

  private static void createCharts(JSONObject jsonData, String outputDir, String key)
      throws ParseException, IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    XYSeries series = new XYSeries(key);

    JSONObject data = jsonData.getJSONObject("result").getJSONObject(key);
    for (String timestamp : data.keySet()) {
      Date date = dateFormat.parse(timestamp);
      double value = data.getDouble(timestamp);
      series.add(date.getTime(), value);
    }

    XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(
        key + " Diagramm",
        "Datum und Uhrzeit",
        key,
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false);

    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
    renderer.setSeriesPaint(0, Color.BLUE);
    plot.setRenderer(renderer);

    DateAxis xAxis = new DateAxis("Datum und Uhrzeit");
    xAxis.setDateFormatOverride(new SimpleDateFormat("MM.dd HH:mm")); // Hier kannst du das Datumsformat anpassen
    plot.setDomainAxis(xAxis);

    File chartFile = new File(outputDir + "/" + key + "_diagram.png");
    ChartUtilities.saveChartAsPNG(chartFile, chart, 800, 400);

    System.out.println(key + " Diagramm wurde in " + chartFile.getAbsolutePath() + " gespeichert.");
  }
}
