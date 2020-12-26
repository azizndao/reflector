package me.abdou.reflector;

import me.abdou.reflector.sampledata.Category;
import me.abdou.reflector.sampledata.Post;
import me.abdou.reflector.sampledata.User;
import org.junit.*;

public class DatabaseTest {


  @BeforeClass
  public static void create() throws Exception {
    Database.connect(
        "jdbc:sqlite:database.sqlite",
        new Class[]{User.class, Category.class, Post.class}
    );
  }

  @Test
  public void verifySingleton() {
    Assert.assertNotNull(Database.getInstance());
  }

  @AfterClass
  public static void close() {
    Database.getInstance().close();
  }
}
