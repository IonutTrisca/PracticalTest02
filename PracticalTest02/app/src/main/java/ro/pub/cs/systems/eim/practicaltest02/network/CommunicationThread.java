package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }

        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            String data = bufferedReader.readLine();
            String[] splitData = data.split(",");
            String output = "done";
            String pageSourceCode = "";
            if (splitData[0].equals("put")) {
                serverThread.getData().put(splitData[1], splitData[2]);
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Key/Value added!");

                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                JSONObject content = new JSONObject(pageSourceCode);

                Integer time = content.getInt("unixtime");
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Time " + time);
                serverThread.getTimeData().put(splitData[1], time);

            } else if (splitData[0].equals("get")) {
                output = serverThread.getData().get(splitData[1]);
                if (output == null || System.currentTimeMillis() / 1000L > serverThread.getTimeData().get(splitData[1]) + 60 ) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Key does not exist!");
                    output = "none";
                }
            }
            printWriter.println(output);
            printWriter.flush();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
