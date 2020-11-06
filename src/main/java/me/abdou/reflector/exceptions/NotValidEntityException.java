package me.abdou.reflector.exceptions;

public class NotValidEntityException extends OrmException {
  public NotValidEntityException(String name) {
    super(name + " must be annotated @EntityInfo to be a valid entity");
  }
}
