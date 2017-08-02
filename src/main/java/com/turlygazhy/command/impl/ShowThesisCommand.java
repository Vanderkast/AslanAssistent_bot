package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Thesis;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by user on 3/1/17.
 */
public class ShowThesisCommand extends Command {
    private WaitingType waitingType;
    private int thesisId;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {


        if (!update.hasMessage()) {
            Integer chatId = update.getCallbackQuery().getFrom().getId();
            thesisId = Integer.parseInt(update.getCallbackQuery().getData());
            bot.sendMessage(new SendMessage()
                    .setChatId((long) chatId)
                    .setText(thesisDao.getThesisText(thesisDao.select(thesisId)))
                    .setReplyMarkup(keyboardMarkUpDao.select(18)));
            waitingType = WaitingType.DELETE_THESIS;
        } else {
            Message updateMessage = update.getMessage();
            Integer userId = updateMessage.getFrom().getId();
            Long chatId = updateMessage.getChatId();
            String updateMessageText = updateMessage.getText();
            if (waitingType == null) {
                showThesis(bot, userId, chatId);
                waitingType = WaitingType.CHANGE_THESIS;
                return false;
            }

            switch (waitingType) {
                case CHANGE_THESIS: {
                    if (updateMessageText.equals(buttonDao.getButtonText(27))) {
                        sendMessage(38, chatId, bot);
                        waitingType = WaitingType.NEW_THESIS;
                        return false;
                    }
                }
                case DELETE_THESIS: {
                    if (updateMessageText.equals(buttonDao.getButtonText(56))) {
                        thesisDao.deleteThesis(thesisId);
                        bot.sendMessage(new SendMessage()
                                .setText(messageDao.getMessageText(74))
                                .setReplyMarkup(keyboardMarkUpDao.select(10))
                                .setChatId(chatId));
                        return false;
                    }
                }
                case NEW_THESIS: {
                    thesisDao.SaveThesis(updateMessageText, userId);
                    showThesis(bot, userId, chatId);
                    waitingType = WaitingType.CHANGE_THESIS;
                    return false;
                }
            }
        }


        return false;
    }

    private void showThesis(Bot bot, Integer userId, Long chatId) throws SQLException, TelegramApiException {
        bot.sendMessage(new SendMessage()
                .setText("Выберите тезис \n")
                .setChatId(chatId)
                .setReplyMarkup(thesisDao.getThesisInlineKeyboard(userId))
        );
        bot.sendMessage(new SendMessage()
                .setText("или выбиерите команду")
                .setChatId(chatId)
                .setReplyMarkup(keyboardMarkUpDao.select(10))
        );
    }

}
