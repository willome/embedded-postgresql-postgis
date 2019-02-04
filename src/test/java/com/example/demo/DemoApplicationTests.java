package com.example.demo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgis.PGbox2d;
import org.postgis.PGbox3d;
import org.postgis.PGgeometry;
import org.postgresql.Driver;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.PreparedDbRule;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DemoApplication.class, DemoApplicationTests.TestConfig.class })
@ImportAutoConfiguration(exclude = { LiquibaseAutoConfiguration.class, DataSourceAutoConfiguration.class })
public class DemoApplicationTests {

    @ClassRule
    public static PreparedDbRule db = EmbeddedPostgresRules.preparedDatabase(new PostgisPreparer())
            .customize(c -> c.setPgBinaryResolver(new ClasspathBinaryResolver()));

    @Configuration
    public static class TestConfig {

        @Bean
        public DataSource ds() {
            return db.getTestDatabase();
        }

    }

    @Autowired
    DataSource ds;

    @Test
    public void test_Postgis() {

        System.out.println("Driver version: " + Driver.getVersion());
        int major;
        try {
            major = new Driver().getMajorVersion();
        }
        catch (Exception e) {
            System.err.println("Cannot create Driver instance: " + e.getMessage());
            System.exit(1);
            return;
        }

        if (major < 8) {
            System.err.println("Your pgdjbc " + major
                    + ".X is too old, it does not support autoregistration!");
            return;
        }

        Connection conn = null;
        Statement stat = null;
        try {
            conn = ds.getConnection();
            stat = conn.createStatement();
        }
        catch (SQLException e) {
            System.err.println("Connection initialization failed, aborting.");
            e.printStackTrace();
            System.exit(1);
            // signal the compiler that code flow ends here:
            throw new AssertionError();
        }

        int postgisServerMajor = 0;
        try {
            postgisServerMajor = getPostgisMajor(stat);
        }
        catch (SQLException e) {
            System.err.println("Error fetching PostGIS version: " + e.getMessage());
            System.err.println("Is PostGIS really installed in the database?");
            System.exit(1);
            // signal the compiler that code flow ends here:
            throw new AssertionError();
        }

        System.out.println("PostGIS Version: " + postgisServerMajor);

        PGobject result = null;

        /* Test geometries */
        try {
            ResultSet rs = stat.executeQuery("SELECT 'POINT(1 2)'::geometry");
            rs.next();
            result = (PGobject) rs.getObject(1);
            if (result instanceof PGgeometry) {
                System.out.println("PGgeometry successful!");
            }
            else {
                System.out.println("PGgeometry failed!");
            }
        }
        catch (SQLException e) {
            System.err.println("Selecting geometry failed: " + e.getMessage());
            System.exit(1);
            // Signal the compiler that code flow ends here.
            return;
        }

        /* Test box3d */
        try {
            ResultSet rs = stat.executeQuery("SELECT 'BOX3D(1 2 3, 4 5 6)'::box3d");
            rs.next();
            result = (PGobject) rs.getObject(1);
            if (result instanceof PGbox3d) {
                System.out.println("Box3d successful!");
            }
            else {
                System.out.println("Box3d failed!");
            }
        }
        catch (SQLException e) {
            System.err.println("Selecting box3d failed: " + e.getMessage());
            System.exit(1);
            // Signal the compiler that code flow ends here.
            return;
        }

        /* Test box2d if appropriate */
        if (postgisServerMajor < 1) {
            System.out.println("PostGIS version is too old, skipping box2ed test");
            System.err.println("PostGIS version is too old, skipping box2ed test");
        }
        else {
            try {
                ResultSet rs = stat.executeQuery("SELECT 'BOX(1 2,3 4)'::box2d");
                rs.next();
                result = (PGobject) rs.getObject(1);
                if (result instanceof PGbox2d) {
                    System.out.println("Box2d successful!");
                }
                else {
                    System.out.println("Box2d failed! " + result.getClass().getName());
                }
            }
            catch (SQLException e) {
                System.err.println("Selecting box2d failed: " + e.getMessage());
                System.exit(1);
                // Signal the compiler that code flow ends here.
                return;
            }
        }

        System.out.println("Finished.");
    }

    public static int getPostgisMajor(Statement stat) throws SQLException {
        ResultSet rs = stat.executeQuery("SELECT postgis_version()");
        rs.next();
        String version = rs.getString(1);
        if (version == null) {
            throw new SQLException("postgis_version returned NULL!");
        }
        version = version.trim();
        int idx = version.indexOf('.');
        return Integer.parseInt(version.substring(0, idx));
    }

}
