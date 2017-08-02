package com.turlygazhy.dao.impl;

import com.turlygazhy.dao.AbstractDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanderkast on 24.06.2017.
 * позволяет сохранять, удалять и получать активности пользователя
 */
public class ActionHistoryDao extends AbstractDao {
    private final Connection connection;

    public ActionHistoryDao(Connection connection) {
        this.connection = connection;
    }

    public List<String> getActionHistoryList(int userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM ACTION_HISTORY WHERE USER_ID=?");
        ps.setInt(1, userId);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();

        List<String> actionList = new ArrayList<>();
        while (resultSet.next()) {
            actionList.add(resultSet.getString(2));
        }
        return actionList;
    }

    public String getLastAction(int userId) throws SQLException {
        List<String> list = getActionHistoryList(userId);

        return list.get(list.size() - 1);
    }

    public void deleteAllActionHistory(int userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM ACTION_HISTORY WHERE USER_ID=?");
        ps.setInt(1, userId);
        ps.execute();
    }

    public void addAction(int userId, String actionName) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO ACTION_HISTORY VALUES(DEFAULT, ?, ?) ");
        ps.setInt(2, userId);
        ps.setString(1, actionName);
        ps.execute();
    }

    public void deleteLastAction(int userId) throws SQLException {
        String actionName = getLastAction(userId);
        int lastActionId = getLastActionId(userId, actionName);
        PreparedStatement ps = connection.prepareStatement("DELETE FROM ACTION_HISTORY WHERE ID=?");
        ps.setInt(1, lastActionId);
        ps.execute();
    }

    public int getLastActionId(int userId, String actionName) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM ACTION_HISTORY WHERE ACTION_NAME=? AND USER_ID=?");
        ps.setString(1, actionName);
        ps.setInt(2, userId);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();
        resultSet.last();
        return resultSet.getInt(1);
    }
}
