package com.tripmoa.chat.utils;

public class ChatDestinationUtils {
    public static String roomDestination(Long roomId) {
        return "/sub/chat/room/" + roomId;
    }
}
