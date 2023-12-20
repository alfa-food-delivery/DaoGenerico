package it.alfasoft.daogenerico;

import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.util.List;

public interface Dao<T,I> {
    //CRUD OPERATIONS
    I create(T elemento) throws DaoException;
    List<I> createFromFile(String percorsoFile) throws DaoException;
    List<T> read() throws DaoException;
    int update(I id, T elemento) throws DaoException;
    int delete(I id) throws DaoException;
    T getById(I id) throws DaoException;
    List<T> find(String searchText) throws DaoException;
    List<T> find(T searchObj) throws DaoException;
    int assegnaCategoria(String nomeCategoria, I id) throws DaoException;

}