package com.technobium;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by leo on 28/11/16.
 */
public class HotnewsNewsParser {

    String BASE_URL = "http://www.hotnews.ro/arhiva/";
    String filename= "input/stiri.txt";

    public static void main(String[] args) throws ParseException, IOException {
        HotnewsNewsParser parser = new HotnewsNewsParser();
       // parser.parse();
    }

    public void parse() throws ParseException, IOException {

        FileWriter fw = new FileWriter(filename,true); //the true will append the new data

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse("2015-04-01");
        Date endDate = formatter.parse("2016-11-25");

        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            writeNewsFromDate(date, fw);
        }

        fw.close();
    }

    private void writeNewsFromDate(LocalDate date, FileWriter fileWriter) throws IOException {
        Document doc = Jsoup.connect(BASE_URL + date).get();
        System.out.println(date);
        Elements paginare = doc.select("div.paginare").first().select("a[href]");
        for(Element element: paginare) {
            String newsPageURL = element.attr("href");
            writeNewsFromPageURL(newsPageURL, fileWriter);
        }
    }

    private void writeNewsFromPageURL(String url, FileWriter fileWriter) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements news = doc.select("span.stire");
        for(Element element: news) {
            fileWriter.write(element.html()+"\n");
        }
    }


}
