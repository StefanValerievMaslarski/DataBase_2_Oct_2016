package models;


import persistence.Column;
import persistence.Entity;
import persistence.Id;
import java.util.Date;

@Entity(name = "users")
public class User {

    @Id
//    @GenerateValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private int age;

    @Column(name = "registration_date")
    private Date registrationDate;

    public User() {
        super();
    }

    public User(String name, int age, Date registrationDate) {
        this.setName(name);
        this.setAge(age);
        this.setRegistrationDate(registrationDate);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }


    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
             sb.append("id: " + this.getId()).append(System.lineSeparator())
               .append("name: " + this.getName()).append(System.lineSeparator())
               .append("age: " + this.getAge()).append(System.lineSeparator())
               .append("registration date: "+ this.getRegistrationDate()).append(System.lineSeparator());
        return sb.toString();

    }
}
