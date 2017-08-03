package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Const;
import com.turlygazhy.entity.Group;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChangeGroupCommand extends Command {
    private long chatId;
    private String messageText;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
            if(messageText != "Работа с группами"){
                return true;
            }
        } else {
            chatId = update.getCallbackQuery().getFrom().getId();
            messageText = update.getCallbackQuery().getData();
        }



        if (update.hasMessage()) {
            List<Group> groups = groupDao.selectAll();
            if (groups.size() == 0) {
                return true;
            }

            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();
            List<InlineKeyboardButton> row = new ArrayList<>();

            button.setText("Удалить");
            bot.sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(93))
                    .setReplyMarkup(keyboard));
            for (Group group : groups) {
                row.clear();
                rows.clear();
                button.setCallbackData(String.valueOf(group.getId()));
                row.add(button);
                rows.add(row);
                keyboard.setKeyboard(rows);

                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(group.getTitle() + " " + group.getId())
                        .setReplyMarkup(keyboard));
            }
            return false;
        } else {
            bot.sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(94)));
            groupDao.deleteGroup(Integer.parseInt(messageText));
            return true;
        }
    }

    private InlineKeyboardMarkup getKeyboard() throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<Group> groups = groupDao.selectAll();

        if (groups.size() == 0) {
            return null;
        }

        for (Group group : groups) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            List<InlineKeyboardButton> row = new ArrayList<>();

            button.setText("Удалить");
            button.setCallbackData(String.valueOf(group.getId()));

            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
