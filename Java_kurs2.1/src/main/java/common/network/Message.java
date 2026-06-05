package main.java.common.network;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String type;
    private final Object data;
    
    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    public String getType() { return type; }
    public Object getData() { return data; }
}