package me.abdou.reflector.sampledata;

import me.abdou.reflector.annotations.Delete;
import me.abdou.reflector.annotations.Insert;
import me.abdou.reflector.annotations.Query;
import me.abdou.reflector.annotations.Update;

import java.util.List;

public interface PostDao {

  @Insert
  void insert(Post... posts);

  @Delete
  void delete(Post... posts);

  @Update
  void update(Post... posts);

  @Query("SELECT * FROM posts WHERE 1;")
  List<User> getAll();

  @Query("SELECT * FROM posts WHERE id = [id]")
  User getUserById(int id);

  @Query("SELECT * FROM users WHERE name = [name]")
  List<User> getByName(String name);

  @Query("DELETE FROM users WHERE 1;")
  void clear();
}
