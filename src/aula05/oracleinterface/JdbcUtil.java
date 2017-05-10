package aula05.oracleinterface;

import java.sql.*;

public class JdbcUtil {

    public static void enable_dbms_output(Connection conn, int buffer_size) {
        System.out.print("Enabling DBMS_OUTPUT - ");
        try {
            CallableStatement stmt = conn.prepareCall("{call sys.dbms_output.enable(?) }");
            stmt.setInt(1, buffer_size);
            stmt.execute();
            //System.out.println("Enabled!");
        } catch (Exception e) {
            //System.out.println("Problem occurred while trying to enable dbms_output! " + e.toString());
        }
    }

    public static String print_dbms_output(Connection conn, String nomeJogo) {
        //System.out.println("Dumping DBMS_OUTPUT to System.out : ");
        String out = new String();
        try {
            CallableStatement stmt = conn.prepareCall("{call DADOSJOGO(?,?)}");
            stmt.setString(1, nomeJogo.toUpperCase());
            stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
            stmt.executeUpdate();
            out = stmt.getString(2);
        } catch (Exception e) {
            //System.out.println("Problem occurred during dump of dbms_output! " + e.toString());
        }
        return out;
    }

    /**
     * Simply creates a connection to an Oracle-database
     *
     * @param jdbc
     * @param userid
     * @param passwd
     * @param autocommit
     * @return
     * @throws java.sql.SQLException
     */
    public static Connection getConnection(String jdbc, String userid, String passwd, boolean autocommit) throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        Connection conn = DriverManager.getConnection(jdbc, userid, passwd);
        conn.setAutoCommit(autocommit);
// ((OracleConnection)conn).setDefaultExecuteBatch (150);
        return conn;
    }
}
