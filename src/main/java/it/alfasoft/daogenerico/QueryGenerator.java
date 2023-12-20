package it.alfasoft.daogenerico;

import java.util.ArrayList;
import java.util.List;

public class QueryGenerator<T,I> {
    public String tableName;
    //COSTRUTTORI
    public QueryGenerator(String tableName){ this.tableName = tableName; }

    //QUERY GENERATORS
    public String getSelectAllQuery() {
        return "SELECT * FROM " + this.tableName;
    }
    //QUERIES
    public String getInsertQuery(List<String> columnNames) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 1; i < columnNames.size(); i++) {
            queryBuilder.append(columnNames.get(i));
            if (i < columnNames.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(") VALUES (");
        for (int i = 1; i < columnNames.size(); i++) {
            queryBuilder.append("?");
            if (i < columnNames.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");
        return queryBuilder.toString();
    }
    public String getSelectByIdQuery(String indexName, I index){
        return "SELECT * FROM " + tableName
                + " x WHERE x." + indexName + " = " + index +";" ;
    }
    public String getUpdateQuery(List<String> columnNames, I index) throws DaoException {
        StringBuilder queryBuilder = new StringBuilder("UPDATE " + tableName + " x SET ");
        for (int i = 1; i < columnNames.size(); i++) {
            queryBuilder.append( " x.");
            queryBuilder.append(columnNames.get(i));
            queryBuilder.append( " = ? ");
            if (i < columnNames.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(" WHERE x." + columnNames.get(0) + " = " + index );
        queryBuilder.append(";");
        return queryBuilder.toString();
    }
    public String getDeleteQuery(String indexName, I index){
        return "DELETE FROM " + tableName + " x WHERE x." + indexName + " = " + index + ";";
    }
    public String getDeleteAllQuery(String columnName){
        return "DELETE FROM " + tableName + " x WHERE x." + columnName + " LIKE '_%';";
    }
    public String getSearchByStringQuery(List<String> columnNames, String searchText){
        String searchContainsQuery = "'%" + searchText +"%'";
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName + " x WHERE");
        for(int i = 1 ; i<columnNames.size() ; i++){
            queryBuilder.append(" x.");
            queryBuilder.append(columnNames.get(i));
            queryBuilder.append(" LIKE " + searchContainsQuery);
            if(i < columnNames.size() - 1 ){
                queryBuilder.append(" OR ");
            }
        }
        queryBuilder.append(";");
        return queryBuilder.toString();
    }
    //COMPLETE SEARCH BY OBJECT
    public String getSearchByObjectQuery(List<String> columnNames, List<Object> valori){
        List<String> searchContainsQuery = new ArrayList<>();
        List<String> colonneEffettive = new ArrayList<>();
        for(int i = 0 ; i<valori.size() ; i++){
            //if(valori.get(i) instanceof java.util.Date){ //TODO :}
            if(i>=1){
                if(valori.get(i)!=null){
                    searchContainsQuery.add("'%" + valori.get(i) +"%'");
                    colonneEffettive.add(columnNames.get(i));
                }
            }
        }
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName + " x WHERE");
        for(int i = 0 ; i<searchContainsQuery.size() ; i++){
            queryBuilder.append(" x.");
            queryBuilder.append(colonneEffettive.get(i));
            queryBuilder.append(" LIKE " + searchContainsQuery.get(i));
            if(i < searchContainsQuery.size() - 1 ){ queryBuilder.append(" AND "); }
        }
        queryBuilder.append(";");
        return queryBuilder.toString();
    }

    //GENERA NOME DTO
    public static String generateDTOClassName(String tableName) {
        // Ottieni il nome del DTO basato sul nome della tabella
        String dtoClassName = tableName.substring(tableName.lastIndexOf(".") + 1);

        // Fai la prima lettera maiuscola
        dtoClassName = dtoClassName.substring(0, 1).toUpperCase() + dtoClassName.substring(1);

        // Se l'ultima lettera è 'e', cambia l'ultima lettera in 'a', altrimenti se finisce con 'i', cambia in 'o'
        if (dtoClassName.endsWith("e")) {
            dtoClassName = dtoClassName.substring(0, dtoClassName.length() - 1) + "a";
        } else if (dtoClassName.endsWith("i")) {
            dtoClassName = dtoClassName.substring(0, dtoClassName.length() - 1) + "o";
        }

        return dtoClassName;
    }
}
