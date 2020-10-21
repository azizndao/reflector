package me.abdou.orm;

import me.abdou.orm.sampledata.Category;
import me.abdou.orm.sampledata.CategoryDao;
import me.abdou.orm.sampledata.Post;
import me.abdou.orm.sampledata.PostDao;
import me.abdou.orm.sampledata.User;
import me.abdou.orm.sampledata.UserDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class DaoTest {

  User user = new User().setName("Abdou Aziz Ndao").setCreateAt(LocalDateTime.now());
  Category category = new Category().setName("Development").setDescription("The development world").setAuthor(user)
      .setCreateAt(LocalDateTime.now());
  Post post1 = new Post().setName("What's new in JetPack Compose").setAuthor(user).setCategory(category)
      .setCreateAt(LocalDateTime.now());

  @Before
  public void setUp() throws Exception {
    Database.connect("jdbc:sqlite:./database.sqlite", new Class[]{User.class, Category.class, Post.class});
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
    user.setId(1);
    categoryDao.insertCategory(category);
    Assert.assertTrue(category.getId() != 0);
    Assert.assertTrue(category.getAuthor().getId() != 0);
  }

  @Test
  public void insertWithTwoForeignKey() throws Exception {
    var postDao = DaoStore.get(PostDao.class);
    postDao.insert(post1);
    user.setId(1);
    category.setId(1);
    Assert.assertTrue(post1.getId() != 0);
    Assert.assertTrue(category.getAuthor().getId() != 0);
    Assert.assertTrue(post1.getAuthor().getId() != 0);
    Assert.assertTrue(post1.getCategory().getId() != 0);
  }

  @Test
  public void delete() throws Exception {
    var userDao = DaoStore.get(UserDao.class);
    Assert.assertNotNull(userDao);
    userDao.insertUser(user);
    Assert.assertTrue(user.getId() != 0);
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
