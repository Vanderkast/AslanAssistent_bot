package com.turlygazhy.reminder.timer_task;

import com.turlygazhy.Bot;
import com.turlygazhy.entity.*;
import com.turlygazhy.exception.NoResultForChartException;
import com.turlygazhy.reminder.Reminder;
import com.turlygazhy.tool.Chart;
import com.turlygazhy.tool.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yerassyl_Turlygazhy on 02-Mar-17.
 */
public class EveryNightTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(EveryNightTask.class);
    private long groupChatId;

    public EveryNightTask(Bot bot, Reminder reminder) {
        super(bot, reminder);
    }


    @Override
    public void run() {
        logger.info("Start run");
        reminder.setNextNightTask();
        try {
            List<Member> members = memberDao.selectAll();
            SendResultToGroup sendResultToGroup = new SendResultToGroup();//отправка результатов в группу
            WeekResult weekResult = new WeekResult(goalDao, savedResultsDao);
            MonthResult monthResult = new MonthResult(goalDao, savedResultsDao);
            DayResult dayResult = new DayResult();
            for (Member member : members) {
                try {
                    //отсылаются данные в беседу
                    int groupId = member.getGroupId();
                    Group group = groupDao.select(groupId);
                    groupChatId = group.getChatId();
                    Integer userId = member.getUserId();
                    List<UserResult> results = goalDao.getForUser(userId);//результаты пользователя (не сохраненные в архив)
                    String firstName = member.getFirstName();
                    String resultText = getResultText(results);

                    UserReadingResult reading = goalDao.getReadingResultForUser(userId);
                    resultText = "<b>" + firstName + "</b>" + "\n" + buttonDao.getButtonText(3) + ": "
                            + reading.getCompleted() + "/" + reading.getAim() + "\n" + resultText.trim()
                            + "\n========================";

                    List<UserResult> dayResults = getOnPeriod(results, "day");

                    boolean DayAnalize = dayResult.analyze(savedResultsDao, goalDao, dayResults, DateUtil.getPastDay(), member);
                    if (DayAnalize) {
                        bot.sendMessage(new SendMessage()
                                .setText(member.getFirstName() + " " + messageDao.getMessageText(79))
                                .setChatId(groupDao.select(member.getGroupId()).getChatId()));
                    }


                    savedResultsDao.insert(userId, dayResults);
                    sendResultToGroup.addResult(groupChatId, resultText, bot);//отправляем в группу результаты
                    //savedResultsDao.insert(userId, results, reading);//сохранение результатов пользователя в архив
                    goalDao.resetResults(dayResults, userId);

                    if (DateUtil.isNewWeek()) {//
                        int readingResult = weekResult.analyze(reading);

                        List<UserResult> weekResults = getOnPeriod(results, "week");

                        savedResultsDao.insert(userId, weekResults);
                        savedResultsDao.insertReading(userId, reading);

                        //weekResult.analyze(member, weekResults, goalDao, readingResult);
                        boolean WeekAnalize = weekResult.analize(member, weekResults, readingResult);
                        if (WeekAnalize) {
                            bot.sendMessage(new SendMessage()
                                    .setText(member.getFirstName() + " " + messageDao.getMessageText(80))
                                    .setChatId(groupDao.select(member.getGroupId()).getChatId()));
                        }

                        goalDao.resetResults(weekResults, userId);
                        goalDao.resetReadingCompleted(userId);

                        savedResultsDao.clearHistory(member.getUserId(), "day");
                    }
                    if (DateUtil.isNewMonth()) {//
                        List<UserResult> monthResults = getOnPeriod(results, "month");

                        savedResultsDao.insert(userId, monthResults);
                        goalDao.resetResults(monthResults, userId);

                        boolean MonthAnalize = monthResult.analize(member, monthResults);
                        if (MonthAnalize) {
                            bot.sendMessage(new SendMessage()
                                    .setText(member.getFirstName() + " " + messageDao.getMessageText(81))
                                    .setChatId(groupDao.select(member.getGroupId()).getChatId()));

                            if (savedResultsDao.getFallStatus(member, DateUtil.getFormatted(DateUtil.getLastMonthFirstDay()), "month result")) {
                                bot.sendMessage(new SendMessage()
                                        .setText(member.getFirstName() + " " + messageDao.getMessageText(84))
                                        .setChatId(groupDao.select(member.getGroupId()).getChatId()));
                                memberDao.deleteMember(member);
                                savedResultsDao.clearHistory(member.getUserId(), "month");

                            } else {
                                savedResultsDao.clearHistory(member.getUserId(), "month");
                            }
                        } else {
                            savedResultsDao.clearHistory(member.getUserId(), "month");
                        }

                        savedResultsDao.clearHistory(member.getUserId(), "week");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.info("sql exception was cached in EveryNightTask.java");
                } catch (TelegramApiException e){
                    e.printStackTrace();
                   logger.info("TelegramApiException was catched");
                }
            }
            /*if (DateUtil.isNewWeek()) {//
                weekResult.send(bot, messageDao, groupDao, groupChatId);
                sendWeekChart(bot);
            }
            if (DateUtil.isNewMonth()) {//
                monthResult.send(bot, messageDao, groupDao);
                sendMonthChart(bot);
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendWeekChart(Bot bot) throws SQLException {
        List<Goal> goals = goalDao.selectAllGoals();
        for (Goal goal : goals) {
            List<SavedResult> savedResults = savedResultsDao.selectForGoal(goal.getId(), DateUtil.getLastWeekMonday(), DateUtil.getLastWeekSunday());
            try {
                sendPieChart(bot, goal.getName(), savedResults);
            } catch (NoResultForChartException ignored) {
            }
        }
        List<SavedResult> readingResults = savedResultsDao.selectForReading(DateUtil.getLastWeekMonday(), DateUtil.getLastWeekSunday());
        try {
            sendPieChart(bot, "Reading", readingResults);
        } catch (NoResultForChartException ignored) {
        }
    }

    private void sendPieChart(Bot bot, String goalName, List<SavedResult> savedResults) throws NoResultForChartException, SQLException {
        File file;
        FileInputStream fileInputStream;
        Chart chart = new Chart();
        String filePath = chart.getPieChart("Results of " + goalName, parseResults(savedResults));
        file = new File(filePath);
        try {
            fileInputStream = new FileInputStream(file + ".jpg");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int groupId = memberDao.selectByUserId(savedResults.get(0).getUserId()).getGroupId();
        long chatId = groupDao.select(groupId).getChatId();
        try {
            bot.sendPhoto(new SendPhoto()
                    .setChatId(chatId)
                    .setNewPhoto("photo", fileInputStream)
            );
        } catch (TelegramApiException ignored) {
        }
    }

    public void sendMonthChart(Bot bot) throws SQLException {
        List<Goal> goals = goalDao.selectAllGoals();
        for (Goal goal : goals) {
            List<SavedResult> savedResults = savedResultsDao.selectForGoalLastMonth(goal.getId());
            sendChart(bot, goal.getName(), savedResults);
        }
        List<SavedResult> readingResults = savedResultsDao.selectForReading(DateUtil.getLastMonthFirstDay(), DateUtil.getLastMonthLastDay());
        sendChart(bot, "Reading", readingResults);
    }

    private void sendChart(Bot bot, String goalName, List<SavedResult> savedResults) throws SQLException {
        File file;
        FileInputStream fileInputStream;
        Chart chart = new Chart();
        String filePath = chart.getChart(goalName, parseResults(savedResults));
        file = new File(filePath);
        try {
            fileInputStream = new FileInputStream(file + ".jpg");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        //int groupId = memberDao.selectByUserId(savedResults.get(0).getUserId()).getGroupId();
        groupChatId = groupDao.select(101).getChatId();//select
        try {
            bot.sendPhoto(new SendPhoto()
                    .setChatId(groupChatId)
                    .setNewPhoto("photo", fileInputStream)
            );
        } catch (TelegramApiException ignored) {
        }
    }

    private Map<String, List<SavedResult>> parseResults(List<SavedResult> savedResults) {
        Map<Integer, List<SavedResult>> userResults = new HashMap<>();
        for (SavedResult savedResult : savedResults) {
            Integer userId = savedResult.getUserId();
            List<SavedResult> results = userResults.get(userId);
            if (results == null) {
                results = new ArrayList<>();
                userResults.put(userId, results);
            }
            results.add(savedResult);
        }
        Map<String, List<SavedResult>> userNameAndResults = new HashMap<>();
        for (Map.Entry<Integer, List<SavedResult>> entry : userResults.entrySet()) {
            try {
                userNameAndResults.put(memberDao.getName(entry.getKey()), entry.getValue());
            } catch (SQLException ignored) {
            }
        }
        return userNameAndResults;
    }

    private String getResultText(List<UserResult> results) throws SQLException {
        String result = "";
        for (UserResult userResult : results) {
            Goal goal = goalDao.select(userResult.getGoalId());
            result = result + "\n" + goal.getName() + ": " + userResult.getCompleted() + "/" + goal.getAim();
        }
        return result;
    }

    private List<UserResult> getOnPeriod(List<UserResult> results, String period) throws SQLException {
        List<UserResult> results1 = new ArrayList<>();
        for (UserResult r : results) {
            if (goalDao.select(r.getGoalId()).getPeriod().equals(period)) {
                results1.add(r);
            }
        }
        return results1;
    }
}
