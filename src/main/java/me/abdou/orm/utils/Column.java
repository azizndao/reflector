package me.abdou.orm.utils;


import me.abdou.orm.annotations.ColumnInfo;
import me.abdou.orm.annotations.ForeignKey;
import me.abdou.orm.annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Column {

  private final Field field;

  public Column(Field field) {
    this.field = field;
  }

  public String getName() {
    if (field.isAnnotationPresent(ColumnInfo.class)) {
      var name = getColumnInfo().name();
      if (!name.equals("Noe")) return name;
    }
    return field.getName();
  }

  public String getCreateQuery() throws NoSuchFieldException {
    var query = new StringBuilder(getName()).append(" ").append(getSqlType());
    if (!isNullable()) query.append(" NOT NULL");
    if (isPrimaryKey()) query.append(" PRIMARY KEY");
    if (isAutoIncremented()) query.append(" AUTOINCREMENT");
    if (isUnique()) query.append(" UNIQUE");
    return query.toString();
  }

  public String getTargetTableName() {
    var type = field.getType();
    return TableUtils.getTableName(type);
  }

  public String getTargetFieldName() {
    if (isForeignKey()) {
      return field.getAnnotation(ForeignKey.class).target();
    }
    return null;
  }

  public Field getTargetField() throws NoSuchFieldException {
    if (isForeignKey()) {
      var target = field.getAnnotation(ForeignKey.class).target();
      return field.getType().getDeclaredField(target);
    }
    return null;
  }

  public String getCreateForeignKeyQuery() {
    return String.format(
        "FOREIGN KEY (%s) REFERENCES %s(%s)",
        getName(), getTargetTableName(), getTargetFieldName()
    );
  }

  private boolean isUnique() {
    if (getColumnInfo() != null) return getColumnInfo().unique();
    return false;
  }

  private ColumnInfo getColumnInfo() {
    return field.getAnnotation(ColumnInfo.class);
  }

  private boolean isNullable() {
    if (getColumnInfo() != null) return getColumnInfo().nullable();
    return false;
  }

  public boolean isAutoIncremented() {
    return isPrimaryKey() && field.getAnnotation(PrimaryKey.class).autoIncremented();
  }

  public boolean isPrimaryKey() {
    return field.isAnnotationPresent(PrimaryKey.class);
  }

  private String getSqlType() throws NoSuchFieldException {
    if (field.isAnnotationPresent(ColumnInfo.class)) {
      var customType = getColumnInfo().type();
      if (!customType.equals("None")) return customType;
    }
    var type = isForeignKey() ? getTargetField().getType() : field.getType();
    if (int.class == type || Integer.class == type) return "INTEGER";
    else if (long.class == type || Long.class == type) return "BIGINT";
    else if (float.class == type || Float.class == type) return "FLOAT";
    else if (double.class == type || Double.class == type) return "DOUBLE";
    else if (short.class == type || Short.class == type) return "SMALLINT";
    else if (String.class == type) return "VARCHAR(255)";
    else if (char.class == type || Character.class == type) return "VARCHAR(255)";
    else if (Date.class == type) return "DATE";
    else if (Time.class == type) return "TIME";
    else if (LocalDate.class == type) return "VARCHAR(10)";
    else if (LocalTime.class == type) return "VARCHAR(18)";
    else if (LocalDateTime.class == type) return "VARCHAR(29)";
    else return "VARCHAR(255)";
  }

  public boolean isForeignKey() {
    return field.isAnnotationPresent(ForeignKey.class);
  }

  public <T> Object getSqlValue(T item) throws NoSuchFieldException {
    if (isForeignKey()) {
      return new Column(getTargetField()).getValue(getValue(item));
    }
    return getValue(item);
  }

  public <T> Object getValue(T item) {
    field.setAccessible(true);
    try {
      return field.get(item);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  public <T, U> void setValue(T owner, U value) {
    if (value == null) return;
    try {
      field.setAccessible(true);
      var type = field.getType();
      if (type == int.class || type == Integer.class) field.setInt(owner, (Integer) value);
      else if (type == long.class || type == Long.class) field.setLong(owner, (Long) value);
      else if (type == float.class || type == Float.class) field.setFloat(owner, (Float) value);
      else if (type == double.class || type == Double.class) field.setDouble(owner, (Double) value);
      else if (type == short.class || type == Short.class) field.setShort(owner, (Short) value);
      else if (type == String.class) field.set(owner, value);
      else if (type == char.class || type == Character.class) field.set(owner, value);
      else if (type == Date.class) field.set(owner, value);
      else if (type == Time.class) field.set(owner, value);
      else if (type == LocalDate.class) field.set(owner, LocalDate.parse((String) value));
      else if (type == LocalTime.class) field.set(owner, LocalTime.parse((String) value));
      else if (type == LocalDateTime.class) field.set(owner, LocalDateTime.parse((String) value));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
