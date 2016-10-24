package orm;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import interfaces.DBContext;
import persistence.Column;
import persistence.Entity;
import persistence.Id;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


public class EntityManager implements DBContext {

    private Connection connection;

    private ResultSet resultSet;

    private PreparedStatement preparedStatement;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <E> boolean persist(E entity) throws SQLException, IllegalAccessException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        this.doCreate(entity, primary);


        Object value = primary.get(entity);

        if (value == null || (Long) value <= 0) {
            return this.doInsert(entity, primary);
        }
        return this.doUpdate(entity, primary);


    }

    private <E> boolean doUpdate(E entity, Field primary) throws IllegalAccessException, SQLException {

        String tableName = this.getTableName(entity.getClass());
        String sqlUpdate = "UPDATE " + tableName + " SET ";
        String where = "WHERE ";

        Field[] fields = entity.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (field.getName().equals(primary.getName())) {
                String primaryColumnName = this.getFieldName(primary);
                Long primaryColumnValue = (Long) primary.get(entity);
                where += "`" + primaryColumnName + "`" + " = " + "`" + primaryColumnValue + "`";
                continue;
            }

            Object value = field.get(entity);
            if (value instanceof Date) {
                sqlUpdate += "`" + this.getFieldName(field)  + "` = " + "`"
                        + new SimpleDateFormat("yyyy-MM-dd").format(value) + "`";
            } else {
                sqlUpdate += "`" + this.getFieldName(field) + "`" + "` = " + "`" + value + "`";
            }

            if (i < fields.length - 1) {
                sqlUpdate += ", ";
            }
        }
        sqlUpdate += where;

        return this.connection.prepareStatement(sqlUpdate).execute();
    }

    private <E> boolean doInsert(E entity, Field primary) throws IllegalAccessException, SQLException {

        String tableName = this.getTableName(entity.getClass());
        String sqlInsert = "INSERT INTO " + tableName + " (";

        String columns = "";
        String values = "";

        Field[] fields = entity.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (!field.getName().equals(primary.getName())) {

                columns += "`" + this.getFieldName(field) + "`";

                Object value = field.get(entity);
                if (value instanceof java.util.Date) {

                    value += "`" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "`";

                } else {

                    values += "`" + value + "`";
                }

                if (i < fields.length - 1) {
                    columns += ", ";
                    values += ", ";
                }
            }
        }

        sqlInsert += columns + ") " + "VALUES(" + values + ")";

        return this.connection.prepareStatement(sqlInsert).execute();
    }

    @Override
    public <E> List<E> find(Class<E> table) throws IllegalAccessException, SQLException, InstantiationException {
        return this.find(table, null);
    }

    @Override
    public <E> List<E> find(Class<E> table, String where) throws IllegalAccessException, InstantiationException, SQLException {
        String tableName = this.getTableName(table);
        String sqlSelect = "SELECT * FROM " + tableName + " WHERE 1=1";
        if (where != null) {
            sqlSelect += " AND " + where;
        }

        List<E> entities = new ArrayList<>();

        ResultSet rs = connection.prepareStatement(sqlSelect).executeQuery();
        while (rs.next()) {
            E entity = table.newInstance();
            this.fillValues(entity, rs);
            entities.add(entity);
        }

        return Collections.unmodifiableList(entities);
    }

    private <E> void fillValues(E entity, ResultSet rs) throws SQLException, IllegalAccessException {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = this.getFieldName(field);
            Object value = Mapper.getFieldValue(field, fieldName, rs);
            field.set(entity, value);
        }
    }

    @Override
    public <E> E findFirst(Class<E> table) throws IllegalAccessException, SQLException, InstantiationException {
        return this.findFirst(table, null);
    }

    @Override
    public <E> E findFirst(Class<E> table, String where) throws IllegalAccessException, InstantiationException, SQLException {
        String tableName = this.getTableName(table);
        String sqlSelect = "SELECT * FROM " + tableName + " WHERE 1=1";
        if (where != null) {
            sqlSelect += " AND " + where;
        }

        ResultSet rs = connection.prepareStatement(sqlSelect).executeQuery();
        rs.first();

        E entity = table.newInstance();

        this.fillValues(entity, rs);

        return entity;
    }

    static class Mapper {
        public static Object getFieldValue(Field field, String fieldName, ResultSet rs) throws SQLException {
            if (field.getType() == Integer.class || field.getType() == int.class) {
                return rs.getInt(fieldName);
            } else if (field.getType() == Long.class || field.getType() == long.class) {
                return rs.getLong(fieldName);
            } else if (field.getType() == String.class) {
                return rs.getString(fieldName);
            } else if (field.getType() == Date.class) {
                return rs.getDate(fieldName);
            }

            return null;
        }
    }

    private <E> String getTableName(Class<E> entity) {
        String tableName = "";

        if (entity.isAnnotationPresent(Entity.class)) {
            Entity entityAnnotation = entity.getAnnotation(Entity.class);
            tableName = entityAnnotation.name();
        }

        if (Objects.equals(tableName, "")) {
            tableName = entity.getSimpleName();
        }

        return tableName;
    }


    private String getFieldName(Field field) {

        String fieldName = "";

        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            fieldName = columnAnnotation.name();
        }

        if (Objects.equals(fieldName, "")) {
            fieldName = field.getName();
        }

        return fieldName;
    }

    private Field getId(Class c) {
        return Arrays.stream(c.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(""));
    }


    private <E> boolean doCreate(E entity, Field primary) throws SQLException {
        String tableName = this.getTableName(entity.getClass());
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + tableName + "( ";

        String columns = "";

        Field[] fields = entity.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (field.getName().equals(primary.getName())) {

                columns += "`" + this.getFieldName(field) + "`" +
                        " BIGINT " + " PRIMARY KEY AUTO_INCREMENT ";

            } else {

                columns += "`" + this.getFieldName(field) + "`" +
                        this.getDataBaseType(field);
            }

            if (i < fields.length - 1) {
                columns += ", ";
            }
        }

        sqlCreate += columns + ")";

        return connection.prepareStatement(sqlCreate).execute();
    }

    private String getDataBaseType(Field field) {
        switch (field.getType().getSimpleName().toLowerCase()) {
            case "int":
                return "INT";
            case "long":
                return "LONG";
            case "string":
                return "VARCHAR(50)";
            case "date":
                return "DATE";
        }

        return null;
    }

}
