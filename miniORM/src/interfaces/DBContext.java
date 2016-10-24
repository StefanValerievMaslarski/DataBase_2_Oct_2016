package interfaces;


import java.sql.SQLException;

public interface DBContext {
    <E> boolean persist(E entity) throws SQLException, IllegalAccessException;
    <E> Iterable<E> find(Class<E> table) throws IllegalAccessException, SQLException, InstantiationException;
    <E> Iterable<E> find(Class<E> table, String where) throws IllegalAccessException, InstantiationException, SQLException;
    <E> E findFirst(Class<E> table) throws IllegalAccessException, SQLException, InstantiationException;
    <E> E findFirst(Class<E> table, String where) throws IllegalAccessException, InstantiationException, SQLException;

}
