package com.turlygazhy.reminder.timer_task;

import com.turlygazhy.Bot;
import com.turlygazhy.entity.Member;
import com.turlygazhy.reminder.Reminder;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 06-Mar-17.
 * напоминает пользователям с доступом о проверке заполнения целей в конце дня
 */
public class EndDayTask extends AbstractTask {
    public EndDayTask(Bot bot, Reminder reminder) {
        super(bot, reminder);
    }

    @Override
    public void run() {
        try {
            List<Member> members = memberDao.selectAll();
            for (Member member : members) {
                try {
                    bot.sendMessage(new SendMessage()
                            .setChatId(member.getChatId())
                            .setText(messageDao.getMessageText(49))
                    );
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            reminder.setEndDayTask(new Date().getHours());
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }
}
