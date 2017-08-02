package com.turlygazhy.entity;

import com.turlygazhy.dao.GoalDao;
import com.turlygazhy.dao.impl.SavedResultsDao;
import com.turlygazhy.tool.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanderkast on 25.06.2017.
 */
public class DayResult {
    public boolean analyze(SavedResultsDao savedResultsDao, GoalDao goalDao,
                           List<UserResult> results, String yesterday, Member member) {
        try {
            int count = 0;
            for (UserResult r : results) {
                if (r.getCompleted() < goalDao.select(r.getGoalId()).getAim()) {
                    count += 1;
                }
            }
            int border = results.size();
            if (2 * count >  border) {
                savedResultsDao.setFalledMember(yesterday, member, "day", true);
                return true;
            } else {
                savedResultsDao.setFalledMember(yesterday, member, "day", false);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}
