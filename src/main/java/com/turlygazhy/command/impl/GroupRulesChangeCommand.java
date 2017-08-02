package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by Vanderkast on 27.06.2017.
 */
public class GroupRulesChangeCommand extends Command {
    private WaitingType waitingType;
    private long chatId;
    private Message message;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        message = update.getMessage();
        chatId = message.getChatId();

        if (waitingType == null) {
            bot.sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(82)));
            waitingType = WaitingType.NEW_RULE;
            return false;
        } else {
            if (waitingType == WaitingType.NEW_RULE) {
                messageDao.setNewGroupRules(message.getText());

                bot.sendMessage(new SendMessage()
                        .setText(messageDao.getMessageText(83) + "\n" + messageDao.getMessageText(62))
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardMarkUpDao.select(13)));

                return true;
            }
        }
        return false;
    }
}
