package it.alfasoft.daogenerico;


import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.alfasoft.propertiesmanager.PropertiesManager;
public abstract class DaoConnected<T,I> implements Dao<T,I>, Serializable {

    public String tableName;
    public QueryGenerator<T,I> qg;
    public DaoConnected(){}
    public DaoConnected(String tableName){
        this.tableName = tableName;
        this.qg = new QueryGenerator<T,I>(tableName);
    }

    //GETTERS
    public abstract I getGeneratedKey(PreparedStatement ps) throws DaoException;
    public Connection getConnection() throws IOException, SQLException {
        Connection connection = null;
        String dburl = PropertiesManager.getProperties().getProperty("db.url");
        String dbuser =PropertiesManager.getProperties().getProperty("db.user");
        String dbpwd = PropertiesManager.getProperties().getProperty("db.password");
        //Il driver manager consente di aprire una connessione con il DB
        connection = DriverManager.getConnection(dburl,dbuser,dbpwd);
        return connection;
    }
    public String getTableName(){
        return this.tableName;
    }
    public void setTableName(String tableName){
        this.tableName = tableName;
        this.qg = new QueryGenerator<T,I>(tableName);
    }
    public List<String> getTableColumns() throws DaoException {
        List<String> columnNames = new ArrayList<>();
        String query = "SELECT * FROM " + getTableName() + " LIMIT 1";
        //Costrutto try-with-resources :
        try ( Statement statement = getConnection().createStatement();
              ResultSet resultSet = statement.executeQuery(query)
        ){
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            addColumnsToList(columnNames,columnCount,metaData);
        } catch (Exception e) {e.printStackTrace(); throw new DaoException();}
        return columnNames;
    }
    public void addColumnsToList(List<String> columnNames, int columnCount, ResultSetMetaData metaData) throws DaoException{
        String columnName = null;
        for (int i = 1; i <= columnCount; i++) {
            try{ columnName = metaData.getColumnName(i);}
            catch(SQLException sqle){ throw new DaoException();}
            columnNames.add(columnName);
        }
    }
    public T getById(I id) throws DaoException {
        try(
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery( qg.getSelectByIdQuery(getTableColumns().get(0) , id))
        )
        { 
            rs.next();
            return creaOggetto(rs);
        }catch(Exception e){ e.printStackTrace();throw new DaoException();}
    }

    public List<T> getList(ResultSet rs) throws DaoException{
        try
        {
            List<T> oggetti = new ArrayList<>();
            while(rs.next()){ oggetti.add( creaOggetto(rs)); }
            return oggetti;
        }catch(SQLException e){ e.printStackTrace(); throw new DaoException();}
    }
    //CREATE
    public I create(T elemento) throws DaoException{
        if(elemento == null) throw new DaoException();
        try( PreparedStatement ps = getConnection().prepareStatement(
                //this.getInsertQuery() --> DaoSimple beta
                qg.getInsertQuery(getTableColumns())
                , Statement.RETURN_GENERATED_KEYS) )
        {
            eseguiUpdate(ps,elemento);
            return getGeneratedKey(ps);
        }
        catch (Exception e) { e.printStackTrace(); throw new DaoException(); }
    }

    public List<I> createFromFile(String percorsoFile) throws DaoException{
        try{
            List<T> oggetti = acquisisciOggettoDaFile(percorsoFile);
            List<I> chiavi = new ArrayList<>();
            for(T t : oggetti){ chiavi.add(create(t)); }
            return chiavi;
        }catch (Exception e) { e.printStackTrace(); throw new DaoException(); }
    }

    //READ
    public List<T> read() throws DaoException {
        try ( 
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery( qg.getSelectAllQuery());
        )
        {
            List<T> oggetti = new ArrayList<>();
            while(rs.next()){ oggetti.add( creaOggetto(rs)); }
            return oggetti;
        }catch (Exception e) { e.printStackTrace(); throw new DaoException(); }
    }
    //UPDATE
    public int update(I id, T elemento) throws DaoException {
        if(checkOggetto(id,elemento)){
            try( PreparedStatement ps = getConnection().prepareStatement( qg.getUpdateQuery(getTableColumns(),id) )){
                return eseguiUpdate(ps, elemento);
            }
            catch (Exception e) { e.printStackTrace(); throw new DaoException(); }
        }
        return 0;
    }
    //DELETE
    public int delete(I id) throws DaoException {
        try( PreparedStatement ps = getConnection().prepareStatement( qg.getDeleteQuery(getTableColumns().get(0),id), Statement.RETURN_GENERATED_KEYS) ){
            return ps.executeUpdate();
        }catch (Exception e) { e.printStackTrace(); throw new DaoException(); }
    }
    public int deleteAll() throws DaoException{
        try{ //Connetti e cancella tutto
            Statement stmt = getConnection().createStatement();
            return stmt.executeUpdate(qg.getDeleteAllQuery(getTableColumns().get(1)));
        }catch (Exception e) { e.printStackTrace(); throw new DaoException(); }
    }
    //FIND BY STRING
    public List<T> find(String searchText) throws DaoException {
        try{
            Statement stmt = getConnection().createStatement();
            List<T> oggetti = new ArrayList<>();
            ResultSet rs = stmt.executeQuery( qg.getSearchByStringQuery(getTableColumns(), searchText) );
            while(rs.next()){ oggetti.add( creaOggetto(rs)); }
            return oggetti;
        }catch(Exception e){ e.printStackTrace(); throw new DaoException();}
    }
    //FIND BY OBJECT
    public List<T> find(T searchObj) throws DaoException {
        try{
            Statement stmt = getConnection().createStatement();
            List<T> oggetti = new ArrayList<>();
            ResultSet rs = stmt.executeQuery( qg.getSearchByObjectQuery(getTableColumns() , getValori(searchObj) ) );
            while(rs.next()){ oggetti.add( creaOggetto(rs)); }
            return oggetti;
        }catch(Exception e){ e.printStackTrace();throw new DaoException();}
    }

    // FUNZIONI CUSTOM PER UTILIZZO NON STANDARD
    public abstract int assegnaCategoria(String nomeCategoria, I id) throws DaoException;

    //FUNZIONI DI UTILITA (di cui devo fare l'override per implementare un dao specifico)
    public abstract T creaOggetto(ResultSet rs) throws DaoException;
    public abstract boolean checkOggetto(I id, T elemento) throws DaoException;
    public abstract List<Object> getValori(T elemento) throws DaoException;
    public abstract int eseguiUpdate(PreparedStatement ps, T elemento) throws DaoException;

    public abstract List<T> acquisisciOggettoDaFile(String percorsoFile) throws DaoException;

}
