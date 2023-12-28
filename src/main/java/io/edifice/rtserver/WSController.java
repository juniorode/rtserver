package io.edifice.rtserver;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@ConditionalOnProperty(name = "ws.enabled", havingValue = "true")
public class WSController {

  private final SocketIOServer server;

  public WSController(SocketIOServer server) {
    this.server = server;
    this.server.addConnectListener(onUserConnectWithSocket);
    this.server.addDisconnectListener(onUserDisconnectWithSocket);

    /**
     * Here we create only one event listener
     * but we can create any number of listener
     * messageSendToUser is socket end point after socket connection user have to send message payload on messageSendToUser event
     */
    this.server.addEventListener("new-message", WSMessage.class, onSendMessage);

  }
  public ConnectListener onUserConnectWithSocket = new ConnectListener() {
    @Override
    public void onConnect(SocketIOClient client) {
      final Map<String, List<String>> urlParams = client.getHandshakeData().getUrlParams();
      if(!urlParams.containsKey("resource")) {
        log.warn("user does not try to connect to a specific resource");
        client.disconnect();
        return;
      }
      final List<String> resources = urlParams.get("resource");
      if(resources.size() != 1) {
        log.warn("user try to connect to more than one resource");
        client.disconnect();
        return;
      }
      final String resource = resources.getFirst();
      log.info("new user connected to resource " + resource);
      client.joinRoom(resource);
    }
  };


  public DisconnectListener onUserDisconnectWithSocket = client -> log.info("Perform operation on user disconnect in controller");

  public DataListener<WSMessage> onSendMessage = new DataListener<>() {
    @Override
    public void onData(SocketIOClient client, WSMessage message, AckRequest ackRequest) {

      log.info(message.senderName() + " user send message about " + message.resource() + " and message is " + message.payload());
      server.getRoomOperations(message.resource()).sendEvent("resource-update", client, "received message " + message.payload());
      ackRequest.sendAckData("WSMessage send to target user successfully");
    }
  };


}
