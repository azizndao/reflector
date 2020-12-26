package me.abdou.reflector.sampledata;

import me.abdou.reflector.annotations.ColumnInfo;
import me.abdou.reflector.annotations.EntityInfo;
import me.abdou.reflector.annotations.ForeignKey;
import me.abdou.reflector.annotations.PrimaryKey;

import java.time.LocalDateTime;

@EntityInfo(tableName = "posts")
public class Post {
  @PrimaryKey(autoIncremented = true)
  private int id;
  private String name;
  @ForeignKey()
  private User author;
  @ForeignKey()
  private Category category;
  @ColumnInfo(name = "created_at")
  private LocalDateTime createAt;
  @ColumnInfo(name = "updated_at", nullable = true)
  private LocalDateTime updateAt;

  public int getId() {
    return id;
  }

  public Post setId(int id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Post setName(String name) {
    this.name = name;
    return this;
  }

  public Category getCategory() {
    return category;
  }

  public Post setAuthor(User author) {
    this.author = author;
    return this;
  }

  public User getAuthor() {
    return author;
  }

  public Post setCategory(Category category) {
    this.category = category;
    return this;
  }

  public LocalDateTime getCreateAt() {
    return createAt;
  }

  public Post setCreateAt(LocalDateTime createAt) {
    this.createAt = createAt;
    return this;
  }

  public LocalDateTime getUpdateAt() {
    return updateAt;
  }

  public Post setUpdateAt(LocalDateTime updateAt) {
    this.updateAt = updateAt;
    return this;
  }
}
