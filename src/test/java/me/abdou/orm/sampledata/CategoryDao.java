package me.abdou.orm.sampledata;

import me.abdou.orm.annotations.Delete;
import me.abdou.orm.annotations.Insert;
import me.abdou.orm.annotations.Query;
import me.abdou.orm.annotations.Update;

import java.util.List;

public interface CategoryDao {

  @Insert
  void insertCategory(Category... categories);

  @Delete
  void deleteUser(Category... categories);

  @Update
  void updateUsers(Category... category);

  @Query("SELECT * FROM users WHERE 1;")
  List<User> getAllUsers();

  @Query("SELECT * FROM categories WHERE id = [id]")
  User getUserById(int id);

  @Query("SELECT * FROM users WHERE name = [name]")
  List<User> getUserByName(String name);

  @Query("DELETE FROM users WHERE 1;")
  void clear();
}
