package cn.sym.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionUtil {

    private static Map<Long, String> userSessionMap;
    private static SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostConstruct
    public void init() {
        userSessionMap = new HashMap<>();
        messagingTemplate = simpMessagingTemplate;
    }

    public static void addSession(Long userId, String sessionId) {
        userSessionMap.put(userId, sessionId);
    }

    public static void removeSession(Long userId, String sessionId) {
        if (userSessionMap.containsKey(userId) && userSessionMap.get(userId).equals(sessionId)) {
            userSessionMap.remove(userId);
        }
    }

    public static boolean isLoggedIn(Long userId) {
        return userSessionMap.containsKey(userId);
    }

    public static boolean isUserOnline(Long userId) {
        return userSessionMap.containsKey(userId);
    }

    public static void sendMessage(Long userId, String message) {
        if (isUserOnline(userId)) {
            messagingTemplate.convertAndSend("/topic/user/" + userId, message);
        }
    }
}