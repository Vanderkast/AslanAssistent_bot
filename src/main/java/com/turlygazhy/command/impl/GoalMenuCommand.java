package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Goal;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanderkast on 25.06.2017.
 */
public class GoalMenuCommand extends Command {
    WaitingType waitingType;
    long chatId;
    String messageText;
    int goalId;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        } else {
            if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getFrom().getId();
                messageText = update.getCallbackQuery().getData();
            }
        }

        if (waitingType == null) {
            bot.sendMessage(new SendMessage()
                    .setText(messageDao.getMessageText(70))
                    .setChatId(chatId)
                    .setReplyMarkup(getGoalsInlineKeyboard()));
            bot.sendMessage(new SendMessage()
                    .setText(messageDao.getMessageText(71))
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardMarkUpDao.select(16)));

            waitingType = WaitingType.GWM_SHOW_GOAL;
        } else {
            switch (waitingType) {
                case GWM_SHOW_GOAL: {
                    try {
                        bot.sendMessage(new SendMessage()
                                .setText(getGoalInfoText())
                                .setParseMode("Markdown")
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardMarkUpDao.select(17)));

                        waitingType = WaitingType.GWM_GOAL_CHANGE;

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        bot.sendMessage(new SendMessage()
                                .setReplyMarkup(keyboardMarkUpDao.select(16))
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(72)));
                    }
                    return false;
                }
                case GWM_GOAL_CHANGE: {
                    if (messageText.equals(buttonDao.getButtonText(55))) {
                        goalDao.deleteGoal(goalId);

                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(73)));
                        bot.sendMessage(new SendMessage()
                                .setText(messageDao.getMessageText(70))
                                .setChatId(chatId)
                                .setReplyMarkup(getGoalsInlineKeyboard()));
                        bot.sendMessage(new SendMessage()
                                .setText(messageDao.getMessageText(71))
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardMarkUpDao.select(16)));
                        waitingType = null;
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private InlineKeyboardMarkup getGoalsInlineKeyboard() throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<Goal> goals = goalDao.selectAllGoals();

        while (!goals.isEmpty()) {
            Goal g = goals.get(0);
            goals.remove(0);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(new InlineKeyboardButton()
                    .setCallbackData(Integer.toString(g.getId()))
                    .setText(g.getName()));
            rows.add(row);
        }
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private String getGoalInfoText() throws SQLException, NumberFormatException {
        Goal goal = goalDao.select(Integer.parseInt(messageText));
        goalId = Integer.parseInt(messageText);
        String text;
        text = "ID цели: *" + goal.getId() + "* \n";
        text = text + "Название: *" + goal.getName() + "* \n";
        text = text + "Значение: *" + goal.getAim() + "* \n";
        text = text + "Можно ли заполнить: *" + goal.isFulfil() + "* \n";

        switch (goal.getKeyboard_type()) {
            case "lowadd": {
                text = text + "Тип клавиатуры: *'+1'* \n";
                break;
            }
            case "midadd": {
                text = text + "Тип клавиатуры: *'+1,+3,+5,+20'* \n";
                break;
            }
            case "done": {
                text = text + "Тип клавиатуры: *'Выполнить'* \n";
                break;
            }
            case "standart": {
                text = text + "Тип клавиатуры: *'Стандарт'* \n";
                break;
            }
        }
        text = text + "Период обновления: *" + goal.getPeriod() + "* \n";
        text = text + "Время доступа: *" + goal.isTimeLimit() + "* \n";

        if (goal.isTimeLimit()) {
            text = text + "С *" + goal.getStartTime() + "* до *" + goal.getEndTime() + "*";
        }

        return text;
    }
}
