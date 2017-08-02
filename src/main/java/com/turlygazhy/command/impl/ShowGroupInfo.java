package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

public class ShowGroupInfo extends Command {
    private String messageText;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        messageText = update.getMessage().getText();

        if (messageText.equals("who is GOD")) {
            bot.sendMessage(new SendMessage()
                    .setText("Мазуров \"Vanderkast\" Валентин")
                    .setChatId(update.getMessage().getChatId()));
            return true;
        }

        String url = groupDao.getGroupUrl(memberDao.getGroupIdByUserId(update.getMessage().getChatId()));

        bot.sendMessage(new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(messageDao.getMessageText(9))
                .setParseMode("Markdown"));
        bot.sendMessage(
                new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText(url)
                        .setParseMode("Markdown"));

        return true;
    }
}
