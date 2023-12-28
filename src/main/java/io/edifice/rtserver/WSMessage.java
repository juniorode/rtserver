package io.edifice.rtserver;


public record WSMessage(String senderName, String resource, String payload) {
}
