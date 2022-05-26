package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class ClientThread extends Thread {
    private String address;
    private int port;
    private String key;
    private String value;
    private TextView outputTextView;

    private boolean isPut;
    private Socket socket;

    public ClientThread(String address, int port, boolean isPut, String key, String value, TextView outputTextView) {
        this.address = address;
        this.port = port;
        this.key = key;
        this.isPut = isPut;
        this.value = value;
        this.outputTextView = outputTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            if (isPut) {
                printWriter.println("put," + key + ","+ value);
                printWriter.flush();
            } else {
                printWriter.println("get," + key);
                printWriter.flush();
            }
            String output;
            while ((output = bufferedReader.readLine()) != null) {
                final String finalizedOutput = output;
                outputTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        outputTextView.setText(finalizedOutput);
                    }
                });
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
