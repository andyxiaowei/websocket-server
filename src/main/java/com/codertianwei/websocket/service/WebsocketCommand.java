package com.codertianwei.websocket.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface WebsocketCommand {
    String value() default "";
}
