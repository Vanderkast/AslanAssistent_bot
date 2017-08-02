package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by user on 2/24/17.
 */
public class AddGoalCommand extends Command {
    private WaitingType waitingType;
    private String goalName;
    private int goal;
    private String period;
    private String keyboard_type;
    private boolean isFulfil;
    private String startTime;
    private String endTime;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        Message updateMessage = update.getMessage();
        Long chatId;
        if (updateMessage != null) {
            chatId = updateMessage.getChatId();
        } else {
            chatId = update.getCallbackQuery().getFrom().getId().longValue();
        }
        if (waitingType == null) {
            sendMessage(26, chatId, bot);
            waitingType = WaitingType.GOAL_NAME;
            return false;
        }
        String text;
        if (updateMessage != null) {
            text = updateMessage.getText();
        } else {
            text = update.getCallbackQuery().getData();
        }
        switch (waitingType) {
            case GOAL_NAME:
                goalName = text;
                sendMessage(27, chatId, bot);
                waitingType = WaitingType.GOAL_DIGIT;
                return false;
            case GOAL_DIGIT:
                try {
                    goal = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    sendMessage(30, chatId, bot);
                    return false;
                }
                sendMessage(29, chatId, bot);
                waitingType = WaitingType.AVAILABLE_TIME_START;
                return false;
            case AVAILABLE_TIME_START:
                CallbackQuery callbackQuery = update.getCallbackQuery();
                if (callbackQuery != null) {
                    if (callbackQuery.getData().equals(buttonDao.getButtonText(25))) {
                        startTime = null;
                        endTime = null;

                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(66))
                                .setReplyMarkup(keyboardMarkUpDao.getKeyboardYesNo(buttonDao)));
                        waitingType = WaitingType.KEYBOARD_TYPE;
                        return false;
                    }
                }
                boolean validateTime = validateTime(text);
                if (validateTime) {
                    startTime = text;
                    waitingType = WaitingType.AVAILABLE_TIME_END;
                    sendMessage(31, chatId, bot);
                    return false;
                } else {
                    sendMessage(30, chatId, bot);
                    return false;
                }
            case AVAILABLE_TIME_END:
                boolean validateEndTime = validateTime(text);
                if (validateEndTime) {
                    endTime = text;
                } else {
                    sendMessage(30, chatId, bot);
                    return false;
                }

                //do not return
            case IS_FULFIL: {
                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(messageDao.getMessageText(66))
                        .setReplyMarkup(keyboardMarkUpDao.getKeyboardYesNo(buttonDao)));
                waitingType = WaitingType.KEYBOARD_TYPE;
                return false;
            }
            case KEYBOARD_TYPE: {
                if (update.getCallbackQuery().getData().equals("Да")) {
                    bot.sendMessage(new SendMessage()
                            .setReplyMarkup(keyboardMarkUpDao.select(14))
                            .setText(messageDao.getMessageText(67))
                            .setChatId(chatId));
                    isFulfil = true;
                    waitingType = WaitingType.PERIOD;
                }
                if (update.getCallbackQuery().getData().equals("Нет")) {
                    bot.sendMessage(new SendMessage()
                            .setReplyMarkup(keyboardMarkUpDao.select(14))
                            .setText(messageDao.getMessageText(67))
                            .setChatId(chatId));
                    isFulfil = false;
                    waitingType = WaitingType.PERIOD;
                } else {
                    bot.sendMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText(messageDao.getMessageText(68))
                            .setReplyMarkup(keyboardMarkUpDao.getKeyboardYesNo(buttonDao)));
                    return false;
                }

                return false;
            }
            case PERIOD: {
                switch (updateMessage.getText()) {
                    case "+1": {
                        keyboard_type = "lowadd";
                        waitingType = WaitingType.NEW_GOAL_SAVING;

                        bot.sendMessage(new SendMessage()
                                .setReplyMarkup(keyboardMarkUpDao.select(15))
                                .setText(messageDao.getMessageText(69))
                                .setChatId(chatId));
                        return false;
                    }
                    case "+1,+3,+5,+20": {
                        keyboard_type = "midadd";
                        waitingType = WaitingType.NEW_GOAL_SAVING;

                        bot.sendMessage(new SendMessage()
                                .setReplyMarkup(keyboardMarkUpDao.select(15))
                                .setText(messageDao.getMessageText(69))
                                .setChatId(chatId));
                        return false;
                    }
                    case "Выполнено": {
                        keyboard_type = "done";
                        waitingType = WaitingType.NEW_GOAL_SAVING;

                        bot.sendMessage(new SendMessage()
                                .setReplyMarkup(keyboardMarkUpDao.select(15))
                                .setText(messageDao.getMessageText(69))
                                .setChatId(chatId));
                        return false;
                    }
                    case "стандарт": {
                        keyboard_type = "standart";
                        waitingType = WaitingType.NEW_GOAL_SAVING;

                        bot.sendMessage(new SendMessage()
                                .setReplyMarkup(keyboardMarkUpDao.select(15))
                                .setText(messageDao.getMessageText(69))
                                .setChatId(chatId));
                        return false;
                    }
                    default: {
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardMarkUpDao.select(14))
                                .setText(messageDao.getMessageText(68) + " " + messageDao.getMessageText(67)));
                        return false;
                    }
                }
            }
            case NEW_GOAL_SAVING: {
                switch (updateMessage.getText()) {
                    case "день": {
                        period = "day";
                        break;
                    }
                    case "неделя": {
                        period = "week";
                        break;
                    }
                    case "месяц": {
                        period = "month";
                        break;
                    }
                    default: {
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardMarkUpDao.select(15))
                                .setText(messageDao.getMessageText(68) + " " + messageDao.getMessageText(67)));
                        return false;
                    }
                }

                goalDao.insert(goalName, goal, startTime, endTime, period, isFulfil, keyboard_type);

                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardMarkUpDao.select(13))
                        .setText(messageDao.getMessageText(28)));
                return true;
            }
        }
        return false;
    }
}
