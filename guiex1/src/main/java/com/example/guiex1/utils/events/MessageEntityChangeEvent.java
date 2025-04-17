package com.example.guiex1.utils.events;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.Utilizator;

public class MessageEntityChangeEvent implements Event {
    private ChangeEventType type;
    private Message data, oldData;

    public MessageEntityChangeEvent(ChangeEventType type, Message data) {
        this.type = type;
        this.data = data;
    }
    public MessageEntityChangeEvent(ChangeEventType type, Message data, Message oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Message getData() {
        return data;
    }

    public Message getOldData() {
        return oldData;
    }
}
