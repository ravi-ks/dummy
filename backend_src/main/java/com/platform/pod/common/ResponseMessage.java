package com.platform.pod.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage {
    private String message;

    public ResponseMessage(Boolean val) {
        message = val.toString();
    }
}



