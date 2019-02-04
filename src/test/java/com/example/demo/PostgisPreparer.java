package com.example.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.opentable.db.postgres.embedded.DatabasePreparer;

public class PostgisPreparer implements DatabasePreparer {

    @Override
    public void prepare(DataSource ds) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = ds.getConnection();
            statement = connection.createStatement();
            statement.execute("CREATE EXTENSION postgis;");
            statement.execute("CREATE EXTENSION postgis_topology;");
            //statement.execute("CREATE EXTENSION postgis_sfcgal;");
            statement.execute("CREATE EXTENSION fuzzystrmatch;");
            statement.execute("CREATE EXTENSION address_standardizer;");
            statement.execute("CREATE EXTENSION address_standardizer_data_us;");
            statement.execute("CREATE EXTENSION postgis_tiger_geocoder;");
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

}
