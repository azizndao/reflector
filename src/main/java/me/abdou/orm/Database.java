package me.abdou.orm;

import me.abdou.orm.annotations.EntityInfo;
import me.abdou.orm.exceptions.NoPrimaryKeyException;
import me.abdou.orm.exceptions.NotValidEntityException;
import me.abdou.orm.utils.TableUtils;

import java.sql.*;

public class Database {

  private volatile static Database INSTANCE;
  private final Connection connection;
  private final String dbType;

  private Database(User user, Class<?>[] tables) throws NoPrimaryKeyException, NotValidEntityException, NoSuchFieldException, SQLException {
    connection = DriverManager.getConnection(user.dbUrl, user.userName, user.userPassword);
    dbType = user.dbUrl.split(":")[1];
    createTables(tables);
  }

  public static synchronized Database connect(User user, Class<?>[] tables) throws SQLException, NoSuchFieldException, NotValidEntityException, NoPrimaryKeyException {
    if (INSTANCE == null) INSTANCE = new Database(user, tables);
    return INSTANCE;
  }

  public static synchronized Database connect(String url, Class<?>[] tables) throws SQLException, NoSuchFieldException, NotValidEntityException, NoPrimaryKeyException {
    var user = new User(url, null, null);
    return connect(user, tables);
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
      } catch (SQLException exception) {
        exception.printStackTrace();
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

  public String getDBType() {
    return dbType;
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
