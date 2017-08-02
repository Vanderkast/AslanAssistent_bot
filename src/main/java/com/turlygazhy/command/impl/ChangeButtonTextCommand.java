package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

public class ChangeButtonTextCommand extends Command {
    private int step;

    private int buttonId;
    private String buttonText;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        switch (step) {
            case 0: {
                bot.sendMessage(new SendMessage()
                        .setText("write button id")
                        .setChatId(update.getMessage().getChatId()));
                break;
            }
            case 1:{
                buttonId = Integer.parseInt(update.getMessage().getText());
                bot.sendMessage(new SendMessage()
                        .setText("write new button text")
                        .setChatId(update.getMessage().getChatId()));
                break;
            }
            case 2:{
                buttonText = update.getMessage().getText();

                buttonDao.updateButtonText(buttonId, buttonText);

                bot.sendMessage(new SendMessage()
                        .setText("text " + buttonText + " // saved into button: " + buttonId)
                        .setChatId(update.getMessage().getChatId()));
                return true;
            }
        }
        step++;
        return false;
    }

}
