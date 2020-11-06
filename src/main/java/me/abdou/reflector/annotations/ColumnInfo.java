package me.abdou.reflector.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnInfo {
  String name() default "None";

  String type() default "None";

  boolean unique() default false;

  boolean nullable() default false;
}
