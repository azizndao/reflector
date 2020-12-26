package me.abdou.reflector.exceptions;

public class NoPrimaryKeyException extends OrmException {
  public NoPrimaryKeyException(String name) {
    super(name + " must have a field annotated @PrimaryKey");
  }
}
