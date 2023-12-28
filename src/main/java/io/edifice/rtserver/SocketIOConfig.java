package io.edifice.rtserver;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DisconnectListener;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Component
@Slf4j
@ConditionalOnProperty(name = "ws.enabled", havingValue = "true")
public class SocketIOConfig {
  private final String socketHost;
  private final int socketPort;
  private SocketIOServer server;

  public SocketIOConfig( @Value("${ws.host}") final String socketHost,
                         @Value("${ws.port}") final int socketPort) {
    this.socketHost = socketHost;
    this.socketPort = socketPort;
  }

  @Bean
  public SocketIOServer socketIOServer() {
    Configuration config = new Configuration();
    config.setHostname(socketHost);
    config.setPort(socketPort);
    final SocketConfig socketConfig = new SocketConfig();
    socketConfig.setReuseAddress(true);
    config.setSocketConfig(socketConfig);
    server = new SocketIOServer(config);
    server.start();
    server.addConnectListener(client -> log.info("new user connected with socket " + client.getSessionId()));

    server.addDisconnectListener(new DisconnectListener() {
      @Override
      public void onDisconnect(SocketIOClient client) {
        client.getNamespace().getAllClients().forEach(data-> {
          log.info("user disconnected "+data.getSessionId().toString());});
      }
    });
    return server;
  }

  @PreDestroy
  public void stopSocketIOServer() {
    this.server.stop();
  }
}
