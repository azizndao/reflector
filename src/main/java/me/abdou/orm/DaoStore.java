package me.abdou.orm;

import me.abdou.orm.annotations.*;
import me.abdou.orm.utils.Column;
import me.abdou.orm.utils.TableUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class DaoStore {

  private static final Map<String, Object> daoMap = new HashMap<>();
  private static final Database database = Database.getInstance();

  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> dao) throws Exception {
    if (!dao.isInterface()) throw new Exception("A Dao must be an interface");
    var key = dao.getName();
    if (!daoMap.containsKey(key)) synchronized (daoMap) {
      daoMap.put(key, createDao(dao));
    }
    return (T) daoMap.get(key);
  }

  @SuppressWarnings("unchecked")
  private static <T> T createDao(Class<T> dao) {
    return (T) Proxy.newProxyInstance(
        dao.getClassLoader(),
        new Class[]{dao},
        (proxy, method, args) -> {
          if (method.isAnnotationPresent(Query.class)) return invokeQueryMethod(method, args);
          if (method.isAnnotationPresent(Insert.class)) return invokeInsertMethod(method, args);
          if (method.isAnnotationPresent(Update.class)) return invokeUpdateMethod(args);
          if (method.isAnnotationPresent(Delete.class)) return invokeDeleteMethod(args);
          return method.invoke(dao, args);
        }
    );
  }

  private static Object invokeQueryMethod(Method method, Object[] args) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    var query = method.getAnnotation(Query.class).value();
    query = query.replaceAll("(\\[[a-zA-Z_0-9]+])", "?");
    var statement = database.preparedStatement(query);
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(i + 1, args[i]);
      }
    }
    var returnType = method.getReturnType();
    Object result;
    if (returnType == boolean.class || returnType == Boolean.class || returnType == void.class) {
      result = statement.execute();
    } else {
      result = fetchResultSet(statement.executeQuery(), method);
    }
    statement.close();
    return result;
  }

  private static boolean invokeInsertMethod(Method method, Object[] args) throws SQLException, NoSuchFieldException {
    var onConflictStrategy = method.getAnnotation(Insert.class).onconflictStrategy();
    Object[] items = getArrayArguments(args);
    var columns = TableUtils.getColumn(items[0].getClass());
    boolean result = false;
    for (Object item : items) {
      var insertQuery = TableUtils.getInsertQuery(item, onConflictStrategy);
      var insertStatement = prepareStatement(insertQuery, item);
      result = insertStatement.execute();
      insertStatement.close();
    }
    var selectStatement = database.createStatement();
    String tableName = TableUtils.getTableName(items[0].getClass());
    @SuppressWarnings("ConstantConditions")
    String primaryKeyName = TableUtils.getPrimaryKey(items[0].getClass()).getName();
    var resultSet = selectStatement.executeQuery(
        String.format("SELECT * FROM %s WHERE 1 ORDER BY %s DESC LIMIT %s",
            tableName, primaryKeyName, items.length)
    );
    var currentIndex = items.length - 1;
    while (resultSet.next()) {
      var currentItem = items[currentIndex--];
      for (Column column : columns) {
        column.setValue(currentItem, resultSet.getObject(column.getName()));
      }
    }
    selectStatement.close();
    return result;
  }

  private static boolean invokeDeleteMethod(Object[] args) throws NoSuchFieldException {
    var items = getArrayArguments(args);
    var primaryKey = TableUtils.getPrimaryKey(items[0].getClass());
    assert primaryKey != null;
    var primaryKeyValues = new StringJoiner(",");
    for (var item : items) {
      primaryKeyValues.add(primaryKey.getSqlValue(item).toString());
    }
    boolean result = false;
    try {
      var statement = database.createStatement();
      var query = String.format("DELETE FROM %s WHERE %s IN (%s)",
          TableUtils.getTableName(items[0].getClass()),
          primaryKey.getName(),
          primaryKeyValues.toString()
      );
      result = statement.execute(query);
      statement.close();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
    return result;
  }

  private static int invokeUpdateMethod(Object[] args) throws NoSuchFieldException, SQLException {
    var items = getArrayArguments(args);
    int result = 0;
    for (Object item : items) {
      var updateQuery = TableUtils.getUpdateQuery(item);
      var statement = prepareStatement(updateQuery, item);
      result = statement.executeUpdate();
      statement.close();
    }
    return result;
  }

  public static PreparedStatement prepareStatement(String query, Object item) throws NoSuchFieldException, SQLException {
    var statement = database.preparedStatement(query);
    var currentIndex = 1;
    for (Column column : TableUtils.getColumn(item.getClass())) {
      if (column.getValue(item) != null && !column.isAutoIncremented()) {
        statement.setObject(currentIndex++, column.getSqlValue(item));
      }
    }
    return statement;
  }

  private static Object[] getArrayArguments(Object[] args) {
    if (args == null || args.length == 0) return new Object[0];
    if (args.length > 1) return args;
    var inputs = args[0];
    if (inputs instanceof Object[]) return (Object[]) inputs;
    else if (inputs instanceof List<?>) return ((List<?>) inputs).toArray();
    else return new Object[]{inputs};
  }

  private static Object fetchResultSet(ResultSet resultSet, Method method) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    var returnType = method.getReturnType();
    Object result = null;
    if (returnType.isAnnotationPresent(EntityInfo.class) && resultSet.next()) {
      var item = returnType.getDeclaredConstructor().newInstance();
      for (Column column : TableUtils.getColumn(returnType)) {
        column.setValue(item, resultSet.getObject(column.getName()));
      }
      result = item;
    } else if (returnType == List.class) {
      var list = new ArrayList<>();
      var itemClassName = method.getGenericReturnType()
          .getTypeName().replaceAll("^[a-z._A-Z0-9]+<", "")
          .replaceAll(">$", "");
      try {
        var itemType = Class.forName(itemClassName);
        while (resultSet.next()) {
          var item = itemType.getDeclaredConstructor().newInstance();
          for (Column column : TableUtils.getColumn(itemType)) {
            column.setValue(item, resultSet.getObject(column.getName()));
          }
          list.add(item);
        }
        result = list;
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    return result;
  }
}
