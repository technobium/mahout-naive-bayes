package com.technobium;

import org.apache.hadoop.io.InputBuffer;
import org.apache.hadoop.io.Text;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leo on 02/12/16.
 */
public class EmagReviewsCleaner {

    String reviewsWithScoresFileName = "output/emag/allReviewsRaw.txt";
    String cleanReviews = "output/emag/allReviewsClean.txt";

    public static void main(String[] args) throws IOException {
        EmagReviewsCleaner cleaner = new EmagReviewsCleaner();

        cleaner.clean();
    }

    public void clean() throws IOException {
        BufferedReader reader = new BufferedReader(
                new FileReader(reviewsWithScoresFileName));
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(cleanReviews));

        int i = 0;
        int k = 0;

        Map<Integer,Integer>  countMap = new HashMap<Integer, Integer>();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                try {
                    int rating = Integer.valueOf(tokens[0]);



                    if(countMap.get(rating) == null) {
                        countMap.put(rating, 1);
                    } else {
                        if(rating == 5 && countMap.get(rating) > 37000) {
                            // Get only 6000 good reviews
                            continue;
                        }

                        if (rating==3 || rating ==4){
                            continue;
                        }

                        int value = countMap.get(rating);
                        countMap.put(rating, value+1);
                    }

                    if (rating < 3) {
                        rating = 0;
                    } else if (rating > 3){
                        rating = 1;
                    } else {
                        // Ignore rating = 3
                        continue;
                    }

                    String review = tokens[1];

                    writer.write(rating + "\t" + review + "\n");
                    k++;

                } catch (Exception e) {
                    System.out.println("Could not parse: " + tokens[0]);
                    i++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
        System.out.println("Faulty: " + i);
        System.out.println("Wrote: " + k);
        for(int j=1; j<=5; j++) {
            System.out.println(  j + " -> " + countMap.get(j));
        }
    }


}
