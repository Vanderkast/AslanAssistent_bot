package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Const;
import com.turlygazhy.entity.Group;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChangeUrlCommand extends Command {
    private WaitingType waitingType;
    private long chatId;
    private String messageText;

    private String url;

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
                url = messageText;
                //buttonDao.updateButtonUrl(15, messageText);
                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(messageDao.getMessageText(90))
                        .setReplyMarkup(getKeyboard()));
                waitingType = WaitingType.ADD_URL;
                return false;
            }
            case ADD_URL:{
                String[] lep = messageText.split(" ");
                groupDao.setUrlToGroup(Integer.parseInt(lep[1]), url);
                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(messageDao.getMessageText(92)));
                return true;
            }
        }
        return false;
    }

    private InlineKeyboardMarkup getKeyboard() throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        List<Group> groups = groupDao.selectAll();

        if (groups.size() == 0) {
            return null;
        }

        for (Group group : groups) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getTitle());
            button.setCallbackData("add " + group.getId());
            row.add(button);
        }

        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
