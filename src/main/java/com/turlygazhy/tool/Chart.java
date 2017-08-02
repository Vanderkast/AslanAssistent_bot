package com.turlygazhy.tool;


import com.turlygazhy.entity.SavedResult;
import com.turlygazhy.exception.NoResultForChartException;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by user on 3/2/17.
 */
public class Chart {
    //private static final String PATH = "C:\\charts";
    private static final String PATH = "E:\\telegrambots";//todo:set source path

    //    private String PATH = "/home/user/Documents/charts";
    private String fileName;
    private Map<Date, Integer> data = new LinkedHashMap<>();

    public String getChart(String goalName) {
        List<Double> yData = new ArrayList<>();
        List<Date> xData = new ArrayList<>();

        for (Map.Entry<Date, Integer> entry : data.entrySet()) {
            xData.add(entry.getKey());
            yData.add(Double.valueOf(entry.getValue()));
        }

        // Create Chart
        XYChart chart = new XYChart(500, 400);
        chart.setTitle(goalName);
        XYSeries series = chart.addSeries("y(x)", xData, yData);//добавляем данные в x и y оси, указана
                                                                                //зависимость y от x
        series.setMarker(SeriesMarkers.CIRCLE);//устанавливается маркер (??)

        String fullPath = PATH + "/" + this.fileName;
        try {
            BitmapEncoder.saveBitmap(chart, fullPath, BitmapEncoder.BitmapFormat.JPG);//создание jpg-картинки диаграммы
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;//возвращается полный путь к файлу(картинке) диаграммы
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addPair(Date date, int result) {
        data.put(date, result);
    }

    public String getChart(String goalName, Map<String, List<SavedResult>> userNameAndResults) {
        String fileName = "chart";

        XYChart chart = new XYChart(500, 400);
        chart.setTitle(goalName);

        for (Map.Entry<String, List<SavedResult>> entry : userNameAndResults.entrySet()) {
            List<SavedResult> results = entry.getValue();
            chart.addSeries(entry.getKey(), getXData(results), getYData(results));
        }

        String fullPath = PATH + "/" + fileName;
        try {
            BitmapEncoder.saveBitmap(chart, fullPath, BitmapEncoder.BitmapFormat.JPG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;
    }

    private List<Double> getYData(List<SavedResult> results) {
        List<Double> yData = new ArrayList<>();
        double prev = 0;
        for (SavedResult result : results) {
            prev = prev + (double) result.getResult();
            yData.add(prev);
        }
        return yData;
    }

    private List<Date> getXData(List<SavedResult> results) {
        List<Date> xData = new ArrayList<>();
        for (SavedResult result : results) {
            xData.add(result.getDate());
        }
        return xData;
    }

    public String getPieChart(String titleText, Map<String, List<SavedResult>> stringListMap) throws NoResultForChartException {
        PieChart chart = new PieChartBuilder().width(800).height(600).title(getClass().getSimpleName()).build();

        int resultCount = 0;
        for (Map.Entry<String, List<SavedResult>> entry : stringListMap.entrySet()) {
            List<SavedResult> value = entry.getValue();
            int onePersonRead = 0;
            for (SavedResult savedResult : value) {
                onePersonRead = onePersonRead + savedResult.getResult();
            }
            chart.addSeries(entry.getKey() + "(" + onePersonRead + ")", onePersonRead);
            resultCount = resultCount + onePersonRead;
        }

        if (resultCount == 0) {
            throw new NoResultForChartException();
        }

        chart.setTitle(titleText + " " + resultCount);

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setAnnotationType(PieStyler.AnnotationType.Label);

        String fullPath = PATH + "/week";
        try {
            BitmapEncoder.saveBitmap(chart, fullPath, BitmapEncoder.BitmapFormat.JPG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;
    }
}