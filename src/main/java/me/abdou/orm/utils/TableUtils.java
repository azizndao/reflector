package me.abdou.orm.utils;

import me.abdou.orm.annotations.EntityInfo;
import me.abdou.orm.exceptions.NoPrimaryKeyException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class TableUtils {
  public static String createTable(Class<?> table) throws NoSuchFieldException, NoPrimaryKeyException {
    if (getPrimaryKey(table) == null) {
      throw new NoPrimaryKeyException(table.getName());
    }
    var tableName = getTableName(table);
    var query = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
    var fields = new StringJoiner(",");
    var foreignKeys = new StringJoiner(",");
    var declaredFields = table.getDeclaredFields();
    for (Field declaredField : declaredFields) {
      Column column = new Column(declaredField);
      var createQuery = column.getCreateQuery();
      fields.add(createQuery);
      if (column.isForeignKey()) foreignKeys.add(column.getCreateForeignKeyQuery());
    }
    query.append(fields);
    if (foreignKeys.length() != 0) query.append(",").append(foreignKeys);
    return query.append(");").toString();
  }

  public static String getTableName(Class<?> table) {
    return table.getAnnotation(EntityInfo.class).name();
  }

  public static <T> String getInsertQuery(T item, OnConflictStrategy onConflictStrategy) {
    var insertedFields = new StringJoiner(",");
    var insertValues = new StringJoiner(",");
    for (Column column : getColumn(item.getClass())) {
      if (column.getValue(item) != null && !column.isAutoIncremented()) {
        insertedFields.add(column.getName());
        insertValues.add("?");
      }
    }
    return String.format(
        "INSERT OR %s INTO %s (%s) VALUES (%s);",
        onConflictStrategy.toString(), getTableName(item.getClass()),
        insertedFields, insertValues);
  }

  public static String getUpdateQuery(Object item) throws NoSuchFieldException {
    var fieldsAndValues = new StringJoiner(",");
    for (Column column : getColumn(item.getClass())) {
      if (column.getValue(item) != null && !column.isAutoIncremented()) {
        fieldsAndValues.add(String.format("%s=?", column.getName()));
      }
    }
    var tableName = getTableName(item.getClass());
    @SuppressWarnings("ConstantConditions")
    var primaryKeyName = getPrimaryKey(item.getClass()).getName();
    @SuppressWarnings("ConstantConditions")
    var primaryKeyValue = getPrimaryKey(item.getClass()).getSqlValue(item);
    return String.format("UPDATE %s SET %s WHERE %s=%s;",
        tableName, fieldsAndValues, primaryKeyName, primaryKeyValue
    );
  }

  public static Column getPrimaryKey(Class<?> aClass) {
    for (Column column : getColumn(aClass)) {
      if (column.isPrimaryKey()) return column;
    }
    return null;
  }

  public static List<Column> getColumn(Class<?> table) {
    return Arrays.stream(table.getDeclaredFields())
        .map(Column::new)
        .collect(Collectors.toList());
  }
}