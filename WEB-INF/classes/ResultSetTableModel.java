/*
    DISCLAIMER: I (Ian Lewis) have MODIFIED this  version of the 
    ResultSetTableModel.java file posted by Dr. Mark Llewellyn as part of the 
    course CNT 4714 at the University of Central Florida. The file has been
    modified to work with my Project 4.

    Specific modifications will be mentioned in comments signed with "- Ian".
*/

// A TableModel that supplies ResultSet data to a JTable.
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;

// ResultSet rows and columns are counted from 1 and JTable 
// rows and columns are counted from 0. When processing 
// ResultSet rows or columns for use in a JTable, it is 
// necessary to add 1 to the row or column number to manipulate
// the appropriate ResultSet column (i.e., JTable column 0 is 
// ResultSet column 1 and JTable row 0 is ResultSet row 1).
public class ResultSetTableModel extends AbstractTableModel 
{
   private Connection connection;
   private Statement statement;
   public ResultSet resultSet;
   private ResultSetMetaData metaData;
   private int numberOfRows = 0;
   private int numberOfCols = 0;

   // keep track of database connection status
   public boolean connectedToDatabase = false;
   
   // constructor initializes resultSet and obtains its meta data object;
   // determines number of rows
   public ResultSetTableModel( String url, String username, String password) 
      throws SQLException, ClassNotFoundException
   {  
          /*
            Removed the calling of the driver class because the project will
            only be using com.mysql.jdbc.Driver as the JDBC driver. -Ian
          */
       
          Class.forName("com.mysql.jdbc.Driver");
       
          // connect to database
          connection = DriverManager.getConnection(url, username, password);

          if(connection == null)
              System.out.println("Connection Failed");
          
          // create Statement to query database
          statement = connection.createStatement( 
           ResultSet.TYPE_SCROLL_INSENSITIVE,
           ResultSet.CONCUR_READ_ONLY );

          // update database connection status
          connectedToDatabase = true;
          
          /*
            Removed the running of a query at the instantiation of the table.
            -Ian
          */
   } // end constructor ResultSetTableModel
   
   // get class that represents column type
   public Class getColumnClass( int column ) throws IllegalStateException
   {
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );
      
      /* 
        Added null catching statements to several methods to allow 
        initialization without needing to first connnect to a database. -Ian
      */
      if(resultSet == null)
          return null;

      // determine Java class of column
      try 
      {
         String className = metaData.getColumnClassName( column + 1 );
         
         // return Class object that represents className
         return Class.forName( className );
      } // end try
      catch ( Exception exception ) 
      {
         exception.printStackTrace();
      } // end catch
      
      return Object.class; // if problems occur above, assume type Object
   } // end method getColumnClass

   // get name of a particular column in ResultSet
   public String getColumnName( int column ) throws IllegalStateException
   {    
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      if(resultSet == null)
          return null;
      
      // determine column name
      try 
      {
         return metaData.getColumnName( column + 1 );  
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return ""; // if problems, return empty string for column name
   } // end method getColumnName

   // get number of columns in ResultSet
   public int getColumnCount() throws IllegalStateException
   {   
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );
 
      return numberOfCols;
   } // end method getColumnCount
   
   // return number of rows in ResultSet
   public int getRowCount() throws IllegalStateException
   {      
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );
 
      return numberOfRows;
   } // end method getRowCount

   // obtain value in particular row and column
   public Object getValueAt( int row, int column ) 
      throws IllegalStateException
   {
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );
      
      if(resultSet == null)
          return null;

      // obtain a value at specified ResultSet row and column
      try 
      {
		   resultSet.next();  /* fixes a bug in MySQL/Java with date format */
         resultSet.absolute( row + 1 );
         return resultSet.getObject( column + 1 );
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return ""; // if problems, return empty string object
   } // end method getValueAt
   
   // set new database query string
   public void setQuery( String query ) 
      throws SQLException, IllegalStateException 
   {
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // specify query and execute it
      resultSet = statement.executeQuery( query );

      // obtain meta data for ResultSet
      metaData = resultSet.getMetaData();

      // determine number of rows in ResultSet
      resultSet.last();                   // move to last row
      numberOfRows = resultSet.getRow();  // get row number
      numberOfCols = metaData.getColumnCount();
      
      // notify JTable that model has changed
      fireTableStructureChanged();
   } // end method setQuery


// set new database update-query string
   public void setUpdate( String query ) 
      throws SQLException, IllegalStateException 
   {
	    int res;
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // specify query and execute it
      res = statement.executeUpdate( query );
/*
      // obtain meta data for ResultSet
      metaData = resultSet.getMetaData();

      // determine number of rows in ResultSet
      resultSet.last();                   // move to last row
      numberOfRows = resultSet.getRow();  // get row number      
  */    
      // notify JTable that model has changed
      clearTable();
   } // end method setUpdate

   /*
     Added a method to allow quick and easy clearing of the table. -Ian
   */
   public void clearTable()
   {
       resultSet = null;
       metaData = null;
       numberOfRows = 0;
       
       /*
            This clears the data presently in the table and rebuilds an empty
            table. -Ian
       */
       
       fireTableStructureChanged();
   }

   // close Statement and Connection               
   public void disconnectFromDatabase()            
   {              
      if ( !connectedToDatabase )                  
         return;

      // close Statement and Connection            
      try                                          
      {                                            
         statement.close();                        
         connection.close();                       
      } // end try                                 
      catch ( SQLException sqlException )          
      {                                            
         sqlException.printStackTrace();           
      } // end catch                               
      finally  // update database connection status
      {                                            
         connectedToDatabase = false;              
      } // end finally                             
   } // end method disconnectFromDatabase          
}  // end class ResultSetTableModel



