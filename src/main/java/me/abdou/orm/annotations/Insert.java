package me.abdou.orm.annotations;

import me.abdou.orm.utils.OnConflictStrategy;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {
  OnConflictStrategy onconflictStrategy() default OnConflictStrategy.FAIL;
}
