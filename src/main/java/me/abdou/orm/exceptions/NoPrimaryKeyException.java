package me.abdou.orm.exceptions;

public class NoPrimaryKeyException extends OrmException {
  public NoPrimaryKeyException(String name) {
    super(name + " must have a field annotated @PrimaryKey");
  }
}
