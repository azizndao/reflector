package me.abdou.reflector.utils;

import me.abdou.reflector.Database;
import me.abdou.reflector.annotations.ColumnInfo;
import me.abdou.reflector.annotations.ForeignKey;
import me.abdou.reflector.annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.StringJoiner;

public class Column {

  public static final Database DATABASE = Database.getInstance();
  private final Field field;

  public Column(Field field) {
    this.field = field;
  }

  public String getName() {
    if (field.isAnnotationPresent(ColumnInfo.class)) {
      var name = getColumnInfo().name();
      if (!name.equals("None"))
        return name;
    }
    return field.getName();
  }


  public String getCreateQuery() {
    var query = new StringJoiner(" ")
        .add(getName()).add(getSqlType());
    if (isPrimaryKey()) query.add("PRIMARY KEY");
    if (isAutoIncremented()) {
      var dbType = DATABASE.getSGBD();
      if (dbType.equals("mysql")) query.add("AUTO_INCREMENT");
      else if (dbType.equals("sqlite")) query.add("AUTOINCREMENT");
    }
    if (!isNullable()) query.add("NOT NULL");
    if (isUnique()) query.add("UNIQUE");
    return query.toString();
  }

  public String getTargetTableName() {
    var type = field.getType();
    return TableUtils.getTableName(type);
  }

  public String getTargetFieldName() {
    if (isForeignKey()) {
      if (field.getType() != List.class) {
        Column primaryKey = TableUtils.getPrimaryKey(field.getType());
        assert primaryKey != null;
        return primaryKey.getName();
      }
    }
    return null;
  }

  public String getCreateForeignKeyQuery() {
    ForeignKey ann = field.getAnnotation(ForeignKey.class);
    var onDeleteStrategy = ann.onDelete();
    var onUpdateStrategy = ann.onUpdate();
    var tableName = TableUtils.getTableName(field.getDeclaringClass());
    if (DATABASE.getSGBD().equals("sqlite")) {
      return String.format(
          "ALTER TABLE %s ADD COLUMN %s %s CONSTRAINT %s_%s_fk  REFERENCES %s(%s) ON DELETE %s ON UPDATE %s;",
          tableName, getName(), getSqlType(), tableName, getName(), getTargetTableName(),
          getTargetFieldName(), onDeleteStrategy, onUpdateStrategy
      );
    } else if (DATABASE.getSGBD().equals("mysql")) {
      return String.format(
          "ALTER TABLE %s ADD COLUMN %s %s, ADD CONSTRAINT %s_%s_fk FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE %s ON UPDATE %s;",
          tableName, getName(), getSqlType(), tableName, getName(), getName(), getTargetTableName(),
          getTargetFieldName(), onDeleteStrategy, onUpdateStrategy
      );
    }
    return null;
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

  private String getSqlType() {
    if (field.isAnnotationPresent(ColumnInfo.class)) {
      var customType = getColumnInfo().type();
      if (!customType.equals("None")) return customType;
    }
    var type = isForeignKey() ? getTargetField() : field.getType();
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

  private Class<?> getTargetField() {
    return getTargetColumn(field.getType()).field.getType();
  }

  public boolean isForeignKey() {
    return field.isAnnotationPresent(ForeignKey.class);
  }

  public <T> Object getSqlValue(T item) {
    if (isForeignKey()) {
      var owner = getValue(item);
      Column targetColumn = getTargetColumn(owner.getClass());
      return targetColumn.getValue(owner);
    }
    return getValue(item);
  }

  private Column getTargetColumn(Class<?> clazz) {
    return TableUtils.getPrimaryKey(clazz);
  }

  public <T> Object getValue(T item) {
    try {
      field.setAccessible(true);
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
      else if (type == LocalDateTime.class) {
        var text = ((String) value).replace(" ", "T");
        var dateTime = LocalDateTime.parse(text);
        field.set(owner, dateTime);
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
