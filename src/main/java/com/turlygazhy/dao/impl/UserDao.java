package com.turlygazhy.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by user on 12/18/16.
 */
public class UserDao {
    private static final String SELECT_ADMIN_CHAT_ID = "SELECT * FROM PUBLIC.USER";
    private static final int PARAMETER_USER_ID = 1;
    private static final int CHAT_ID_COLUMN_INDEX = 2;
    public static final int ADMIN_ID = 1;
    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public Long getAdminChatId() {
        try {
            PreparedStatement ps = connection.prepareStatement(SELECT_ADMIN_CHAT_ID);
           // ps.setLong(PARAMETER_USER_ID, ADMIN_ID);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            rs.first();
            return rs.getLong(CHAT_ID_COLUMN_INDEX);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAdmin(int chatId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM USER WHERE CHAT_ID=?");
        ps.setInt(1, chatId);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();
        if(resultSet.first()){
            return true;
        } else {
            return false;
        }
    }

    public void setAdmin(long newAdminId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO USER VALUES(DEFAULT, ?)");
        ps.setInt(1, (int) newAdminId);
        ps.execute();
    }

    public void deleteAdmin(long adminId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM USER WHERE CHAT_ID=?");
        ps.setInt(1, (int) adminId);
        ps.execute();
    }
}
