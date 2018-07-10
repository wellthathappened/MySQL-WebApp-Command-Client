<!--
	Name: Ian Lewis
	Course: CNT 4714 - Summer 2017 - Project Four
	Assignment title: A Three-Tier Distributed Web-Based Application
	Date: August 1, 2017
-->
<html>
    <head>
        <title>CNT 4714 Remote Database Management System</title>
    </head>
    
    <body>
        <h1 style="text-align:center;">Welcome to the Project 4 Remote Database Management System</h1>
        
        <hr>
        <div id="middle" style="text-align:center;">
            <p>
                You are connected to the Project4 database.<br>
                Please enter any valid SQL query or update statement.<br>
                If no query/update command is given the Execute button will display all supplier information in the database.<br>
                All execution results will appear below.<br>
            </p>
            
			<!-- We declare the form to be sent to use the "handler" servlet. -->
            <form name="index" action='handler' method="POST">
                
                <textarea type="textarea" name="command" rows="30" cols="70" style="height: 300px; width: 700px;">${command}</textarea>
                
                <br>
                <br>
                
                <input type="submit" value="Execute Command" name="Submit" style="width: 125px;">
                <input type="reset" value="Clear Form" name="clear" style="width: 85px;">
            </form>
            
        </div>
        
        <hr>
        <div style="text-align:center;">
            <h3>Database Results:</h3>
            <table border='2' style='margin: auto;'>
                    
			<!-- This jsp code is simple since the servlet formats the result in HTML -->
            <!-- Simply take the string result from the servlet's operations and display it -->
            <%  
                String resultTable = (String)request.getAttribute("resultTable");
                        
                if(resultTable == null)
                    resultTable = "";
            %>
            
            <%= resultTable %>
                    
            </table>
        </div>
    </body>
</html>