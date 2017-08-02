package com.turlygazhy.dao.impl;

import com.sun.org.apache.regexp.internal.RE;
import com.turlygazhy.entity.Member;
import com.turlygazhy.entity.SavedResult;
import com.turlygazhy.entity.UserReadingResult;
import com.turlygazhy.entity.UserResult;
import com.turlygazhy.tool.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Yerassyl_Turlygazhy on 03-Mar-17.
 */
public class SavedResultsDao {
    private final Connection connection;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");


    public SavedResultsDao(Connection connection) {
        this.connection = connection;
    }

    public List<SavedResult> select(Integer userId, Date dateFrom, Date dateTill) throws SQLException {
        List<SavedResult> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM SAVED_RESULTS WHERE user_id=? AND IS_READING=FALSE");
        ps.setInt(1, userId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            Date dbFrom = convertDBDate(rs.getString(3));
            boolean checkFromDate = isFirstGreaterOrEqual(dbFrom, dateFrom);
            boolean checkTillDate = isFirstGreaterOrEqual(dateTill, dbFrom);
            if (checkFromDate && checkTillDate) {
                SavedResult savedResult = new SavedResult();
                savedResult.setId(rs.getInt(1));
                savedResult.setUserId(userId);
                savedResult.setDate(dbFrom);
                savedResult.setResult(rs.getInt(4));
                savedResult.setGoalId(rs.getInt(5));
                results.add(savedResult);
            }
        }

        Collections.sort(results, new Comparator<SavedResult>() {
            @Override
            public int compare(SavedResult first, SavedResult second) {
                Date firstDate = first.getDate();
                Date secondDate = second.getDate();
                int yearDifference = firstDate.getYear() - secondDate.getYear();
                if (yearDifference != 0) {
                    return yearDifference;
                } else {
                    int monthDifference = firstDate.getMonth() - secondDate.getMonth();
                    if (monthDifference != 0) {
                        return monthDifference;
                    } else {
                        return firstDate.getDate() - secondDate.getDate();
                    }
                }
            }
        });

        return results;
    }

    private boolean isFirstGreaterOrEqual(Date first, Date second) {
        if (first.getYear() > second.getYear()) {
            return true;
        } else {
            if (first.getYear() == second.getYear()) {
                if (first.getMonth() > second.getMonth()) {
                    return true;
                } else {
                    if (first.getMonth() == second.getMonth()) {
                        if (first.getDate() >= second.getDate()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Date convertDBDate(String date) {
        try {
            return format.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(Integer userId, List<UserResult> results, UserReadingResult reading) throws SQLException {
        List<SavedResult> alreadyCounted = selectThisWeek(userId);
        PreparedStatement ps = connection.prepareStatement("INSERT INTO SAVED_RESULTS VALUES(default, ?, ?, ?, ?, ?)");
        ps.setInt(1, userId);
        ps.setString(2, DateUtil.getPastDay());
        for (UserResult result : results) {
            int completed = result.getCompleted();
            int goalId = result.getGoalId();
            int counted = 0;
            for (SavedResult savedResult : alreadyCounted) {
                if (savedResult.getGoalId() == goalId) {
                    counted = counted + savedResult.getResult();
                }
            }
            ps.setInt(3, completed - counted);
            ps.setInt(4, goalId);
            ps.setBoolean(5, false);
            ps.execute();
        }
        insertReading(userId, reading);
    }

    public void insert(Integer userId, List<UserResult> results) throws SQLException {
        List<SavedResult> alreadyCounted = selectThisWeek(userId);
        PreparedStatement ps = connection.prepareStatement("INSERT INTO SAVED_RESULTS VALUES(default, ?, ?, ?, ?, ?)");
        ps.setInt(1, userId);
        ps.setString(2, DateUtil.getPastDay());
        for (UserResult result : results) {
            int completed = result.getCompleted();
            int goalId = result.getGoalId();
            int counted = 0;
            for (SavedResult savedResult : alreadyCounted) {
                if (savedResult.getGoalId() == goalId) {
                    counted = counted + savedResult.getResult();
                }
            }
            ps.setInt(3, completed - counted);
            ps.setInt(4, goalId);
            ps.setBoolean(5, false);
            ps.execute();
        }
    }

    public List<SavedResult> selectThisWeek(Integer userId) throws SQLException {
        return select(userId, DateUtil.getThisMonday(), DateUtil.getThisSunday());
    }

    public void insertReading(Integer userId, UserReadingResult reading) throws SQLException {
        //значения для цели "чтение" goal_id=0 и is_reading=true
        PreparedStatement ps = connection.prepareStatement("INSERT INTO SAVED_RESULTS VALUES(default, ?, ?, ?, ?, ?)");
        ps.setInt(1, userId);
        ps.setString(2, DateUtil.getPastDay());
        ps.setInt(3, reading.getCompleted());
        ps.setInt(4, 0);
        ps.setBoolean(5, true);
        ps.execute();
    }

    public List<SavedResult> selectThisWeekForReading(Integer userId) throws SQLException {
        return selectForReading(userId, DateUtil.getThisMonday(), DateUtil.getThisSunday());
    }

    public List<SavedResult> selectForReading(Integer userId, Date dateFrom, Date dateTill) throws SQLException {
        List<SavedResult> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM SAVED_RESULTS WHERE user_id=? AND IS_READING=TRUE");
        ps.setInt(1, userId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            Date dbFrom = convertDBDate(rs.getString(3));
            boolean checkFromDate = isFirstGreaterOrEqual(dbFrom, dateFrom);
            boolean checkTillDate = isFirstGreaterOrEqual(dateTill, dbFrom);
            if (checkFromDate && checkTillDate) {
                SavedResult savedResult = new SavedResult();
                savedResult.setId(rs.getInt(1));
                savedResult.setUserId(userId);
                savedResult.setDate(dbFrom);
                savedResult.setResult(rs.getInt(4));
                results.add(savedResult);
            }
        }

        Collections.sort(results, new Comparator<SavedResult>() {
            @Override
            public int compare(SavedResult first, SavedResult second) {
                Date firstDate = first.getDate();
                Date secondDate = second.getDate();
                int yearDifference = firstDate.getYear() - secondDate.getYear();
                if (yearDifference != 0) {
                    return yearDifference;
                } else {
                    int monthDifference = firstDate.getMonth() - secondDate.getMonth();
                    if (monthDifference != 0) {
                        return monthDifference;
                    } else {
                        return firstDate.getDate() - secondDate.getDate();
                    }
                }
            }
        });

        return results;
    }

    public List<SavedResult> select(Integer userId, String dateAsString) throws SQLException {
//        List<SavedResult> results = new ArrayList<>();
//        PreparedStatement ps = connection.prepareStatement("SELECT * FROM SAVED_RESULTS_READING where user_id=?");
//        ps.setInt(1, userId);
//        ps.execute();
//        ResultSet rs = ps.getResultSet();
//        while (rs.next()) {
//            Date dbFrom = convertDBDate(rs.getString(3));
//            boolean checkFromDate = isFirstGreaterOrEqual(dbFrom, dateFrom);
//            boolean checkTillDate = isFirstGreaterOrEqual(dateTill, dbFrom);
//            if (checkFromDate && checkTillDate) {
//                SavedResult savedResult = new SavedResult();
//                savedResult.setId(rs.getInt(1));
//                savedResult.setUserId(userId);
//                savedResult.setDate(dbFrom);
//                savedResult.setResult(rs.getInt(4));
//                results.add(savedResult);
//            }
//        }
//
//        Collections.sort(results, new Comparator<SavedResult>() {
//            @Override
//            public int compare(SavedResult first, SavedResult second) {
//                Date firstDate = first.getDate();
//                Date secondDate = second.getDate();
//                int yearDifference = firstDate.getYear() - secondDate.getYear();
//                if (yearDifference != 0) {
//                    return yearDifference;
//                } else {
//                    int monthDifference = firstDate.getMonth() - secondDate.getMonth();
//                    if (monthDifference != 0) {
//                        return monthDifference;
//                    } else {
//                        return firstDate.getDate() - secondDate.getDate();
//                    }
//                }
//            }
//        });
//
//        return results;
        return null;
    }

    public List<SavedResult> selectForLastMonth(Integer userId) throws SQLException {
        return select(userId, DateUtil.getLastMonthFirstDay(), DateUtil.getLastMonthLastDay());
    }

    public List<SavedResult> selectForReadingForLastMonth(Integer userId) throws SQLException {
        return selectForReading(userId, DateUtil.getLastMonthFirstDay(), DateUtil.getLastMonthLastDay());
    }

    public List<SavedResult> selectForGoalLastMonth(int goalId) throws SQLException {
        return selectForGoal(goalId, DateUtil.getLastMonthFirstDay(), DateUtil.getLastMonthLastDay());
    }

    public List<SavedResult> selectForGoal(int goalId, Date dateFrom, Date dateTo) throws SQLException {
        List<SavedResult> savedResults = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM SAVED_RESULTS WHERE goal_id=?");
        ps.setInt(1, goalId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            String date = rs.getString(3);
            Date goalDate;
            try {
                goalDate = format.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if (isFirstGreaterOrEqual(goalDate, dateFrom) && isFirstGreaterOrEqual(dateTo, goalDate)) {
                SavedResult savedResult = new SavedResult();
                savedResult.setDate(goalDate);
                int result = rs.getInt(4);
                savedResult.setResult(result);
                int userId = rs.getInt(2);
                savedResult.setUserId(userId);
                savedResults.add(savedResult);
            }
        }
        return savedResults;
    }

    public List<SavedResult> selectForReading(Date dateFrom, Date dateTill) throws SQLException {
        List<SavedResult> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM SAVED_RESULTS WHERE IS_READING=TRUE");
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            Date dbFrom = convertDBDate(rs.getString(3));
            boolean checkFromDate = isFirstGreaterOrEqual(dbFrom, dateFrom);
            boolean checkTillDate = isFirstGreaterOrEqual(dateTill, dbFrom);
            if (checkFromDate && checkTillDate) {
                SavedResult savedResult = new SavedResult();
                savedResult.setId(rs.getInt(1));
                savedResult.setUserId(rs.getInt(2));
                savedResult.setDate(dbFrom);
                savedResult.setResult(rs.getInt(4));
                results.add(savedResult);
            }
        }

        Collections.sort(results, new Comparator<SavedResult>() {
            @Override
            public int compare(SavedResult first, SavedResult second) {
                Date firstDate = first.getDate();
                Date secondDate = second.getDate();
                int yearDifference = firstDate.getYear() - secondDate.getYear();
                if (yearDifference != 0) {
                    return yearDifference;
                } else {
                    int monthDifference = firstDate.getMonth() - secondDate.getMonth();
                    if (monthDifference != 0) {
                        return monthDifference;
                    } else {
                        return firstDate.getDate() - secondDate.getDate();
                    }
                }
            }
        });

        return results;
    }

    public void setFalledMember(String date, Member member, String period, boolean isFall) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO FALL_MEMBER VALUES(?, ?, ?, ?)");
        ps.setInt(1, member.getUserId());
        ps.setBoolean(2, isFall);
        ps.setString(3, period);
        ps.setString(4, date);
        ps.execute();
    }

    public boolean getFallStatus(Member member, String date, String period) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM FALL_MEMBER WHERE USER_ID=? AND DATE=? AND PERIOD=?");
        ps.setInt(1, member.getUserId());
        ps.setString(2, date);
        ps.setString(3, period);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();
        resultSet.first();
        return resultSet.getBoolean(2);
    }

    public List<Boolean> getFallStatus(Member member, String period) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM FALL_MEMBER WHERE USER_ID=? AND PERIOD=?");
        ps.setInt(1, member.getUserId());
        ps.setString(2, period);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();

        List<Boolean> list = new ArrayList<>();
        while (resultSet.next()){
            list.add(resultSet.getBoolean(2));
        }

        return list;
    }

    public void deleteFallHistory() throws SQLException{
        PreparedStatement ps = connection.prepareStatement("DELETE FROM FALL_MEMBER");
        ps.execute();
    }

    public void clearHistory(int userId, String period) throws SQLException{
        PreparedStatement ps = connection.prepareStatement("DELETE FROM FALL_MEMBER WHERE USER_ID=? AND PERIOD=?");
        ps.setInt(1, userId);
        ps.setString(2, period);
        ps.execute();
    }
}
