package org.geocraft.core.query;


import java.sql.SQLException;


public interface IProjectQuery {

  /**
   * Issue SELECT columnNames FROM tableName WHERE constraint0 AND constraint1 AND ... AND constraintN
   * 
   * The columnNames, tableName, and names in where clause constraints are GeoCraft names.
   * The IO implementations are responsible for mapping column and table names to specific vendor databases.
   * Unsupported queries should simply return NULL;
   * 
   * Returns a StaticResultSet containing the requested data
   * 
   * @param columnNames
   * @param tableName
   * @param whereClause
   * @return
   * @throws SQLException 
   */
  public StaticResultSet select(String[] columnNames, String tableName, QueryConstraint[] constraints) throws SQLException;

  /**
   * implement equivalent of DELETE from tableName WHERE constraint0 AND constraint1 AND ... AND constraintN
   * 
   * tableName and field names in constraint clause are GeoCraft names.
   * 
   * returns the number of "tableName" entities deleted.  Will cascade to delete child objects if necessary.
   */
  public int delete(String tableName, QueryConstraint[] constraints) throws SQLException;

  /**
   * Implement the equivalent SELECT COUNT(*) FROM tableName WHERE constraint0 AND constraint1 AND ... AND constraintN
   * 
   * The columnNames, tableName, and names in where clause constraints are GeoCraft names.
   * The IO implementations are responsible for mapping column and table names to specific vendor databases.
   * Unsupported queries should simply return NULL;
   * 
   * @param columnNames
   * @param tableName
   * @param whereClause
   * @return the number of rows matching the whereClause
   */
  public int count(String tableName, QueryConstraint[] constraints) throws SQLException;

}
