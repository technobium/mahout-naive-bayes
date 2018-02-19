package com.technobium;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by leo on 29/11/16.
 */
public class EmagReviewsParser {

    String BASE_URL = "http://www.emag.ro/all-departments";
    String reviewsFileName = "output/emag/round2_reviews.txt";
    String reviewsWithScoresFileName = "output/emag/round2_reviewsWithScores.txt";

    int defaultTimeout = 60000;

    FileWriter reviewsWithScore = new FileWriter(reviewsWithScoresFileName,true); //the true will append the new data
    FileWriter justReviews = new FileWriter(reviewsFileName,true); //the true will append the new data

    private List<String> topLevelCategories = new ArrayList<String>();

    private List<String> exclusionPrefixes = Arrays.asList("https://www.emag.ro/","#m");

    public EmagReviewsParser() throws IOException {
    }

    public static void main(String[] args) throws ParseException, IOException, InterruptedException {
        EmagReviewsParser parser = new EmagReviewsParser();
        parser.parse();
        //parser.extractReviewsFromCategory("/coolere_memorii/c");
        //parser.extractReviewsFromProductPage("http://www.emag.ro/laptop-lenovo-v310-15isk-cu-procesor-intelr-coretm-i5-6200u-2-30-ghz-skylaketm-15-6-4gb-500gb-8gb-sshd-dvd-rw-intelr-hd-graphics-520-free-dos-black-80sy00h0ri/pd/DTNNP2BBM/");
    }

    public void parse() throws IOException, InterruptedException {

        Document doc = Jsoup.connect(BASE_URL)
                .timeout(defaultTimeout)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .get();
        Elements menuLinks = doc.select("div#departments-page").first().select("a[href]");

        // Build a list with all top level categories
        for(Element element:menuLinks) {
            String url = element.attr("href");
            boolean toBeExcluded = false;
            for(String prefix: exclusionPrefixes) {
                if (url.startsWith(prefix) || url.endsWith("/sd")) {
                    toBeExcluded = true;
                    break;
                }
            }
            if(!toBeExcluded) {
                topLevelCategories.add(url);
            }
        }

        // Go through all to top level categories
        for(int i=76; i < topLevelCategories.size(); i++) {
            String url = topLevelCategories.get(i);
            System.out.println(url);
            extractReviewsFromCategory(url);
        }
    }

    private void extractReviewsFromCategory(String categoryURL)  {

        int totalExtracted = 0;
        String sortedItemsURL = "http://www.emag.ro" + categoryURL + "/sort-reviewsdesc/c?pc=60";
        Document doc = null;
        try {
            doc = Jsoup.connect(sortedItemsURL)
                    .timeout(defaultTimeout)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .get();

            int maxPages = getMaxPages(doc);

            // Parse separately the first page
            parseProductsOnDocument(doc);
            for (int i = 2; i<= maxPages; i++) {
                String url = "http://www.emag.ro/"+categoryURL+"/sort-reviewsdesc/p" + i +"/c";
                Document subDoc = Jsoup.connect(url)
                        .timeout(defaultTimeout)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .get();
                int extractedCount = parseProductsOnDocument(subDoc);
                totalExtracted += extractedCount;

                if (extractedCount < 3) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Extracted :" + totalExtracted + " for category : " + categoryURL);
    }

    private int getMaxPages(Document doc) {
        int maxPages = 1;
        try {
            Element element = doc.select("a.emg-pagination-no").last();
            maxPages = Integer.valueOf(element.html());
        } catch (Exception e){
            System.out.println("Reviews could not be parsed: " + e.getLocalizedMessage() + " URL: " + doc.baseUri());
        }
        return maxPages;
    }

    private int parseProductsOnDocument(Document doc)  {

        int reviewsCount = 0;

        try {
            Elements products = doc.select("div.middle-container");
            for(Element product: products) {
                String productUrl = product.select("a[href]").first().attr("href");
                productUrl = "http://www.emag.ro" + productUrl;
                System.out.println("Start parsing for :" + productUrl);
                int extractedCount = extractReviewsFromProductPage(productUrl);
                reviewsCount += extractedCount;

                // Stop paring if we cannot find reviews
                if (extractedCount < 3) {
                    break;
                }
                System.out.println("Stop parsing for :" + productUrl);
            }
        } catch (Exception e) {
            // Skip the current page
        }

        return reviewsCount;
    }

    private int extractReviewsFromProductPage(String productUrl) throws IOException, InterruptedException {
        int reviewsCount = 0;

        List<Review> reviews = new ArrayList<Review>();
        try {
            Document doc = Jsoup.connect(productUrl)
                    .timeout(defaultTimeout)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .get();

            Thread.sleep(1000);

            String pid  = doc.select("input[type=hidden][name=pid]").first().attr("value");
            String did  = doc.select("input[type=hidden][name=did]").first().attr("value");
            String fid  = doc.select("input[type=hidden][name=fid]").first().attr("value");

            doc = Jsoup.connect("http://www.emag.ro/ajax-product-feedback")
                    .data("s","0")
                    .data("o","age")
                    .data("filter","age")
                    .data("l","1000")
                    .data("cl","300")
                    .data("json_response","1")
                    .data("pid",pid)
                    .data("did",did)
                    .data("fid", fid)
                    .timeout(defaultTimeout)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .post();

            Thread.sleep(1000);

            String jsonString = doc.body().text();
            ObjectMapper mapper = new ObjectMapper();

            // Read the response as a tree model
            final JsonNode response = mapper.readTree(jsonString).path("reviews");

            // Create the collection type (since it is a collection of Reviews)
            final CollectionType collectionType =
                    TypeFactory
                            .defaultInstance()
                            .constructCollectionType(List.class, Review.class);
            // Convert the tree model to the collection (of Reviews-objects)
            reviews = mapper.reader(collectionType).readValue(response);

            for(Review review: reviews) {
                String cleanContent = review.getContent().replace("<br />","");
                justReviews.write(cleanContent + "\n");

                String reviewItem = review.getRating() + "\t" + cleanContent;
                reviewsWithScore.write(reviewItem +"\n"); // Write the review to a file

                System.out.println(reviewItem);
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Reviews could not be parsed :" + e.getLocalizedMessage());
        }

        return reviews.size();
    }

//    private int extractRatingValue(Element element){
//        String style = element.select("ul.star-rating-active").first().attr("style");
//
//        Matcher m = Pattern.compile("\\d+").matcher(style);
//        m.find();
//        Integer rating = Integer.valueOf(m.group(0));
//
//        return rating / 20;
//    }
//
//    private String extractRatingText(Element element){
//        String content = element.select("div.pad-btm-xs").last().text();
//        return  content;
//    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Review {
    private int rating;
    private String content;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating=" + rating +
                ", content='" + content + '\'' +
                '}';
    }
}
