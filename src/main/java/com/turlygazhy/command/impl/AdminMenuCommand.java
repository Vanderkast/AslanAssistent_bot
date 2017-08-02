package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Goal;
import com.turlygazhy.entity.Member;
import com.turlygazhy.entity.UserResult;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanderkast on 23.06.2017.
 */
public class AdminMenuCommand extends Command {
    private long userId;
    private String messageText;
    private WaitingType waitingType;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (update.hasMessage()) {
            userId = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        } else {
            userId = update.getCallbackQuery().getFrom().getId();
            messageText = update.getCallbackQuery().getData();
        }

        if(messageText.contains("del")){
            String[] lips = messageText.split(" ");
            memberDao.deleteMember(Long.parseLong(lips[1]));
            bot.sendMessage(new SendMessage()
                    .setChatId(userId)
                    .setText(messageDao.getMessageText(91))
                    .setReplyMarkup(keyboardMarkUpDao.select(13)));
            waitingType = WaitingType.ADMIN_MENU;
        }

        if (waitingType == null) {
            if (userDao.isAdmin((int) userId)) {
                waitingType = WaitingType.ADMIN_MENU;
                //do not return
            } else {
                bot.sendMessage(new SendMessage()
                        .setText(messageDao.getMessageText(61))
                        .setChatId(userId));
                waitingType = WaitingType.ADMIN_ADDING;
                return false;
            }
        } else {
            if (waitingType == waitingType.ADMIN_ADDING) {
                if (messageText.equals(buttonDao.getButtonText(41))) {
                    userDao.setAdmin(userId);
                    bot.sendMessage(new SendMessage()
                            .setText(messageDao.getMessageText(62))
                            .setChatId(userId));
                    waitingType = WaitingType.ADMIN_MENU;
                    //do not return
                }
            }
        }

        switch (waitingType) {
            case ADMIN_MENU: {
                bot.sendMessage(new SendMessage()
                        .setChatId(userId)
                        .setText(messageDao.getMessageText(63))
                        .setReplyMarkup(keyboardMarkUpDao.select(13)));
                waitingType = WaitingType.ADMIN_COMMAND;
                return false;
            }
            case ADMIN_COMMAND: {
                if (messageText.equals(buttonDao.getButtonText(43))) {
                    bot.sendMessage(new SendMessage()
                            .setText(messageDao.getMessageText(64))
                            .setChatId(userId)
                            .setReplyMarkup(keyboardMarkUpDao.getKeyboardOfMembers(memberDao)));

                    waitingType = WaitingType.ADMIN_USER_LIST_SHOW;
                    return false;
                }
                if (messageText.equals(buttonDao.getButtonText(45))) {
                    userDao.deleteAdmin(userId);
                    bot.sendMessage(new SendMessage()
                            .setText(messageDao.getMessageText(65))
                            .setChatId(userId));
                    waitingType = null;
                    return false;
                }
                if (messageText.equals(buttonDao.getButtonText(57))) {
                    bot.sendMessage(new SendMessage()
                            .setChatId(userId)
                            .setText(messageDao.getMessageText(6)));
                    bot.sendMessage(new SendMessage()
                            .setChatId(userId)
                            .setText(messageDao.getMessageText(77)));
                    waitingType = WaitingType.NEW_ABOUT_PROJECT;
                }
                return false;
            }
            case ADMIN_USER_LIST_SHOW: {
                ArrayList<String> text = new ArrayList<>();
                ArrayList<String> callBack = new ArrayList<>();
                text.add("Удалить пользователя");
                callBack.add("del " + Integer.parseInt(messageText));

                bot.sendMessage(new SendMessage()
                        .setChatId(userId)
                        .setText(getMemberInfo(Integer.parseInt(messageText)))
                        .setReplyMarkup(keyboardMarkUpDao.getKeyboardWithCallBack(text, callBack)));

                waitingType = WaitingType.ADMIN_MENU;
                return false;//здесь действия с пользователем
            }
            case NEW_ABOUT_PROJECT: {
                messageDao.setNewAboutProjectText(messageText);
                bot.sendMessage(new SendMessage()
                        .setChatId(userId)
                        .setText(messageDao.getMessageText(78))
                        .setReplyMarkup(keyboardMarkUpDao.select(13)));
                waitingType = WaitingType.ADMIN_COMMAND;
            }
        }
        return false;
    }

    /*private boolean isAdmin() throws SQLException{
        if (userDao.isAdmin((int) userId)) {
            return true;
        } else {
            return false;
        }
    }*/

    private String getMemberInfo(int userId) throws SQLException {
        ArrayList<String> info = new ArrayList<>();
        Member member = memberDao.selectByUserId(userId);

        info.add("ID: " + member.getUserId());
        info.add("Name: " + member.getFirstName() + " " + member.getLastName());
        info.add("Number: " + member.getPhoneNumber());
        info.add("Is fallen: " + member.getFallsInThisMonth());

        List<Goal> goals = goalDao.selectAllGoals();
        for (Goal g : goals) {
            info.add(g.getName() + ": " + goalDao.getGoalResultCompleted(userId, g.getId())
                    + "/" + goalDao.select(g.getId()).getAim());
        }

        info.add("Чтение: " + goalDao.getReadingResultForUser(userId).getCompleted()
                + "/" + goalDao.getReadingResultForUser(userId).getAim());

        String text = "";
        for (String s : info) {
            text = text + s + "\n";
        }
        return text;
    }
}
