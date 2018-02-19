package com.technobium;

import org.bytedeco.javacpp.presets.opencv_core;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by leo on 30/11/16.
 */
public class CapitalNewsParser {

    String BASE_URL = "http://www.capital.ro/arhiva/";
    String filename= "output/capital/stiri.txt";
    int defaultTimeout = 60000;

    public CapitalNewsParser() throws IOException {
    }
    public static void main(String[] args) throws ParseException, IOException {

        CapitalNewsParser parser = new CapitalNewsParser();
        // parser.parse();
        //parser.parseSingleArticle("http://www.capital.ro/arhiva/2006-11-30");
//        parser.writeNewsFromDate("http://www.capital.ro/arhiva/2015-09-15");

//        String input = "ArcelorMittal va închide o uzină anul viitor Autor: capital.ro | marţi, 15 septembrie 2015 | 0 Comentarii 1442335913 ArcelorMittal va opri în iunie 2016 producţia la fabrica sa din Bettembourg (Luxemburg), iar tuturor angajaţilor le vor fi oferite 'soluţii adecvate', se arată într-un articol apărut marţi în publicaţia 'Luxemburger Wort'. ArcelorMittal - cel mai mare producător de oţel din lume, cu sediul central în Luxemburg, a dat asigurări că producţia va continua până la mijlocul anului viitor pentru a răspunde comenzilor actuale.  După 2016, portofoliul de clienţi va fi preluat de grupul austriac Voestalpine, iar clădirea unde sunt liniile de producţie va fi achiziţionată de compania Sam Hwa, din Coreea de Sud.  Cei 65 de angajaţi ai fabricii din Bettembourg vor primi alte însărcinări în cadrul companiei, a anunţat ArcelorMittal.  Recent, ArcelorMittal a revizuit în scădere estimările privind consumul de oţel pe plan mondial în 2015, în special în SUA, China, Brazilia, Rusia şi statele vecine.  Compania se aşteaptă la un consum stabil de oţel în acest an, după ce anterior previzionase o creştere de 0,5 - 1,5%. ArcelorMittal, cel mai mare producător de oţel din lume, şi-a menţinut estimarea de profit la 6 - 7 miliarde de dolari în 2015.  În trimestrul doi din 2015, datoria netă s-a menţinut la 16,6 miliarde de dolari, similar cu nivelul înregistrat la sfârşitul primului trimestru din acest an.  Profitul operaţional a înregistrat un declin de 21%, în perioada aprilie - iunie 2015, la 1,4 miliarde de dolari, de la 1,76 miliarde de dolari în trimestrul doi din 2014, în timp ce analiştii se aşteptau la 1,34 miliarde de dolari.  Compania se bazează pe cererea din Europa, cea mai mare piaţă a sa, pentru a compensa reducerea cererii în SUA, China, Brazilia, Rusia şi statele vecine.  Livrările de oţel ale ArcelorMittal au crescut cu 2,8% în trimestrul doi din 2015, la 22,2 milioane de tone.  ArcelorMittal este cea mai mare companie siderurgică şi minieră din lume, prezentă în peste 60 de ţări, în 19 dintre acestea având unităţi proprii de producţie. În 2014, ArcelorMittal a înregistrat venituri de 79,3 miliarde dolari şi o producţie de oţel de 93,1 milioane de tone, iar producţia de minereu de fier a fost de 63,98 milioane de tone.  În România, ArcelorMittal deţine unităţi de producţie la Galaţi (est), Iaşi (est), Roman (nord-est) şi Hunedoara (centru).  AGERPRES";
//        System.out.println(parser.clearArticle(input));

       // parser.parseSingleArticle("http://www.capital.ro/download-pe-piax163a-pensiilor-pivate-104743.html", null);

    }

    public void parse() throws ParseException, IOException {

        FileWriter fw = new FileWriter(filename,true); //the true will append the new data

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse("2001-05-06");
        Date endDate = formatter.parse("2005-01-01");

        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            writeNewsFromDate(date, fw);
        }

        fw.close();
    }

    private void writeNewsFromDate(LocalDate date, FileWriter fileWriter) {

        Document doc = null;
        try {
            doc = Jsoup.connect(BASE_URL + date)
                        .timeout(defaultTimeout)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .get();

            parseNewsForPage(doc, fileWriter);

            // See how many pages we need to parse for the current page, besides the first page
            Element pagination = doc.select("div.search-nav").first();
            if (pagination == null) {
                // No items found, move to the next date
                return;
            }

            int maxPages = getMaxPages(pagination.text());

            for(int i=2; i<= maxPages; i++) {
                String nextSubPageURL = BASE_URL + date+ "?&page=" + i;
                doc = Jsoup.connect(nextSubPageURL)
                        .timeout(defaultTimeout)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .get();
                parseNewsForPage(doc, fileWriter);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getMaxPages(String text) {
        Integer maxPages = 0;
        try {
            Pattern p = Pattern.compile("din\\s[0-9]+");
            Matcher m = p.matcher(text);
            String maxPagesString = "";
            if (m.find()) {
                maxPagesString = m.group(0);
                // Extract just the number
                maxPagesString = maxPagesString.substring(maxPagesString.indexOf(" ") + 1);
            }
            maxPages = Integer.valueOf(maxPagesString);
        } catch (Exception e) {
            // Do nothing, we will parse only the first page
        }
        return maxPages;
    }

    void parseNewsForPage(Document doc, FileWriter fileWriter) {
        Elements articles = doc.select("div.search-result");
        for(Element article : articles) {
            String articleURL = article.select("a[href]").first().attr("href");
            parseSingleArticle(articleURL, fileWriter);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void parseSingleArticle(String url, FileWriter fileWriter) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .timeout(defaultTimeout)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .get();


            Element continut = doc.select("div[itemprop$=articleBody]").first();

            if (continut == null) {
                // Skip the article because we could not find the body
                return;
            }

            System.out.println(url);
            String cleanArticle = clearArticle(continut.text());
            if (cleanArticle.length() > 0) {
                fileWriter.write(cleanArticle + "\n");
            } else {
                System.out.println("Skipped");
            }

        } catch (IOException e) {
            // Skip the article
        }
    }

    public String clearArticle(String inputText) {
        String resultText = inputText;
        Pattern p = Pattern.compile("Comentarii\\s[0-9]+");
        Matcher m = p.matcher(inputText);
        String maxPagesString = "";
        if (m.find()) {
            int endIndex = m.end();
            if (endIndex + 1 <= inputText.length()) {
                resultText = inputText.substring(endIndex + 1);
            } else {
                resultText = "";
            }
        }

        if (resultText.endsWith("AGERPRES")) {
            int lastIndex = resultText.lastIndexOf("AGERPRES");
            resultText = resultText.substring(0, lastIndex);
        }

        return resultText;
    }
}
