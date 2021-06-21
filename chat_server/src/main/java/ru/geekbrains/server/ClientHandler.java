package ru.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private ru.geekbrains.server.Server server;
    private Socket socket;
    private String username;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(ru.geekbrains.server.Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String inputMessage = in.readUTF();
                        if (inputMessage.startsWith("/auth ")) {
                            String[] tokens = inputMessage.split("\\s+");
                            if(tokens.length == 1){
                                sendMessage("Имя пользователя не указано!");
                                continue;
                            }
                            if(tokens.length > 2){
                                sendMessage("Имя пользователя не должно содержать несколько слов!");
                                continue;
                            }
                            String newUsername = tokens[1];
                            if(server.isUsernameUsed(newUsername)){
                                sendMessage("Имя используется!");
                                continue;
                            }
                            username = newUsername;
                            sendMessage("/authok");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage("SERVER: Вам необходимо авторизоваться");
                        }
                    }
                    while (true) {
                        String inputMessage = in.readUTF();
                        if (inputMessage.startsWith("/")) {
                            if(inputMessage.equals("/exit")){
                                sendMessage("/exit");
                                break;
                            }
                            continue;
                        }
                        server.broadcastMessage(username + ": " + inputMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(username + " вышел из чата!");
                    server.unsubscribe(this);
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
    public void closeConnection(){
        try {
            if(in != null){
                in.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            if(out != null){
                out.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
