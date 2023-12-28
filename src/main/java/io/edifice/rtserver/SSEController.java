package io.edifice.rtserver;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@ConditionalOnProperty(name = "sse.enabled", havingValue = "true")
public class SSEController {

  private final SSEService SSEService;

  public SSEController(SSEService SSEService) {
    this.SSEService = SSEService;
  }

  @PostMapping("/sse/publish/{resource}")
  @ResponseBody
  public String ping(@PathVariable final String resource,
                     @RequestBody final String payload) {
    SSEService.sendEventToAllEmitters(resource, payload);
    return "pong";
  }

  @GetMapping("/sse/load")
  @ResponseBody
  public int getLoad() {
    return SSEService.getNbOfConnections();
  }

  @GetMapping("/sse/subscribe/{resource}")
  public SseEmitter streamSseMvc(@PathVariable String resource) {
    return SSEService.createNewEmitter(resource);
  }

}
