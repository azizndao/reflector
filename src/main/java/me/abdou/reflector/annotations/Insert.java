package me.abdou.reflector.annotations;

import me.abdou.reflector.utils.OnConflict;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {
  OnConflict onConflict() default OnConflict.FAIL;
}
