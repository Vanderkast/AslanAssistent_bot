package com.turlygazhy.entity;

import com.turlygazhy.Bot;
import com.turlygazhy.dao.GoalDao;
import com.turlygazhy.dao.impl.GroupDao;
import com.turlygazhy.dao.impl.MessageDao;
import com.turlygazhy.dao.impl.SavedResultsDao;
import com.turlygazhy.dao.impl.UserDao;
import com.turlygazhy.tool.DateUtil;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import sun.nio.cs.US_ASCII;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 10-Mar-17.
 */
public class WeekResult {
    private List<Member> fall = new ArrayList<>();
    private List<Member> closeToFall = new ArrayList<>();

    private GoalDao goalDao;
    private SavedResultsDao savedResultsDao;

    public WeekResult(GoalDao goalDao, SavedResultsDao savedResultsDao) {
        this.goalDao = goalDao;
        this.savedResultsDao = savedResultsDao;
    }

    //анализ того, по скольки параметрам юзер не достиг гапа в целях
    public void analyze(Member member, List<UserResult> results, GoalDao goalDao, int readingResult) throws SQLException {
        int fallCount = readingResult;
        for (UserResult result : results) {
            Goal goal = goalDao.select(result.getGoalId());
            if (goal.getPeriod().equals("week")) {
                if (!(result.getCompleted() >= goal.getAim())) {
                    fallCount++;
                }
                if (fallCount >= (results.size() / 2)) {
                    fall.add(member);
                    return;
                }
            }
        }
        if (fallCount > 0) {
            closeToFall.add(member);
        }
    }

    public boolean analize(Member member, List<UserResult> results, int readingResult) throws SQLException {
        List<String> lastWeek = DateUtil.getLastWeek();

        int count = 0;
        for(String day : lastWeek){
            if(savedResultsDao.getFallStatus(member, day, "day")){
                count++;
            }
        }
        int daysCount = results.size();

        int weekCount = 0;
        for(UserResult r :results){
            if(r.getCompleted() < goalDao.select(r.getGoalId()).getAim()){
                weekCount++;
            }
        }
        weekCount = weekCount + readingResult;

        savedResultsDao.setFalledMember(lastWeek.get(0), member, "week", isFall(count, daysCount,weekCount));
        return isFall(count, daysCount, weekCount);
    }

    //если провалов по дневным нормам больше 3, то добавляется 1 к возмодному недельному провалу
    // если количество провалов по недельным нормам больше чем количество целей всего минус кол-во дневных целей плюс чтение(1)
    private boolean isFall(int dayStatus, int daysCount,int weekStatus) throws SQLException{
        int border = (goalDao.selectAllGoals().size() - daysCount - goalDao.countOfGoalsByPeriod("month")) / 2;
        if(dayStatus > 3){
            if (weekStatus > border){
                return true;
            } else {
                return false;
            }
        }else {
            if(weekStatus > border + 1){
                return true;
            } else {
                return false;
            }
        }
    }

    public int analyze(UserReadingResult reading) {
        if (reading.getCompleted() < reading.getAim()) {
            return 0;
        }
        return 1;
    }

    public void send(Bot bot, MessageDao messageDao, GroupDao groupDao, long groupChatId) throws TelegramApiException, SQLException {
        if (fall.size() == 0) {
            //for (Group group : groupDao.selectAll()) {
            bot.sendMessage(new SendMessage()
                    .setChatId(groupChatId)//group.getChatId()
                    .setText(messageDao.getMessageText(55))
            );
            //}
        } else {
            for (Member member : fall) {
                bot.sendMessage(new SendMessage()
                        .setChatId(member.getChatId())//groupDao.select(member.getGroupId()).getChatId()       (long) -236873508
                        .setText(member.getFirstName() + " " + messageDao.getMessageText(56))
                );
            }
        }
        if (closeToFall.size() > 0) {
            for (Member member : closeToFall) {
                bot.sendMessage(new SendMessage()
                        .setChatId(member.getChatId())//groupDao.select(member.getGroupId()).getChatId()
                        .setText(member.getFirstName() + " " + messageDao.getMessageText(57))
                );
            }
        }
    }
}
