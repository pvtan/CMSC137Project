import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.util.Iterator;

public class UDPServer implements Runnable {
    String playerData;
    int connectedPlayers = 0;
    DatagramSocket serverDatagramSocket = null;
    GameState gameState;
    int stage = 3; //public final int WAITING_FOR_PLAYERS=3;
    int playerLimit;
    Thread thread = new Thread(this);

    public UDPServer (int playerLimit) {
        this.playerLimit = playerLimit;

        try {
            serverDatagramSocket = new DatagramSocket(4444);
            serverDatagramSocket.setSoTimeout(7000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        gameState = new GameState();
        thread.start();
    }

    public void broadcast(String message) { //broadcast data to all players
        //iterate through players from getGamePlayers method of GameState
        for(Iterator i = gameState.getGamePlayers().keySet().iterator(); i.hasNext();){
			String playerName = (String) i.next();
			GamePlayer player = (GamePlayer) gameState.getGamePlayers().get(playerName);			
			sendMessage(player, message);	
		}
    }

    public void sendMessage(GamePlayer gamePlayer, String message) { //send message to a player
        byte buffer[] = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, gamePlayer.getInetAddress(), gamePlayer.getPortNumber());
        try {
            serverDatagramSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // packet for receiving packets with lengthf buffer
            try {
                serverDatagramSocket.receive(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            playerData = new String(buffer);
            playerData = playerData.trim();
            System.out.println("playerData = " + playerData);
            System.out.println("stage = " + stage);

            switch(stage) {
                case 3: //if waiting for players
                    if(playerData.startsWith("CONNECT")) {
                        String playerDataTokens[] = playerData.split(" "); //split playerData by space
                        System.out.println("playerDataToken[1] = " + playerDataTokens[1]);                        
                        GamePlayer player = new GamePlayer(playerDataTokens[1], packet.getAddress(), packet.getPort()); //instantiate new player
                        gameState.update(playerDataTokens[1].trim(), player); //add to player hashmap
                        System.out.println("Player connected: " + playerDataTokens[1]);                        
                        broadcast("CONNECTED " + playerDataTokens[1]);
                        connectedPlayers++;
                        if (connectedPlayers == playerLimit){
                            stage = 0; //public static final int GAME_START=0;
                        }
                    }
                    break;
                case 0: //GAME_START
                    System.out.println("Game State: START");
                    broadcast("START");
                    stage = 1; //IN_PROGRESS
                    break;
                case 1: //IN_PROGRESS
                    if (playerData.startsWith("PLAYER")){
                        String[] playerPosition = playerData.split(" ");	 //Tokenize:				  
                        String playerName = playerPosition[1]; //The format: PLAYER <player name> <x> <y>
                        int xPosition = Integer.parseInt(playerPosition[2].trim());
                        int yPosition = Integer.parseInt(playerPosition[3].trim());
                        GamePlayer player = (GamePlayer) gameState.getGamePlayers().get(playerName);	//Get the player from the game state				  
                        player.setX(xPosition);
                        player.setY(yPosition);
                        gameState.update(playerName, player); //Update current player in hashmap of players
                        broadcast(gameState.gameToString()); //Send to all the updated game state
					}
					break;
            }
        }
    }
}