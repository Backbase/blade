package com.backbase.oss.blade.utils;

import com.backbase.oss.blade.model.Blade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BladeUtils {

    private static final int MAX_PORT_NUMBER = 65535;
    private static final int MIN_PORT_NUMBER = 1024;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Log bladeLogger = LogFactory.getLog(BladeUtils.class);

    private BladeUtils() {
    }

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    @SuppressWarnings({"squid:S2093","java:S4818"})
    public static boolean isPortAvailable(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port, 50, InetAddress.getByName("localhost"));
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            /* should not be thrown */
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }
        return false;
    }

    public static synchronized void updateBladeMaster(Blade blade) {
        updateBladeStatus(blade.getBladeMaster() + "/blade/api/blades", blade);
    }

    public static synchronized void updateLocalBlade(Blade blade) {
        updateBladeStatus("http://localhost:" + blade.getPort() + "/blade/api/blades", blade);

    }

    private static synchronized void updateBladeStatus(String bladeServletUrl, Blade blade) {
        try {
            String json = BladeUtils.getBladeStatusAsJson(blade);
            postJson(json, new URL(bladeServletUrl));
        } catch (Exception e) {
            bladeLogger.info("Cannot send Blade Status to Blade Servlet: " + bladeServletUrl + " due to: " + e.getMessage());
        }
    }

    private static void postJson(String json, URL addBlade) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) addBlade.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");

        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.write(json.getBytes(StandardCharsets.UTF_8));
        output.close();
        output.flush();

        int responseCode = connection.getResponseCode();

        connection.disconnect();
        bladeLogger.debug("[" + responseCode + "] Sent Blade Status to Master Blade: " + addBlade + " --> " + json);
    }

    public static String getBladeStatusAsJson(Blade blade) throws JsonProcessingException {
        return objectMapper.writeValueAsString(blade);
    }

    public static Blade getBladeStatus(Blade blade) throws IOException {
        URL bladeStatusUrl = new URL("http://localhost:" + blade.getPort() + "/blade/api/status/" + blade.getId());
        return objectMapper.readValue(bladeStatusUrl, Blade.class);
    }


}
