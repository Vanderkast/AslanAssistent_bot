package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

public class ChangeUrlCommand extends Command {
    private WaitingType waitingType;
    private long chatId;
    private String messageText;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        } else {
            chatId = update.getCallbackQuery().getFrom().getId();
            messageText = update.getCallbackQuery().getData();
        }

        if (waitingType == null) {
            bot.sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(89)));
            waitingType = WaitingType.SAVE_URL;
            return false;
        }
        switch (waitingType) {
            case SAVE_URL: {
                buttonDao.updateButtonUrl(15, messageText);
                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(messageDao.getMessageText(90)));
                return true;
            }
        }
        return false;
    }
}
