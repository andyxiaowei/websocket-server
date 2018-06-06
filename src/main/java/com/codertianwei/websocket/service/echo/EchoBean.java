package com.codertianwei.websocket.service.echo;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class EchoBean {
    private String message;
}
