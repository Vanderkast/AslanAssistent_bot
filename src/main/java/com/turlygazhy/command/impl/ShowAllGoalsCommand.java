package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Goal;
import com.turlygazhy.entity.UserReadingResult;
import com.turlygazhy.entity.UserResult;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 2/24/17.
 */
public class ShowAllGoalsCommand extends Command {
    private WaitingType waitingType;
    private int userResultId = 0;
    private int goalId;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        Message updateMessage = update.getMessage();
        Long chatId = updateMessage.getChatId();
        User user = updateMessage.getFrom();
        Integer userId = user.getId();
        String updateMessageText = updateMessage.getText();
        if (waitingType == null) {
            List<UserResult> userResults = goalDao.getForUser(userId);
            String text = messageDao.getMessageText(2);
            UserReadingResult reading = goalDao.getReadingResultForUser(userId);

            ReplyKeyboardMarkup keyboardMarkup = getKeyboardForGoals();

            for (UserResult result : userResults) {
                Goal goal = goalDao.select(result.getGoalId());
                text = text + "\n" + goal.getName() + ": *" + result.getCompleted() + "*" + " / " + goal.getAim();
                addToKeyboard(keyboardMarkup, goal.getName());
            }
            text = text + "\n" + buttonDao.getButtonText(3) + ": *" + reading.getCompleted() + "*" + " / " + reading.getAim();
            addToKeyboardReading(keyboardMarkup);
            bot.sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setParseMode("Markdown")
                    .setReplyMarkup(keyboardMarkup)
                    .setText(text)
            );
            waitingType = WaitingType.GOAL_NAME;
            return false;
        }

        switch (waitingType) {
            case GOAL_NAME:
                showGoal(bot, chatId, userId, updateMessageText);
                return false;
            case CHANGE_GOAL: {
                //показ клавиатур
                onChangeGoal(bot, chatId);

                waitingType = waitingType.GOAL_SAVING;
                return false;
            }
            case GOAL_SAVING: {
                String text;
                //action saving
                //actionHistoryDao.addAction(userId, updateMessageText);

                int inc = onGoalSaving(updateMessageText, userId);

                if(inc != 0) {
                    int completed = goalDao.getGoalResultCompleted(userId, goalId);
                    text = inc + " добавлено \n сейчас: " + completed + " / " + goalDao.select(goalId).getAim();
                } else{
                    text = messageDao.getMessageText(103);
                }

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(text);
                bot.sendMessage(sendMessage);


                return false;
            }
            case GOAL_COMPLETED:
                try {
                    goalDao.inputResult(userResultId, Integer.parseInt(updateMessageText));
                    showGoal(bot, chatId, userId, goalDao.getGoalName(goalId));
                    return false;
                } catch (NumberFormatException e) {
                    sendMessage(30, chatId, bot);
                    return false;
                }
        }

        return false;
    }


    //определение кака клавиатура требуется для заполнения цели
    private boolean onChangeGoal(Bot bot, long chatId) throws SQLException, TelegramApiException {
        switch (goalDao.select(goalId).getKeyboard_type()) {
            case "lowadd": {
                bot.sendMessage(MessageConstructor(chatId, 103, 101));
                return false;
            }
            case "midadd": {
                bot.sendMessage(MessageConstructor(chatId, 101, 101));
                return false;
            }
            case "done": {
                bot.sendMessage(MessageConstructor(chatId, 102, 102));
                return false;
            }
            case "standart": {
                bot.sendMessage(new SendMessage()
                        .setText(messageDao.getMessageText(8))
                        .setChatId(chatId)
                );
                waitingType = WaitingType.GOAL_COMPLETED;
                return false;
            }
        }
        return false;
    }

    //конструктор сообщений
    private SendMessage MessageConstructor(long chatID, int keyboardID, int messageID) throws TelegramApiException, SQLException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboardMarkUpDao.select(keyboardID));
        sendMessage.setText(messageDao.getMessageText(messageID));
        sendMessage.setChatId(chatID);

        return sendMessage;
    }

    //сохранение результата или выбрасывание пользователя вверх
    private int onGoalSaving(String messageText, int userId) throws SQLException, TelegramApiException {
        int increment = 0;

        switch (messageText){
            case "Выполнено":{
                increment = 1;

                increment = InputGoal(increment, userId);
                actionHistoryDao.addAction(userId, messageText);
                break;
            }
            case "Отмена":{
                String lastAction = actionHistoryDao.getLastAction(userId);
                if(lastAction.equals("Выполнено")){
                    increment = -1;
                } else {
                    increment = Integer.parseInt(lastAction);
                    increment -= 2 * increment;
                }
                    increment = InputGoal(increment, goalId);
                    actionHistoryDao.deleteLastAction(userId);
                    break;
            }
            default:{
                try{
                    increment = Integer.parseInt(messageText);
                    increment = InputGoal(increment, userId);

                    actionHistoryDao.addAction(userId,messageText);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }
        return increment;
    }

    /*
    private boolean CheckGoalFull(int userID) throws SQLException{
        if(goalDao.select(goalId).getAim() == goalDao.getGoalResultCompleted(goalId, userID)){
            return true;
        }
        return false;
    }*/

    //если цель можно достичь, то проверяем на переполнение
    private int InputGoal(int increment, int userId) throws SQLException {
        if(increment >= 0) {
            if (goalDao.select(goalId).isFulfil()) {
                //если введенное число больше чем максимальное то запсываем максимум из возможного
                int completed = increment + goalDao.getGoalResultCompleted(userId, goalId);
                if (completed <= goalDao.select(goalId).getAim()) {
                    goalDao.inputResult(userResultId, increment);

                } else {
                    increment = goalDao.select(goalId).getAim() - goalDao.getGoalResultCompleted(userId, goalId);

                    goalDao.inputResult(userResultId, increment);
                }
            } else {
                goalDao.inputResult(userResultId, increment);
            }
        } else {
            goalDao.inputResult(userResultId, increment);
        }
        return increment;
    }

    private void addToKeyboardReading(ReplyKeyboardMarkup keyboardMarkup) throws SQLException {
        List<KeyboardRow> keyboard = keyboardMarkup.getKeyboard();
        KeyboardRow keyboardButtons = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton(buttonDao.getButtonText(3));
        keyboardButtons.add(keyboardButton);
        keyboard.add(1, keyboardButtons);
    }

    /**
     * @return false if not in time limit
     */
    private boolean showGoal(Bot bot, Long chatId, Integer userId, String updateMessageText) throws SQLException, TelegramApiException {
        List<UserResult> userResults = goalDao.getForUser(userId);
        for (UserResult userResult : userResults) {
            int goalId = userResult.getGoalId();
            Goal goal = goalDao.select(goalId);
            //при совпадении имени и сообщения
            if (goal.getName().equals(updateMessageText)) {
                //проверка есть ли тайминг у цели
                //если да то можно ли ее заполнить в момент попытки
                if (goal.isTimeLimit()) {
                    String startTime = goal.getStartTime();
                    String endTime = goal.getEndTime();
                    if (!checkTime(startTime, endTime)) {
                        bot.sendMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText(messageDao.getMessageText(32) + startTime + "-" + endTime)
                        );
                        return false;
                    }
                }

                //если цель достигнута выбрасываем пользователя вверх
                if (goal.getAim() == goalDao.getGoalResultCompleted(userId, goalId)) {
                    bot.sendMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText(messageDao.getMessageText(103)));
                    return false;
                }

                //выводим клавиатуру и состояние на данный момент
                bot.sendMessage(new SendMessage()
                        .setText(goal.getName() + ": " + userResult.getCompleted() + "/" + goal.getAim())
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardMarkUpDao.select(3))
                );
                userResultId = userResult.getId();
                waitingType = WaitingType.CHANGE_GOAL;
                this.goalId = goalId;
                return true;
            }
        }
        return true;
    }

    private boolean checkTime(String startTime, String endTime) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm"); //HH = 24h format
            dateFormat.setLenient(false); //this will not enable 25:67 for example

            Date now = new Date();
            int nowHours = now.getHours();
            int nowMinutes = now.getMinutes();

            Date start = dateFormat.parse(startTime);
            int startHours = start.getHours();
            int startMinutes = start.getMinutes();

            Date end = dateFormat.parse(endTime);
            int endHours = end.getHours();
            int endMinutes = end.getMinutes();

            if (startHours < nowHours) {
                if (endHours > nowHours) {
                    return true;
                } else {
                    if (endHours == nowHours) {
                        if (endMinutes >= nowMinutes) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                if (startHours == nowHours) {
                    if (startMinutes <= nowMinutes) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addToKeyboard(ReplyKeyboardMarkup keyboardMarkup, String name) {
        List<KeyboardRow> keyboard = keyboardMarkup.getKeyboard();
        KeyboardRow keyboardButtons = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton(name);
        keyboardButtons.add(keyboardButton);
        keyboard.add(keyboard.size() - 1, keyboardButtons);
    }

    private ReplyKeyboardMarkup getKeyboardForGoals() throws SQLException {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> rowsList = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText(buttonDao.getButtonText(10));
        keyboardRow.add(button);
        rowsList.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(rowsList);
        return replyKeyboardMarkup;
    }
}
