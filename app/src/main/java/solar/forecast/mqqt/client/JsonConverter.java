package solar.forecast.mqqt.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonConverter {

  public static String convertJsonToText(String jsonResponse) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(jsonResponse);
      return jsonNode.toString();
    } catch (IOException e) {
      e.printStackTrace();
      return "Error converting JSON to text";
    }
  }
}
