package me.abdou.reflector;

import me.abdou.reflector.sampledata.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;

public class DaoTest {

  User user = new User().setName("Abdou Aziz Ndao").setCreateAt(LocalDateTime.now());
  Category category = new Category().setName("Development").setDescription("The development world").setAuthor(user)
      .setCreateAt(LocalDateTime.now());
  Post post1 = new Post().setName("What's new in JetPack Compose").setAuthor(user).setCategory(category)
      .setCreateAt(LocalDateTime.now());

  @BeforeClass
  public static void setUp() throws Exception {
//    Database.connect("jdbc:sqlite:./database.sqlite", new Class[]{User.class, Category.class, Post.class});
    Database.connect(
        new Database.User("jdbc:mysql://localhost/demo", "abdou", "aziz")
        , new Class[]{User.class, Category.class, Post.class}
    );
  }

  @AfterClass
  public static void beforeClass() {
    Database.getInstance().close();
  }

  @Test
  public void insert() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    userDao.insertUser(user);
    Assert.assertTrue(user.getId() != 0);
  }

  @Test
  public void insertWithOneForeignKey() throws Exception {
    var categoryDao = DaoStore.get(CategoryDao.class);
    var userDao = DaoStore.get(UserDao.class);
    userDao.insertUser(user);
    categoryDao.insertCategory(category);
    Assert.assertTrue(category.getId() != 0);
    Assert.assertTrue(category.getAuthor().getId() != 0);
  }

  @Test
  public void insertWithTwoForeignKey() throws Exception {
    user.setId(19);
    category.setId(10);
    var postDao = DaoStore.get(PostDao.class);
    postDao.insert(post1);
    Assert.assertTrue(post1.getId() != 0);
    Assert.assertTrue(category.getAuthor().getId() != 0);
    Assert.assertTrue(post1.getAuthor().getId() != 0);
    Assert.assertTrue(post1.getCategory().getId() != 0);
  }

  @Test
  public void delete() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    Assert.assertNotNull(userDao);
    user.setId(12);
    userDao.deleteUser(user);
  }

  @Test
  public void update() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    userDao.insertUser(user);
    user.setUpdateAt(LocalDateTime.now());
    userDao.updateUsers(user);
  }

  @Test
  public void queryItem() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    userDao.insertUser(user);
    var users = userDao.getUserByName("Abdou Aziz Ndao");
    Assert.assertTrue(users.size() != 0);
  }

  @Test
  public void queryAllItems() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    var users = userDao.getAllUsers();
    Assert.assertNotNull(users);
    Assert.assertTrue(users.size() != 0);
    Assert.assertNotNull(users.get(0).getName());
  }

  @Test
  public void clear() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    userDao.clear();
    var users = userDao.getAllUsers();
    Assert.assertEquals(0, users.size());
  }
}
