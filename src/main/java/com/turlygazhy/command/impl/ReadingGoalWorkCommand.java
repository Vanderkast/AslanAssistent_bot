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
 * Created by Vanderkast on 28.06.2017.
 */
public class ReadingGoalWorkCommand extends Command {
    private WaitingType waitingType;
    private Message message;
    private long chatId;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        message = update.getMessage();
        chatId = message.getChatId();

        if (waitingType == null) {
            bot.sendMessage(new SendMessage()
                    .setReplyMarkup(keyboardMarkUpDao.select(19))
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(87)));
            waitingType = WaitingType.READING_GOAL_WORK;
            return false;
        } else {
            switch (waitingType) {
                case READING_GOAL_WORK: {
                    if (message.getText().equals(buttonDao.getButtonText(60))) {
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(85)));
                        waitingType = WaitingType.NEW_READING_NAME;
                        return false;
                    } else {
                        if (message.getText().equals(buttonDao.getButtonText(61))) {
                            bot.sendMessage(new SendMessage()
                                    .setChatId(chatId)
                                    .setText(messageDao.getMessageText(88)));
                            waitingType = WaitingType.NEW_READING_AIM;
                            return false;
                        } else {
                            bot.sendMessage(new SendMessage()
                                    .setText(messageDao.getMessageText(7))
                                    .setChatId(chatId));
                            return false;
                        }
                    }
                }
                case NEW_READING_NAME: {
                    if(!message.getText().equals(buttonDao.getButtonText(61))) {
                        buttonDao.updateButtonText(3, message.getText());
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(buttonDao.getButtonText(60) + " " + messageDao.getMessageText(86)));
                        return true;
                    }else{
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(72)));
                        waitingType = WaitingType.NEW_READING_NAME;
                        return false;
                    }
                }
                case NEW_READING_AIM: {
                    try {
                        goalDao.updateReadingAim(Integer.parseInt(message.getText()));
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(buttonDao.getButtonText(61) + " " + messageDao.getMessageText(86)));
                        return true;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(72)));
                        waitingType = WaitingType.NEW_READING_AIM;
                        return false;
                    }

                }
            }
        }
        return false;
    }
}
