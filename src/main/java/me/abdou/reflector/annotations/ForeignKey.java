package me.abdou.reflector.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.abdou.reflector.utils.OnReferenceChange;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
  OnReferenceChange onDelete() default OnReferenceChange.CASCADE;

  OnReferenceChange onUpdate() default OnReferenceChange.CASCADE;
}
