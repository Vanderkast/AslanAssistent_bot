package com.turlygazhy.entity;

import com.turlygazhy.Bot;
import com.turlygazhy.dao.GoalDao;
import com.turlygazhy.dao.impl.GroupDao;
import com.turlygazhy.dao.impl.MessageDao;
import com.turlygazhy.dao.impl.SavedResultsDao;
import com.turlygazhy.tool.DateUtil;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.jws.soap.SOAPBinding;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 29-Mar-17.
 */
public class MonthResult {
    private List<Member> fall = new ArrayList<>();

    private GoalDao goalDao;
    private SavedResultsDao savedResultsDao;

    public MonthResult(GoalDao goalDao, SavedResultsDao savedResultsDao) {
        this.goalDao = goalDao;
        this.savedResultsDao = savedResultsDao;
    }


    public void analyze(Member member, SavedResultsDao savedResultsDao, GoalDao goalDao) {
        List<Week> lastMonthWeeks = DateUtil.getLastMonthWeeks();

        int fallCountForMonth = 0;
        for (Week week : lastMonthWeeks) {
            try {
                List<SavedResult> results = savedResultsDao.select(member.getUserId(), week.getMonday(), week.getSunday());
                int fallCount = 0;
                for (SavedResult result : results) {
                    Goal goal = goalDao.select(result.getGoalId());
                    if (goal.getPeriod().equals("month")) {
                        if (!(result.getResult() >= goal.getAim())) {
                            fallCount++;
                        }
                        if (fallCount >= (results.size() / 2)) {
                            fallCountForMonth++;
                            break;
                        }
                    }
                }
            } catch (SQLException ignored) {
            }
        }
        if (fallCountForMonth > 0) {
            member.setFallsInThisMonth(fallCountForMonth);
            fall.add(member);
        }
    }

    public boolean analize(Member member, List<UserResult> monthResults) {
        List<Boolean> weekStatus;
        try {
            weekStatus = savedResultsDao.getFallStatus(member, "week");
            int count = 0;
            for (Boolean s : weekStatus) {
                if (s) {
                    count++;
                }
            }

            int monthCount = 0;
            for (UserResult r : monthResults) {
                if (r.getCompleted() < goalDao.select(r.getGoalId()).getAim()) {
                    monthCount++;
                }
            }
            savedResultsDao.setFalledMember(DateUtil.getPastDay(), member, "month result", isFall(count, weekStatus.size(),monthCount));

            return isFall(count, weekStatus.size(),monthCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //сколько пользователей провалило цели в этом месяце
    public void send(Bot bot, MessageDao messageDao, GroupDao groupDao) {
        String result = "";
        try {
            result = messageDao.getMessageText(58);
        } catch (SQLException ignored) {
        }
        if (fall.size() == 0) {
            return;
        }
        Long groupChatId = 0L;
        for (Member member : fall) {
            result = result + "\n" + member.getFirstName() + " - " + member.getFallsInThisMonth();
            if (groupChatId == 0) {
                try {
                    groupChatId = groupDao.select(member.getGroupId()).getChatId();
                } catch (SQLException ignored) {
                }
            }
        }
        try {
            bot.sendMessage(new SendMessage()
                    .setChatId(groupChatId)
                    .setText(result)
            );

        } catch (TelegramApiException ignored) {
        }
    }

    private boolean isFall(int weekStatus,int countWeeks ,int monthStatus) throws SQLException{
        int border = goalDao.selectAllGoals().size() - goalDao.countOfGoalsByPeriod("day") - goalDao.countOfGoalsByPeriod("week") - 1;
        if(weekStatus > countWeeks / 2){
            if (monthStatus > border - 1){
                return true;
            }
        }else {
            if (monthStatus > border){
                return true;
            }
        }
        return false;
    }

}
