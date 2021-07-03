package com.geekq.miaosha.order.init;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface RunningAfterStartUp {

}
