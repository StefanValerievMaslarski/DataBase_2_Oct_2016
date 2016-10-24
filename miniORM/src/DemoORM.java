
import com.mysql.jdbc.Connection;
import connection.DataBaseConnection;
import models.User;
import orm.EntityManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;


public class DemoORM {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException{
        try {
            EntityManager em = new EntityManager((Connection) DataBaseConnection.getConnection());
            User user = new User("Ivan2", 23, new Date());
            user.setId(1);
            em.persist(user);
            User userFind = em.findFirst(User.class);
            System.out.println(userFind.toString());

            List<User> users = em.find(User.class);
            for (User userFromList : users) {
                System.out.println(userFromList.toString());
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
