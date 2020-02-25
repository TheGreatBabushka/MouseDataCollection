package sql;

import data.MouseData;

import java.io.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class MouseMovementDatabase
{
    String databaseName = "mouse-data.db";

    public boolean createDatabase(File dbFile)
    {
        if(!dbFile.exists())
            System.out.println("No file database found. Attempting to create a new one...");

        Connection conn = null;
        Statement stmt = null;
        try{
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

            System.out.println("Creating database...");
            stmt = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS mouse_data (PosX INT, PosY INT, SystemTime BIGINT)";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try { if(stmt!=null) stmt.close(); } catch(SQLException se2){}
            try{ if(conn!=null)  conn.close(); }catch(SQLException se){ se.printStackTrace(); }
        }
        return false;
    }

    public void setDatabaseName(String name)
    {
        databaseName = name;
    }

    public LinkedList<MouseData> loadMouseDataCSVGroup(File directory)
    {
        LinkedList<MouseData> data = new LinkedList<>();

        for(File f : directory.listFiles(pathname -> pathname.getPath().endsWith(".mscsv")))
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if(tokens.length != 3) {System.out.println("Warning: malformed line in " + f.getPath() + ": " + line); continue;}

                    int x = Integer.parseInt(tokens[0]);
                    int y = Integer.parseInt(tokens[1]);
                    long time = Long.parseLong(tokens[2]);
                    data.add(new MouseData(x, y, time));
                }

            }
            catch (FileNotFoundException e) { e.printStackTrace(); }
            catch (IOException e) { e.printStackTrace(); }

        }
        return data;
    }

    public LinkedList<MouseData> loadMouseDataSQLite(File database)
    {
        Connection conn = null;
        Statement stmt = null;
        LinkedList<MouseData> data = new LinkedList<>();
        try
        {
            conn = DriverManager.getConnection("jdbc:sqlite:" + database.getPath());
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT PosX, PosY, SystemTime FROM mouse_data;");

            while (rs.next()) {
                int x = rs.getInt("PosX");
                int y = rs.getInt("PosY");
                long time = rs.getLong("SystemTime");
                data.add(new MouseData(x, y, time));
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { if(stmt!=null) stmt.close(); } catch(SQLException se2){}
            try{ if(conn!=null)  conn.close(); }catch(SQLException se){ se.printStackTrace(); }
            return data;
        }
    }


    public void save(File dbFile, List<MouseData> data)
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        try
        {
            if(!dbFile.exists())
                createDatabase(dbFile);

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
            stmt = conn.prepareStatement("INSERT INTO mouse_data VALUES (?, ?, ?)");
            int i = 0;

            for (MouseData md : data) {
                stmt.setInt(1, md.x);
                stmt.setInt(2, md.y);
                stmt.setLong(3, md.time);
                stmt.addBatch();

                i++;

                if (i % 500 == 0 || i == data.size())
                {
                    stmt.executeBatch(); // Execute every 500 items.
                    System.out.println("Exporting to DB... " + i + "/" + data.size());
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { if(stmt!=null) stmt.close(); } catch(SQLException se2){}
            try{ if(conn!=null)  conn.close(); }catch(SQLException se){ se.printStackTrace(); }
        }
    }
}
