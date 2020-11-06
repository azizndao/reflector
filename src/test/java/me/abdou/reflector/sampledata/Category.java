package me.abdou.reflector.sampledata;

import me.abdou.reflector.annotations.ColumnInfo;
import me.abdou.reflector.annotations.EntityInfo;
import me.abdou.reflector.annotations.ForeignKey;
import me.abdou.reflector.annotations.PrimaryKey;

import java.time.LocalDateTime;

@EntityInfo(tableName = "categories")
public class Category {
  @PrimaryKey(autoIncremented = true)
  private int id;
  @ColumnInfo(unique = true)
  private String name;
  private String description;
  @ForeignKey()
  private User author;
  @ColumnInfo(name = "created_at")
  private LocalDateTime createAt;
  @ColumnInfo(name = "updated_at", nullable = true)
  private LocalDateTime updateAt;

  public int getId() {
    return id;
  }

  public Category setId(int id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Category setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Category setDescription(String description) {
    this.description = description;
    return this;
  }

  public User getAuthor() {
    return author;
  }

  public Category setAuthor(User author) {
    this.author = author;
    return this;
  }

  public LocalDateTime getCreateAt() {
    return createAt;
  }

  public Category setCreateAt(LocalDateTime createAt) {
    this.createAt = createAt;
    return this;
  }

  public LocalDateTime getUpdateAt() {
    return updateAt;
  }

  public Category setUpdateAt(LocalDateTime updateAt) {
    this.updateAt = updateAt;
    return this;
  }
}
