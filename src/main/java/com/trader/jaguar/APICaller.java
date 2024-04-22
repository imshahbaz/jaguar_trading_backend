import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class APICaller {
    public static void main(String[] args) {
        // API endpoints
        String firstAPIEndpoint = "https://jaguar-trading-py.onrender.com/hello";
        String secondAPIEndpoint = "https://jaguar-trading-py.onrender.com/publishMessage";

        // Call the first API until it returns 200 status code
        boolean firstAPIReached = false;
        while (!firstAPIReached) {
            try {
                URL url = new URL(firstAPIEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                
                if (responseCode == 200) {
                    System.out.println("First API call successful (status code 200)");
                    firstAPIReached = true;
                } else {
                    System.out.println("First API call unsuccessful, status code: " + responseCode + ", retrying...");
                    // Add some delay before retrying, e.g., Thread.sleep(1000);
                }
            } catch (IOException e) {
                System.out.println("Error occurred while calling the first API: " + e.getMessage());
                // Add exception handling
            }
        }

        // Call the second API
        try {
            URL url = new URL(secondAPIEndpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                System.out.println("Second API call successful (status code 200)");
            } else {
                System.out.println("Second API call unsuccessful, status code: " + responseCode);
                // Handle other status codes if needed
            }
        } catch (IOException e) {
            System.out.println("Error occurred while calling the second API: " + e.getMessage());
            // Add exception handling
        }
    }
}

