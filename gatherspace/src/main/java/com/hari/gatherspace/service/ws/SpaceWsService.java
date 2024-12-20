package com.hari.gatherspace.service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hari.gatherspace.model.ws.UserWs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SpaceWsService {

    // Stores rooms and the users within them
    private final Map<String, List<UserWs>> rooms = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Method to add a user to a room
    public void addUserToRoom(String spaceId, UserWs user) {
        rooms.computeIfAbsent(spaceId, k -> new java.util.ArrayList<>()).add(user);
        user.setSpaceId(spaceId);
        System.out.println("User " + user.getId() + " added to room " + spaceId);
    }

    // Method to remove a user from a room
    public void removeUserFromRoom(UserWs user, String spaceId) {
        if (rooms.containsKey(spaceId)) {
            rooms.get(spaceId).removeIf(u -> u.getId().equals(user.getId()));
            if (rooms.get(spaceId).isEmpty()) {
                rooms.remove(spaceId); // Remove the room if it's empty
            }
            System.out.println("User " + user.getId() + " removed from room " + spaceId);
        }
    }

    // Method to broadcast a message to all users in a room except the sender
    public void broadcast(Object messagePayload, UserWs sender, String roomId) {
        if (rooms.containsKey(roomId)) {
            String messageJson;
            try {
                messageJson = objectMapper.writeValueAsString(messagePayload);
                for (UserWs user : rooms.get(roomId)) {
                    if (!user.getId().equals(sender.getId())) {
                        user.send(messageJson);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error broadcasting message to room " + roomId + ": " + e.getMessage());
            }
        }
    }

    // Get all users in a specific room
    public List<UserWs> getUsersInRoom(String spaceId) {
        return rooms.getOrDefault(spaceId, java.util.Collections.emptyList());
    }
}