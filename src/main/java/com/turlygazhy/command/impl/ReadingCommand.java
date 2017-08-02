package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.UserReadingResult;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by user on 2/18/17.
 */
public class ReadingCommand extends Command {
    private WaitingType waitingType;
    private String bookName;
    private int incremented;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        Message updateMessage = update.getMessage();
        String updateMessageText;
        Integer userId;
        if (updateMessage == null) {
            CallbackQuery callbackQuery;
            callbackQuery = update.getCallbackQuery();
            updateMessage = callbackQuery.getMessage();
            updateMessageText = callbackQuery.getData();
            userId = callbackQuery.getFrom().getId();
        } else {
            updateMessageText = updateMessage.getText();
            userId = updateMessage.getFrom().getId();
        }
        Long chatId = updateMessage.getChatId();

        if (waitingType == null) {
            if(updateMessageText.equals(buttonDao.getButtonText(29))){
                sendMessage(33, chatId, bot);
                waitingType = WaitingType.BOOK_NAME;
                return false;
            } else {
                UserReadingResult readingResult = goalDao.getReadingResultForUser(userId);
                String bookName = readingResult.getBookName();
                if (bookName == null || bookName.equals("")) {
                    sendMessage(33, chatId, bot);
                    waitingType = WaitingType.BOOK_NAME;
                    return false;
                }

                showRead(bot, chatId, readingResult, bookName);
            }
            waitingType = WaitingType.CHANGE_GOAL;
            return false;
        }

        switch (waitingType) {
            case BOOK_NAME:
                //проверка на правильность введенного названия книги
                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(messageDao.getMessageText(34) + "\n'" + updateMessageText + "'")
                        .setReplyMarkup(keyboardMarkUpDao.select(11))
                );
                bookName = updateMessageText;

                waitingType = WaitingType.CHECK_BOOK_NAME;

                return false;

            case CHECK_BOOK_NAME:
                //проверка выбора правильности введенного названия
                if (updateMessageText.equals(buttonDao.getButtonText(32))) {
                    goalDao.setBookName(userId, bookName);
                    goalDao.saveBook(userId, bookName);


                    UserReadingResult readingResult = goalDao.getReadingResultForUser(userId);
                    showRead(bot, chatId, readingResult, bookName);

                    waitingType = WaitingType.CHANGE_GOAL;
                    return false;
                }
                if (updateMessageText.equals(buttonDao.getButtonText(33))) {
                    sendMessage(33, chatId, bot);
                    waitingType = WaitingType.BOOK_NAME;
                    return false;
                }

            case CHANGE_GOAL: {
                //ввод выполнения
                if (updateMessageText.equals(buttonDao.getButtonText(14))) {
                    bot.sendMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText(messageDao.getMessageText(101))
                            .setReplyMarkup(keyboardMarkUpDao.select(110))
                    );
                    waitingType = WaitingType.GOAL_ADDED;
                    return false;
                }
                if (updateMessageText.equals(buttonDao.getButtonText(29))) {
                    UserReadingResult readingResult = goalDao.getReadingResultForUser(userId);
                    thesisDao.SaveThesis(readingResult.getBookName(), userId);
                    goalDao.resetReading(userId);
                    sendMessage(33, chatId, bot);
                    waitingType = WaitingType.BOOK_NAME;
                    return false;
                }

                waitingType = waitingType.GOAL_ADDED;

                sendMessage(7, chatId, bot);
                return true;
            }
            case GOAL_ADDED:{
                switch(updateMessageText){
                    case "Чтение +1":{
                        incremented = 1;
                        //incremented = CheckIncrement(incremented, userId);
                        goalDao.inputReadingResult(userId, incremented);
                        actionHistoryDao.addAction(userId, "1");
                        waitingType = waitingType.GOAL_ADDED;
                        break;
                    }
                    case "Чтение +5":{
                        incremented = 5;
                        //incremented = CheckIncrement(incremented, userId);
                        goalDao.inputReadingResult(userId, incremented);
                        actionHistoryDao.addAction(userId, "5");
                        waitingType = waitingType.GOAL_ADDED;
                        break;
                    }
                    case "Чтение +20":{
                        incremented = 20;
                        //incremented = CheckIncrement(incremented, userId);
                        goalDao.inputReadingResult(userId, incremented);
                        actionHistoryDao.addAction(userId, "20");
                        waitingType = waitingType.GOAL_ADDED;
                        break;
                    }
                    case "Назад":{
                        waitingType = waitingType.GOAL_COMPLETED;
                        actionHistoryDao.addAction(userId, "0");
                        incremented = 0;
                        break;
                    }
                    case "отмена":{
                        try {
                            int canceled = 0 - Integer.parseInt(actionHistoryDao.getLastAction(userId));
                            goalDao.inputReadingResult(userId, canceled);
                            incremented = canceled;
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                        }
                        break;
                    }
                    default:{
                        incremented = 0;
                        goalDao.inputReadingResult(userId, incremented);
                        break;
                    }
                }

                int comp = goalDao.getReadingResultForUser(userId).getCompleted();
                bot.sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(incremented +" добавлено \n сейчас " + comp + "/100" )
                        .setReplyMarkup(keyboardMarkUpDao.select(110))
                );
                return false;
            }
            case GOAL_COMPLETED:
                //завершение выполнения
                try {
                    UserReadingResult readingResult = goalDao.inputReadingResult(userId, Integer.parseInt(updateMessageText));
                    showRead(bot, chatId, readingResult, readingResult.getBookName());
                    waitingType = WaitingType.CHANGE_GOAL;
                    return false;
                } catch (NumberFormatException e) {
                    sendMessage(30, chatId, bot);
                    return false;
                }
        }
        return false;
    }

    private int CheckIncrement(int incremented, int userId) throws SQLException{
        int done = goalDao.getReadingResultForUser(userId).getCompleted();
        if(done +  incremented > goalDao.getReadingResultForUser(userId).getAim()){
            incremented = goalDao.getReadingResultForUser(userId).getAim() - done;
        }
        return incremented;
    }

    private void showRead(Bot bot, Long chatId, UserReadingResult readingResult, String bookName) throws SQLException, TelegramApiException {
        int comp = incremented + readingResult.getCompleted();
        String text = bookName + "\n" + buttonDao.getButtonText(3) + ": " + comp + "/" + readingResult.getAim();
        bot.sendMessage(new SendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboardMarkUpDao.select(9))
        );
    }
}
