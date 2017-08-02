package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Goal;
import com.turlygazhy.entity.UserResult;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by user on 2/17/17.
 */
public class ShareAchievmentsCommand extends Command {
    private long userId;
    private long chatId;
    private List<Goal> goals;
    private String text;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        userId = update.getMessage().getChatId();
        chatId =  memberDao.getChatId((int)userId);
        goals = goalDao.selectAllGoals();

        for(Goal g : goals){
            text = text + g.getName() +" : " + goalDao.getGoalResultCompleted((int) userId, g.getId()) + "\n";
        }
        text = text + buttonDao.getButtonText(3) + " : " + goalDao.getReadingResultForUser((int) userId);

        bot.sendMessage(MessageConstructor());
        return false;
    }

    private SendMessage MessageConstructor(){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        return sendMessage;
    }

}
