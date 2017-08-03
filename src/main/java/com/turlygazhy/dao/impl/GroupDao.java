package com.turlygazhy.dao.impl;

import com.sun.org.apache.regexp.internal.RE;
import com.turlygazhy.entity.Group;
import com.turlygazhy.reminder.timer_task.EveryNightTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2/22/17.
 */
public class GroupDao {
    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    private final Connection connection;

    public GroupDao(Connection connection) {
        this.connection = connection;
    }

    public boolean isChatRegistered(Long chatId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM groups WHERE chat_id=?");
        ps.setLong(1, chatId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        return rs.next();
    }

    public void checkTitle(Long chatId, String title) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM groups WHERE chat_id=?");
        ps.setLong(1, chatId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        rs.next();
        String titleDB = rs.getString(2);
        if (!titleDB.equals(title)) {
            PreparedStatement psUpdate = connection.prepareStatement("UPDATE groups SET title=? WHERE user_id=?");
            psUpdate.setString(1, title);
            psUpdate.setLong(2, chatId);
            psUpdate.execute();
        }
    }

    public void insert(Long chatId, String title) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO groups VALUES(default, ?,?,?)");
        ps.setString(1, title);
        ps.setLong(2, chatId);
        ps.setBoolean(3, false);
        ps.execute();
        ps = connection.prepareStatement("INSERT INTO group_url VALUES (?,?);");
        ps.setInt(1, chatId.intValue());
        ps.setString(2, "url is not added yet");
        ps.execute();
    }

    public void giveAccess(Long chatId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("UPDATE groups SET can_work=? WHERE chat_id=?");
        ps.setBoolean(1, true);
        ps.setLong(2, chatId);
        ps.execute();
    }

    public List<Group> selectAll() throws SQLException {
        List<Group> result = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM groups");
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            boolean hasAccess = rs.getBoolean(4);
            if (hasAccess) {
                Group group = new Group();
                group.setId(rs.getInt(1));
                group.setTitle(rs.getString(2));
                group.setChatId(rs.getLong(3));
                result.add(group);
            }
        }
        return result;
    }

    public Group select(Integer groupId) throws SQLException {
        logger.info("select * from groups where id=" + groupId);
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM groups WHERE id=?");
        ps.setInt(1, groupId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        rs.next();
        Group group = new Group();
        group.setTitle(rs.getString(2));
        group.setChatId(rs.getLong(3));
        return group;
    }

    public void setUrlToGroup(int groupId, String url) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("UPDATE group_url SET url=? WHERE group_id=?");
        ps.setInt(2, groupId);
        ps.setString(1, url);
        ps.execute();
    }

    public String getGroupUrl(int groupId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM group_url WHERE group_id=?");
        ps.setInt(1, groupId);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();
        resultSet.next();
        return resultSet.getString(2);
    }

    public void deleteGroup(int groupId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM groups WHERE id=" + groupId);
        ps.execute();
        ps = connection.prepareStatement("DELETE FROM group_url WHERE group_id=" + groupId);
        ps.execute();
    }
}
