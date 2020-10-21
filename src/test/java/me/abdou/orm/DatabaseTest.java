package me.abdou.orm;

import me.abdou.orm.sampledata.Category;
import me.abdou.orm.sampledata.Post;
import me.abdou.orm.sampledata.User;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

  private Database database;

  @Before
  public void create() throws Exception {
    database = Database.connect(
        "database.sqlite",
        new Class[]{User.class, Category.class, Post.class}
    );
  }

  @Test
  public void close() {
    database.close();
  }
}
