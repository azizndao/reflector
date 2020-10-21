package me.abdou.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.abdou.orm.utils.OnReferenceChangeStrategy;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
  String target();

  OnReferenceChangeStrategy onDelete() default OnReferenceChangeStrategy.CASCADE;

  OnReferenceChangeStrategy onUpdate() default OnReferenceChangeStrategy.CASCADE;
}
