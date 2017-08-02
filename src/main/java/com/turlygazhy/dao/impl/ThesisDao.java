package com.turlygazhy.dao.impl;

import com.turlygazhy.dao.GoalDao;
import com.turlygazhy.entity.Thesis;
import com.turlygazhy.entity.UserReadingResult;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 3/1/2017.
 */
public class ThesisDao {
    private final Connection connection;

    public ThesisDao(Connection connection) {
        this.connection = connection;
    }

    /*public void insert(UserReadingResult readingResult, String userName) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Thesis VALUES(default, ?, ?, ?)");
        ps.setString(1, readingResult.getBookName());
        ps.setString(2, userName);
        ps.setString(3, readingResult.getThesis());
        ps.execute();
    }*/

    public List<Thesis> selectAll() throws SQLException {
        List<Thesis> thesisList = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM thesis");
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            Thesis thesis = new Thesis();
            thesis.setId(rs.getInt(1));
            thesis.setBookName(rs.getString(3));
            thesis.setUserId(rs.getInt(2));
            thesis.setThesis(rs.getString(4));
            thesisList.add(thesis);
        }
        return thesisList;
    }

    public Thesis select(int thesisId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM thesis WHERE id=?");
        ps.setInt(1, thesisId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        rs.next();

        Thesis thesis = new Thesis();
        thesis.setId(rs.getInt(1));
        thesis.setBookName(rs.getString(3));
        thesis.setUserId(rs.getInt(2));
        thesis.setThesis(rs.getString(4));

        return thesis;
    }

    public ArrayList<String> getBooksNames(int userId) throws SQLException {
        ArrayList<String> books = new ArrayList<>();

        PreparedStatement ps = connection.prepareStatement("SELECT * FROM BOOKS WHERE USER_ID=?");
        ps.setInt(1, userId);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            books.add(rs.getString(2));
        }
        return books;
    }

    public ArrayList<Thesis> getAllThesis(int userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT  * FROM THESIS WHERE USER_ID=?");
        ps.setInt(1, userId);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();

        ArrayList<Thesis> theses = new ArrayList<>();

        while (resultSet.next()) {
            Thesis t = new Thesis();
            t.setId(resultSet.getInt(1));
            t.setUserId(resultSet.getInt(2));
            t.setBookName(resultSet.getString(3));
            t.setThesis(resultSet.getString(4));
            theses.add(t);
        }

        return theses;
    }

    public InlineKeyboardMarkup getThesisInlineKeyboard(int userId) throws SQLException {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<Thesis> theses = getAllThesis(userId);
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Thesis t : theses) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            String text = t.getId() + " книга  " + t.getBookName() + " : " + t.getThesis().substring(1, t.getThesis().length() / 2) + "\n";

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(text);
            button.setCallbackData(String.valueOf(t.getId()));

            row.add(button);
            buttons.add(row);
        }
        keyboardMarkup.setKeyboard(buttons);

        return keyboardMarkup;
    }

    public String getThesisText(Thesis thesis) {
        String text = thesis.getId() + " книга " + thesis.getBookName() + " : " + thesis.getThesis();
        return text;
    }

    public void SaveThesis(String text, int userId) throws SQLException {
        String bookName = getBookName(userId);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO THESIS VALUES (DEFAULT , ?, ?, ?)");
        ps.setInt(1, userId);
        ps.setString(2, bookName);
        ps.setString(3, text);
        ps.execute();
    }

    private String getBookName(int userId)throws SQLException{
        PreparedStatement dop = connection.prepareStatement("SELECT * FROM BOOKS WHERE USER_ID=?");
        dop.setInt(1,userId);
        dop.execute();
        ResultSet resultSet = dop.getResultSet();
        resultSet.last();
        return resultSet.getString(2);
    }

    public void deleteThesis(int thesisId) throws SQLException{
        PreparedStatement ps = connection.prepareStatement("DELETE FROM THESIS WHERE ID=?");
        ps.setInt(1,thesisId);
        ps.execute();
    }
}
