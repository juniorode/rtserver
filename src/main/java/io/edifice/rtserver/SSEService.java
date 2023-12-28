package io.edifice.rtserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.currentTimeMillis;

@Slf4j
@Service
@ConditionalOnProperty(name = "sse.enabled", havingValue = "true")
public class SSEService {

  private final Map<String, Set<SseEmitter>> emittersByResourceUri;

  public SSEService() {
    this.emittersByResourceUri = new HashMap<>();
  }

  public SseEmitter createNewEmitter(final String resource) {
    log.info("Creating new emitter for resource: {}", resource);
    final SseEmitter emitter = new SseEmitter();
    emitter.onCompletion(() -> this.destroyEmitter(resource, emitter));
    emitter.onTimeout(() -> this.destroyEmitter(resource, emitter));
    emitter.onError(th -> {
      log.error("Error occurred for emitter on resource: {}", resource, th);
      this.destroyEmitter(resource, emitter);
    });
    this.emittersByResourceUri.computeIfAbsent(resource, r -> new HashSet<>()).add(emitter);
    return emitter;
  }

  public void destroyEmitter(final String resource, final SseEmitter emitter) {
    log.error("Destroying emitter for resource: {}", resource);
    this.emittersByResourceUri.get(resource).remove(emitter);
  }

  public void sendEventToAllEmitters(final String resource, final String payload) {
    final Set<SseEmitter> emitters = this.emittersByResourceUri.get(resource);
    if(emitters != null) {
      SseEmitter.SseEventBuilder event = SseEmitter.event()
          .data(payload)
          .id(String.valueOf(currentTimeMillis()))
          .name("sse event - mvc");
      emitters.forEach(emitter -> {
        try {
          emitter.send(event);
        } catch (Exception ex) {
          emitter.completeWithError(ex);
        }
      });
    }
  }

  public int getNbOfConnections() {
    return this.emittersByResourceUri.values().stream().mapToInt(Set::size).sum();
  }
}
