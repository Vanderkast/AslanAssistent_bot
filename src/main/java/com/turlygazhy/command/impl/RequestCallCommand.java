package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import org.telegram.telegrambots.api.objects.Contact;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by user on 1/5/17.
 */
public class RequestCallCommand extends Command {
    private boolean expectContact = false;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        if (!expectContact) {
            expectContact = true;
            sendMessage(39, chatId, bot);
            return false;
        }

        Contact contact = message.getContact();
        if (contact != null) {
            sendMessageToAdmin(35, bot, contact);
            sendMessage(40, chatId, bot);
            expectContact = false;
            return true;
        } else {
            String text = message.getText();
            sendMessageToAdmin(35, bot);
            sendMessageToAdmin(text, bot);
        }

        sendMessage(40, chatId, bot);

        expectContact = false;
        return true;
    }
}
