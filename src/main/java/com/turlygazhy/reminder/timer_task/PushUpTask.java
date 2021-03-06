package com.turlygazhy.reminder.timer_task;

import com.turlygazhy.Bot;
import com.turlygazhy.entity.Member;
import com.turlygazhy.reminder.Reminder;
import com.turlygazhy.tool.DateUtil;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 06-Mar-17.
 * Бесполезный класс, который напоминает, что отжимания нужно заполнить до 12am
 */
public class PushUpTask extends AbstractTask {

    public PushUpTask(Bot bot, Reminder reminder) {
        super(bot, reminder);
    }

    @Override
    public void run() {
        try {
            reminder.setNextPushUpTask();//обновление потока на следующий день
            List<Member> members = memberDao.selectAll();//получение всех участников
            for (Member member : members) {
                boolean access = member.isAccess();//запрос о наличии допуска у участника
                try {
                    if (access) {
                        bot.sendMessage(new SendMessage()
                                .setChatId(member.getChatId())
                                .setText(messageDao.getMessageText(48))//напоминание. что отжимания принимаются только до 12am
                        );
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
