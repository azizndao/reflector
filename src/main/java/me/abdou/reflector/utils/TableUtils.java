package me.abdou.reflector.utils;

import me.abdou.reflector.Database;
import me.abdou.reflector.annotations.EntityInfo;
import me.abdou.reflector.exceptions.NoPrimaryKeyException;

import java.util.*;
import java.util.stream.Collectors;

public final class TableUtils {

  private static final Map<String, EntityInfo> INFO_MAP = new HashMap<>();

  public static String createTable(Class<?> table) throws NoPrimaryKeyException {
    if (getPrimaryKey(table) == null) {
      throw new NoPrimaryKeyException(table.getName());
    }
    var tableName = getTableName(table);
    var query = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
    var fields = new StringJoiner(",");
    for (Column column : TableUtils.getColumn(table)) {
      if (!column.isForeignKey()) fields.add(column.getCreateQuery());
    }
    return query.append(fields).append(");").toString();
  }

  public static List<String> createForeignKeys(Class<?> table) {
    var foreignKeys = new ArrayList<String>();
    for (Column column : TableUtils.getColumn(table)) {
      if (column.isForeignKey()) foreignKeys.add(column.getCreateForeignKeyQuery());
    }
    return foreignKeys;
  }

  public static String getTableName(Class<?> table) {
    if (!INFO_MAP.containsKey(table.getName()))
      INFO_MAP.put(table.getName(), table.getAnnotation(EntityInfo.class));
    return INFO_MAP.get(table.getName()).tableName();
  }

  public static <T> String getInsertQuery(T item, OnConflict onConflict) {
    var insertedFields = new StringJoiner(",");
    var insertedValues = new StringJoiner(",");
    for (Column column : getColumn(item.getClass())) {
      if (column.getValue(item) != null && !column.isAutoIncremented()) {
        insertedFields.add(column.getName());
        insertedValues.add("?");
      }
    }
    System.out.println(insertedFields);
    String sgbd = Database.getInstance().getSGBD();
    if (sgbd.equals("mysql")) {
      if (onConflict == OnConflict.REPLACE) {
        return String.format(
            "REPLACE INTO %s (%s) VALUES (%s);",
            getTableName(item.getClass()),
            insertedFields, insertedValues);
      }
      return String.format(
          "INSERT  %s (%s) VALUES (%s);",
          getTableName(item.getClass()),
          insertedFields, insertedValues);
    } else if (sgbd.equals("sqlite")) {
      return String.format(
          "INSERT OR %s INTO %s (%s) VALUES (%s);",
          onConflict.toString(), getTableName(item.getClass()),
          insertedFields, insertedValues);
    }
    return null;
  }

  public static String getUpdateQuery(Object item) {
    var fieldsAndValues = new StringJoiner(",");
    for (Column column : getColumn(item.getClass())) {
      if (column.getValue(item) != null && !column.isAutoIncremented()) {
        fieldsAndValues.add(String.format("%s=?", column.getName()));
      }
    }
    var tableName = getTableName(item.getClass());
    Column primaryKey = getPrimaryKey(item.getClass());
    assert primaryKey != null;
    return String.format("UPDATE %s SET %s WHERE %s=%s;",
        tableName, fieldsAndValues, primaryKey.getName(), primaryKey.getSqlValue(item)
    );
  }

  public static String getDeleteQuery(Object[] items) {
    var primaryKeyValues = new StringJoiner(",");
    var primaryKey = getPrimaryKey(items[0].getClass());
    for (var item : items) {
      assert primaryKey != null;
      primaryKeyValues.add(primaryKey.getSqlValue(item).toString());
    }
    return String.format("DELETE FROM %s WHERE %s IN (%s)",
        TableUtils.getTableName(items[0].getClass()),
        primaryKey.getName(),
        primaryKeyValues.toString()
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