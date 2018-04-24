/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Faigjaz
 */
public class Server {

    private static ArrayList<Message> messages = new ArrayList();
    private boolean isRunning = false;
    private int port;
    private static final String REQUEST_PATH = "/message";

    public Server(int port) {
        this.port = port;

    }

    public static void addMessage(String messageString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject messageJSON = (JSONObject) parser.parse(messageString);
        Date timeStamp = new Date((long) messageJSON.get("timeStamp"));
        Message newMessage = new Message(
                (String) messageJSON.get("userName"),
                (String) messageJSON.get("message"),
                timeStamp);
        messages.add(newMessage);
    }

    public static byte[] createResponseBody() {
        JSONArray messages = new JSONArray();
        for (int iMessage = 0; iMessage < Server.messages.size(); iMessage++) {
            Message currMessage = Server.messages.get(iMessage);
            JSONObject message = new JSONObject();
            message.put("userName", currMessage.userName);
            message.put("message", currMessage.message);
            message.put("timeStamp", currMessage.timeStamp.getTime());
            messages.add(message);
        }
        return messages.toJSONString().getBytes();
    }

    public static String getBufferFromBody(InputStreamReader streamReader) throws IOException {
        BufferedReader buffReader = new BufferedReader(streamReader);
        int charAt;
        StringBuilder buffer = new StringBuilder(512);
        while ((charAt = buffReader.read()) != -1) {
            buffer.append((char) charAt);
        }

        buffReader.close();
        streamReader.close();
        return buffer.toString();
    }

    public static void sendResponse(HttpExchange httpExchange, int statusCode, byte[] response) throws IOException {
        try {
            httpExchange.sendResponseHeaders(statusCode, response.length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response);
            os.close();
        } catch (IOException err) {
            throw err;
        }
    }

    static class MessageHandler implements HttpHandler {

        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            URI requestURI = httpExchange.getRequestURI();
            System.out.println(requestURI.getPath());
            try {
                if (requestURI.getPath() == null ? REQUEST_PATH == null : requestURI.getPath().equals(REQUEST_PATH)) {
                    if (method.equalsIgnoreCase("POST")) {
                        String messageString = Server.getBufferFromBody(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"));
                        Server.addMessage(messageString);

                        byte[] response = Server.createResponseBody();
                        Server.sendResponse(httpExchange, 200, response);

                    } else if (method.equalsIgnoreCase("GET")) {
                        byte[] response = Server.createResponseBody();
                        Server.sendResponse(httpExchange, 200, response);

                    } else {
                        byte[] response = "Not implemented".getBytes();
                        Server.sendResponse(httpExchange, 501, response);
                    }
                } else {
                    byte[] response = "No context found for request".getBytes();
                    Server.sendResponse(httpExchange, 404, response);
                }
            } catch (IOException | ParseException err) {
                if (err instanceof ParseException) {
                    byte[] response = "Could not parse JSON-Body".getBytes();
                    Server.sendResponse(httpExchange, 400, response);

                } else if (err instanceof IOException) {
                    byte[] response = "Could not Parse JSON-Body".getBytes();
                    Server.sendResponse(httpExchange, 500, response);
                }
            }
        }
    }

    public void start() throws IOException {
        try {
            if (this.isRunning) {
                return;
            }
            HttpServer server = HttpServer.create(new InetSocketAddress(this.port), 0);
            server.createContext(REQUEST_PATH, new MessageHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            this.isRunning = true;
        } catch (IOException err) {
            this.isRunning = false;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public int getPort() {
        return port;
    }
}
