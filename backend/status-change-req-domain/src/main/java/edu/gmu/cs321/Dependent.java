package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Dependent {

    private int id;
    private Connection conn;

    /*
     * Dependent constructor
     * Takes an existing Person ID and a DB connection,
     * then inserts this Dependent into the Dependent's table.
     */
    public Dependent(int id, Connection conn) {
        this.id = id;
        this.conn = conn;

        boolean ok = addToDB();
        if (!ok) {
            System.out.println("ERROR OCCURRED. Unable to add Dependent to DB.");
            System.exit(-1);
        }
    }

    /*
     * addToDB uses this Dependent's fields to create a new row
     * in the Dependents table.
     * Returns true if insert succeeded, false otherwise.
     */
    public boolean addToDB() {
        String sql = "INSERT INTO public.Dependents (id) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("SUCCESSFULLY INSERTED DEPENDENT");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ========= GETTER =========

    public int getId() {
        return id;
    }

    // ========= SETTER (DB + object) =========
    // This updates the ID in the Dependents table AND in this object.
    public void setId(int newId) {
        String sql = "UPDATE public.Dependents SET id = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // new ID value
            stmt.setInt(1, newId);
            // current ID value (row to update)
            stmt.setInt(2, this.id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                this.id = newId;  // only change in memory if DB update succeeded
            } else {
                System.out.println("No Dependent row found with id = " + this.id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
