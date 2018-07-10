/*
    Name: Ian Lewis
    Course: CNT 4714 - Summer 2017 - Project Four
    Assignment title: A Three-Tier Distributed Web-Based Application
    Date: August 1, 2017
*/

/*
    NOTE: Used in this program is Prof. Llewellyn's "ResultTableModel.java" with
    a few personal modifications. Modifcations are noted in the source file as
    having been made by me.
*/

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class SQLHandler extends HttpServlet
{
    /*
        These are just shortcut strings. I didn't want to place the queries and
        SQL commands in shortcut strings just to avoid confusing you any further.
    */
    String home = "/index.jsp";
    String[] databaseInfo = {"jdbc:mysql://localhost:3306/project4", "root", "password"};
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String HTMLOutput = null;
        String sqlCommand = request.getParameter("command");
        ResultSetTableModel resultTable = null;
        
        // We check to see if we need to submit our default query.
        if(sqlCommand == null || "".equals(sqlCommand))
            sqlCommand = "select * from suppliers";
        
        /*
            If not, we create our resultTable that we will work with for the
            remainder of the program as our container for our SQL results.
            
            Again, to clarify, the ResultTableModel.java file included in this
            package is the same java file provided by Prof. Llewellyn as part of
            module 3 on webcourses.
        */
        try 
        {
            resultTable = new ResultSetTableModel(databaseInfo[0], 
                                                  databaseInfo[1], 
                                                  databaseInfo[2]);
        }
        
        catch (SQLException ex) 
        {
            HTMLOutput = "Cannot connect to Database: MySQL login credentials are set as 'Username = root' and 'Password = password'";
        } 
        
        catch (ClassNotFoundException ex) 
        {
            HTMLOutput = "MySQL Driver not found: Check the directory '/Project4/WEB-INF/lib' and ensure the driver jar is there.";
        }
        
        // Provided we properly instantiated our ResultTableModel, continue!
        if(resultTable != null)
        {
            /*
                Simply put, if we have a select statement, we send the query to
                the server and write the results to our output in HTML form.
            */
            if(sqlCommand.toLowerCase().startsWith("select"))
            {
                try
                {
                    resultTable.setQuery(sqlCommand);
                    HTMLOutput = tableToHTML(resultTable);
                }
            
                catch (SQLException ex)
                {
                    HTMLOutput = "SQL ERROR: Check your SQL syntax and ensure you are entering proper table names/values.";
                }
            
                catch (IllegalStateException ex)
                {
                    HTMLOutput = "There is a database connection error. Please reload the application and check your MySQL Server.";
                }
            }
        
            /*
                So this big else statement is essentially the business logic.
                Now I warn you that some of the for loops get all wonky, but they
                are that way so that I could properly format the SQL queries and
                update commands.
            
                I'll explain them as clearly as I can but apologies if it's all
                confusing.
            */
            else
            {
                try
                {
                    int updatedAmount = 0;
                    
                    /*
                        We check any insertion into shipments to see if the
                        quantity is higher than or equal to 100 so we can make
                        updates to the supplier status if necessary.
                    */
                    if(sqlCommand.toLowerCase().startsWith("insert into shipments"))
                    {
                        int quantity = 0;
                        String temp = "";
                    
                        /*
                            We run the insertion first so we know later to update
                            the given supplier's status as well as everyone else's
                        */
                        resultTable.setUpdate(sqlCommand);
                    
                        // More string trickery to properly format the SQL command
                        for(int i = (sqlCommand.toLowerCase().indexOf("j") + 2);i < sqlCommand.length();i++)
                        {
                            if(sqlCommand.charAt(i) == ')')
                                break;
                        
                            else if((sqlCommand.charAt(i) != ' ') && (sqlCommand.charAt(i) != ',') && (sqlCommand.charAt(i) != '\''))
                                temp += sqlCommand.charAt(i);
                        }
                    
                        /*
                            If we need to update our status', do so and report
                            how many were updated.
                        */
                        if(Integer.valueOf(temp) >= 100)
                            updatedAmount = statusUpdate(resultTable);
                    }
                    
                    /*
                        Now for the case we have an update to our shipments table.
                        
                        Apologies for how messy this gets...
                    */
                    else if(sqlCommand.toLowerCase().startsWith("update shipments"))
                    {
                        ArrayList<Integer> oldQuantityList = new ArrayList<>();
                        ArrayList<Integer> newQuantityList = new ArrayList<>();
                        String suppliersList = "(";
                        String partsList = "(";
                        
                        // More string trickery for SQL command formatting.
                        for(int i = (sqlCommand.indexOf("pnum") + 4);i < sqlCommand.length();i++)
                        {
                            /*
                                Essentially I tried to find any part ID's in
                                the user input but I wanted to account for any
                                and all anomalies in the input formatting.
                            */
                            if((sqlCommand.charAt(i) == 'P') && ((i + 1) < sqlCommand.length()))
                            {
                                partsList += "'P";
                            
                                for(int j = (i + 1);j < sqlCommand.length();j++)
                                {
                                    if((sqlCommand.charAt(j) != ' ') && (sqlCommand.charAt(j) != ',') && (sqlCommand.charAt(j) != ';'))
                                        partsList += sqlCommand.charAt(j);
                                
                                    else if((sqlCommand.charAt(j) == '\'') || (sqlCommand.charAt(j) == ' '))
                                    {
                                        partsList += "'";
                                        break;
                                    }
                                }
                            }
                            
                            if(sqlCommand.charAt(i) == ',')
                                partsList += ", ";
                        
                            else if((sqlCommand.charAt(i) == ')') || (i == (sqlCommand.length() - 1)))
                                partsList += ")";
                        }
                        
                        /*
                            Phew! Once we have our properly formatted SQL set,
                            we can see what suppliers look like before the update.
                        */
                        resultTable.setQuery("select snum, quantity from shipments where pnum in " + partsList);
                        
                        for(int i = 0;i < resultTable.getRowCount();i++)
                            oldQuantityList.add((Integer)resultTable.getValueAt(i, 1));
                        
                        // We run our user input update command.
                        resultTable.setUpdate(sqlCommand);
                        
                        /*
                            Now we check to see what the suppliers look like 
                            after the update is made.
                        */
                        resultTable.setQuery("select snum, quantity from shipments where pnum in " + partsList);
                        
                        for(int i = 0;i < resultTable.getRowCount();i++)
                            newQuantityList.add((Integer)resultTable.getValueAt(i, 1));
                        
                        /*
                            If we find that any of the supplier's shipment
                            quantities have hit or gone over 100 after the update
                            was made, we update our status'. Otherwise, leave
                            everything as is.
                        */
                        for(int i = 0;i < oldQuantityList.size();i++)
                            if((!Objects.equals(oldQuantityList.get(i), newQuantityList.get(i))) && (newQuantityList.get(i) >= 100))
                            {
                                updatedAmount = statusUpdate(resultTable);
                                break;
                            }
                    }
                
                    /*
                        Finally, if we have any other update or insertion command
                        just run it because there's no restrictions for those.
                    */
                    else
                        resultTable.setUpdate(sqlCommand);
                    
                    // This is just to help better manage the output text.
                    HTMLOutput = "The statement executed successfully.\n"
                               + "<br>\n";
                    
                    if(updatedAmount != 0)
                        HTMLOutput += (updatedAmount + " status records have been updated!");
                    
                    else
                        HTMLOutput += "No business logic required!";
                }
            
                catch (SQLException ex)
                {
                    HTMLOutput = "SQL ERROR: Check your SQL syntax and ensure you are entering proper table names/values.";
                }
            
                catch (IllegalStateException ex)
                {
                    HTMLOutput = "There is a database connection error. Please reload the application and check your MySQL Server.";
                }
            }
        }
        
        // We send our HTML formatted output to our home jsp.
        // We also make sure to keep the user input in the input field.
        request.setAttribute("command", sqlCommand);
        request.setAttribute("resultTable", HTMLOutput);
        request.getRequestDispatcher(home).forward(request, response);
    }
    
    // This is a function for increasing of the status of suppliers when necessary
    public int statusUpdate(ResultSetTableModel resultTable) throws SQLException
    {
        int updateCount = 0;
        String supplierList = "(";
        /*
            We can assume there can be more than one supplier we need to reference
            so we assume that it is a set '(...)' of suppliers.
        */
        
        /*
            We fetch the suppliers that need to have their status increased.
        */
        resultTable.setQuery("select distinct(suppliers.snum) from suppliers join shipments on suppliers.snum = shipments.snum and shipments.quantity >= 100");
        
        /*
            We do some string trickery to add them to a set to be updated.
        */
        for(int i = 0;i < resultTable.getRowCount();i++)
        {   
            /*
                We need to format the set very carefully or else SQL will have a
                hissy fit.
            
                We also keep track of the "update count" because we need to report
                how many status records have been updated.
            */
            supplierList += ("'" + resultTable.getValueAt(i, 0) + "'");
            updateCount++;
            
            if(i != (resultTable.getRowCount() - 1))
            {
                supplierList += ", ";
            }
            
            else
            {
                supplierList += ")";
            }
        }
        
        // We finally apply our update and report the number of records updated.
        resultTable.setUpdate("update suppliers set status = (status + 5) where snum in " + supplierList);
        
        return updateCount;
    }
    
    // This function allows the Table Model to be converted to HTML taple data.
    public String tableToHTML(ResultSetTableModel resultTable)
    {
        /*
            We start off with a table row made because we know our table won't
            be empty.
        */
        String HTMLTable = "<tr>\n";
        
        /*
            Add stylized table header data to the HTML table.
        */
        for(int i = 0;i < resultTable.getColumnCount();i++)
            HTMLTable += "<td style='text-align:center;'><b>" + resultTable.getColumnName(i) +"</b></td>\n";
        
        /*
            Close our current table row.
        */
        HTMLTable += "</tr>\n";
        
        /*
            Now we add the actual table data elements to the table.
        */
        for(int i = 0;i < resultTable.getRowCount();i++)
        {
            HTMLTable += "<tr>\n";
            
            /*
                We go cell by cell and ensure we get every element of data in
                the table.
            */
            for(int j = 0;j < resultTable.getColumnCount();j++)
                HTMLTable += "<td style='text-align:center;'>" + resultTable.getValueAt(i, j) + "</td>\n";
            
            HTMLTable += "</tr>\n";
        }
        
        return HTMLTable;
    }
}