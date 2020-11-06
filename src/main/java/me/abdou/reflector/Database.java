package me.abdou.reflector;

import me.abdou.reflector.annotations.EntityInfo;
import me.abdou.reflector.exceptions.NoPrimaryKeyException;
import me.abdou.reflector.exceptions.NotValidEntityException;
import me.abdou.reflector.exceptions.OrmException;
import me.abdou.reflector.utils.TableUtils;

import java.sql.*;

public final class Database {

  private volatile static Database INSTANCE;
  private final Connection connection;
  private final String SGBD;

  private Database(User user) throws SQLException {
    synchronized (this) {
      SGBD = user.dbUrl.split(":")[1];
      connection = DriverManager.getConnection(user.dbUrl, user.userName, user.userPassword);
    }
  }

  public static Database connect(User user, Class<?>[] tables) throws SQLException, OrmException {
    INSTANCE = new Database(user);
      INSTANCE.createTables(tables);
    return INSTANCE;
  }

  public static Database connect(String url, Class<?>[] tables) throws SQLException, OrmException {
    var user = new User(url, null, null);
    return connect(user, tables);
  }

  private void createTables(Class<?>[] tables) throws NotValidEntityException, NoPrimaryKeyException {
    createTableItems(tables);
    createForeignKeys(tables);
  }

  private void createTableItems(Class<?>[] tables) throws NotValidEntityException, NoPrimaryKeyException {
    for (Class<?> table : tables) {
      if (!table.isAnnotationPresent(EntityInfo.class)) throw new NotValidEntityException(table.getName());
      try (var stm = createStatement()) {
        assert stm != null;
        stm.execute(TableUtils.createTable(table));
      } catch (SQLException exception) {
        exception.printStackTrace();
      }
    }
  }

  private void createForeignKeys(Class<?>[] tables) {
    for (Class<?> table : tables) {
      for (String key : TableUtils.createForeignKeys(table)) {
        try (var stm = createStatement()) {
          assert stm != null;
          stm.execute(key);
        } catch (SQLException ignored) {
        }
      }
    }
  }

  public static Database getInstance() {
    return INSTANCE;
  }

  public Statement createStatement() {
    try {
      return connection.createStatement();
    } catch (SQLException exception) {
      exception.printStackTrace();
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
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  public String getSGBD() {
    return SGBD;
  }

  public static class User {
    public final String dbUrl;
    public final String userName;
    public final String userPassword;

    User(String dbUrl, String userName, String userPassword) {
      this.dbUrl = dbUrl;
      this.userName = userName;
      this.userPassword = userPassword;
    }
  }
}
