package me.abdou.orm;

import me.abdou.orm.annotations.EntityInfo;
import me.abdou.orm.exceptions.NoPrimaryKeyException;
import me.abdou.orm.exceptions.NotValidEntityException;
import me.abdou.orm.utils.TableUtils;

import java.sql.*;

public class Database {

  private volatile static Database INSTANCE;
  public Connection connection;

  private Database(String path, Class<?>[] tables) throws SQLException, NoSuchFieldException, NotValidEntityException, NoPrimaryKeyException {
    connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    createTables(tables);
  }

  public static synchronized Database connect(String path, Class<?>[] tables) throws SQLException, NoSuchFieldException, NotValidEntityException, NoPrimaryKeyException {
    if (INSTANCE == null) INSTANCE = new Database(path, tables);
    return INSTANCE;
  }

  private void createTables(Class<?>[] tables) throws NoSuchFieldException, NotValidEntityException, NoPrimaryKeyException {
    for (Class<?> table : tables) {
      if (!table.isAnnotationPresent(EntityInfo.class)) {
        throw new NotValidEntityException(table.getName());
      }
      try {
        var stm = createStatement();
        stm.execute(TableUtils.createTable(table));
        stm.close();
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
    }
  }

  public static Database getInstance() {
    return INSTANCE;
  }

  public Statement createStatement() {
    try {
      return connection.createStatement();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return null;
  }

  public PreparedStatement preparedStatement(String query) {
    try {
      return connection.prepareStatement(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }
}
