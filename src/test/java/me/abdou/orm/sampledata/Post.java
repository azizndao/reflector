package me.abdou.orm.sampledata;

import me.abdou.orm.annotations.ColumnInfo;
import me.abdou.orm.annotations.EntityInfo;
import me.abdou.orm.annotations.ForeignKey;
import me.abdou.orm.annotations.PrimaryKey;

import java.time.LocalDateTime;

@EntityInfo(name = "posts")
public class Post {
  @PrimaryKey(autoIncremented = true)
  private int id;
  private String name;
  @ForeignKey(target = "id")
  private User author;
  @ForeignKey(target = "id")
  private Category category;
  @ColumnInfo(name = "created_at")
  private LocalDateTime createAt;
  @ColumnInfo(name = "updated_at", nullable = true)
  private LocalDateTime updateAt;
}
