package com.technobium;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.tartarus.snowball.ext.RomanianStemmer;

/**
 * Created by leo on 28/11/16.
 */
public class Word2VecDemo {

    public static void main(String[] args) throws IOException {

        //train();

        Word2Vec vec = WordVectorSerializer.readWord2VecModel(new File("input/roWord2Vec.txt"));

        Collection<String> lst = vec.wordsNearest("rege", 10);
        System.out.println(lst);

        lst = vec.wordsNearest("ibm", 10);
        System.out.println(lst);

    }

    public static void train() throws IOException {
        String inputFilePath = "output/capital/stiri.txt";

        // Strip white space before and after for each line
        SentenceIterator iter = new FileSentenceIterator(new File(inputFilePath));

        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());//(new RomanianStemmingPreprocessor());

        System.out.println("Staring training...");

        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(300)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        vec.fit();

        System.out.println("End training");

        WordVectorSerializer.writeWordVectors(vec, "input/roWord2Vec.txt");

    }

}

class RomanianStemmingPreprocessor extends CommonPreprocessor {
    @Override
    public String preProcess(String token) {
        String prep = super.preProcess(token);
        RomanianStemmer stemmer = new RomanianStemmer();
        stemmer.setCurrent(prep);
        stemmer.stem();

        return stemmer.getCurrent();
    }
}
