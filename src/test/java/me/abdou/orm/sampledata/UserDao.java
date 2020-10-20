package me.abdou.orm.sampledata;

import me.abdou.orm.annotations.Delete;
import me.abdou.orm.annotations.Insert;
import me.abdou.orm.annotations.Query;
import me.abdou.orm.annotations.Update;

import java.util.List;

public interface UserDao {

  @Insert
  void insertUser(User... users);

  @Delete
  void deleteUser(User... users);

  @Update
  void updateUsers(User... users);

  @Query("SELECT * FROM users WHERE 1;")
  List<User> getAllUsers();

  @Query("SELECT * FROM users WHERE id = [id]")
  User getUserById(int id);

  @Query("SELECT * FROM users WHERE name = [name]")
  List<User> getUserByName(String name);

  @Query("DELETE FROM users WHERE 1;")
  void clear();
}
