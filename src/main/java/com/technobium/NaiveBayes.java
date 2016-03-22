package com.technobium;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.splitWord.analysis.BaseAnalysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.classifier.naivebayes.StandardNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.apache.mahout.vectorizer.TFIDF;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class NaiveBayes {

	Configuration configuration = new Configuration();

	String inputFilePath = "input_yuqing";
	String sequenceFilePath = "input_yuqing/yuqing-seq";
	String labelIndexPath = "input_yuqing/yuqing-labelindex";
	String modelPath = "input_yuqing/yuqing-model";
	String vectorsPath = "input_yuqing/yuqing-vectors";
	String dictionaryPath = "input_yuqing/yuqing-vectors/dictionary.file-0";
	String documentFrequencyPath = "input_yuqing/yuqing-vectors/df-count/part-r-00000";

	public static void main(String[] args) throws Throwable {
		NaiveBayes nb = new NaiveBayes();
        nb.directoryToSequenceFile();
		nb.sequenceFileToSparseVector();
		nb.trainNaiveBayesModel();
        String yuqing="融资威力，主力持续逼空！沪指涨2.15%七连阳、重回3000点，时隔四个多月，券商接近涨停，创业板重回2200点上方。今天量超预期，券商也超预期，复盘大师没想到大盘强到这地步。股市迅速转热，行情来的太突然，主力高举高打过60日线，量能放出的太快，沪指回踩一下3000点附近确认支撑最好。短线获利盘较多，明日或继续在3000点震荡整理。朋友说：当A股开始不讲道理上涨时，意味着流氓行情到来了，确实，这个世界没有比A股更刺激的市场，跌的时候跌得傻，涨的时候又太快，简直就是个精神病。一、沪指突破3000点券商板块近涨停：沪指高开后，在券商板块迎来重大利好的情况下，指数继续走高，而券商板块一度集体涨停，沪指也重回3000点。而创业板也同样表现不错，目前来看，大盘继续普涨，市场投资热情较高，量能方面也表现较为理性。两点左右，股指继续震荡回落，盘中跌破3000点整数关口，这也是预期中的事，券商开板不少。截至收盘，沪指涨2.15%，报3018.80点，成交3808亿元；深成指涨2.64%，报10394.4点，成交5228亿元。创业板上涨2.26%报2227.03点。个股操作迷茫，可微信fupan588，分析经验技巧，带你走上炒股致富的道路！盘面上，权重、题材齐发力，券商股集体涨停。个股方面，两市2400余只个股上涨，上百只个股涨停。截止A股收盘，沪股通资金净流入8434万元，早盘资金净流入一度超10亿；港股通资金流入5.78亿元。";
        nb.classifyNewYuqing(yuqing);
	}

    public void directoryToSequenceFile() throws Exception {
       try {
           // Configuration conf = new Configuration();
           // conf.addResource(new Path("/usr/local/hadoop/conf/core-site.xml"));
           // FileSystem fs = FileSystem.getLocal(configuration);            

           // String input = "input_yuqing";
           // String output = "yuqing-seq" ;
            
           // Path in = new Path(input);
           // Path out = new Path(output);
            
           // FileSystem fs = FileSystem.get(configuration);
            
          //  if(fs.exists(in)){
          //  if(fs.exists(out)){
                //boolean参数是，是否递归删除的意思
          //      fs.delete(out, true);
          //      }
            SequenceFilesFromDirectory sffd = new SequenceFilesFromDirectory();
            sffd.run(new String[]{ "-i", inputFilePath,"-o",sequenceFilePath, "-c","UTF-8", "-ow" });
          //  String[] params = new String[]{"-i",input,"-o",output};
          //  ToolRunner.run(sffd, params);
            
            }
          catch (Exception e) {
            // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("文件序列化失败！");
                System.exit(1);
          }     
    }
	public void inputDataToSequenceFile() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
		FileSystem fs = FileSystem.getLocal(configuration);
		Path seqFilePath = new Path(sequenceFilePath);
		fs.delete(seqFilePath, false);
		SequenceFile.Writer writer = SequenceFile.createWriter(fs,configuration, seqFilePath, Text.class, Text.class);
		int count = 0;
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\t");
				writer.append(new Text("/" + tokens[0] + "/tweet" + count++),
						new Text(tokens[1]));
			}
		} finally {
			reader.close();
			writer.close();
		}
	}

	void sequenceFileToSparseVector() throws Exception {
		SparseVectorsFromSequenceFiles svfsf = new SparseVectorsFromSequenceFiles();
        //ansj segment     
		//svfsf.run(new String[] {"-i", sequenceFilePath, "-o", vectorsPath,"-a", org.ansj.splitWord.analysis.ToAnalysis,"-ow"});
        svfsf.run(new String[] {"-i", sequenceFilePath, "-o", vectorsPath,"-ow"});
	}

	void trainNaiveBayesModel() throws Exception{
		TrainNaiveBayesJob trainNaiveBayes = new TrainNaiveBayesJob();
		trainNaiveBayes.setConf(configuration);
		trainNaiveBayes.run(new String[] { "-i",vectorsPath + "/tfidf-vectors", "-o", modelPath, "-li",	labelIndexPath, "-el", "-c", "-ow" });
	}

	private void classifyNewYuqing(String yuqing) throws IOException {
		System.out.println("yuqing: " + yuqing);
        //fen ci 
        //List<String> words = new ArrayList<String>();    
        String yuqing_fenci="";
        
        List<Term> terms = org.ansj.splitWord.analysis.ToAnalysis.parse(yuqing);
        for(Term term : terms) {
         //   words.add(term.toString());
            yuqing_fenci = yuqing_fenci +" "+ term.toString();
        }
         System.out.println("yuqing_fenci: " + yuqing_fenci);
		Map<String, Integer> dictionary = readDictionary(configuration,	new Path(dictionaryPath));
		Map<Integer, Long> documentFrequency = readDocumentFrequency(configuration, new Path(documentFrequencyPath));

		Multiset<String> words = ConcurrentHashMultiset.create();

		// Extract the words from the new tweet using Lucene
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_32);
		TokenStream tokenStream = analyzer.tokenStream("text",new StringReader(yuqing_fenci));
		CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();

		int wordCount = 0;
		while (tokenStream.incrementToken()) {
			if (termAttribute.length() > 0) {
				String word = tokenStream.getAttribute(CharTermAttribute.class).toString();
				Integer wordId = dictionary.get(word);
				// If the word is not in the dictionary, skip it
				if (wordId != null){
					words.add(word);
					wordCount++;
				}
			}
		}
		tokenStream.end();
		tokenStream.close();

		int documentCount = documentFrequency.get(-1).intValue();

		// Create a vector for the new tweet (wordId => TFIDF weight)
		Vector vector = new RandomAccessSparseVector(10000);
		TFIDF tfidf = new TFIDF();
		for (Multiset.Entry<String> entry : words.entrySet()) {
			String word = entry.getElement();
			int count = entry.getCount();
			Integer wordId = dictionary.get(word);
			Long freq = documentFrequency.get(wordId);
			double tfIdfValue = tfidf.calculate(count, freq.intValue(),wordCount, documentCount);
			vector.setQuick(wordId, tfIdfValue);
		}

		// Model is a matrix (wordId, labelId) => probability score
		NaiveBayesModel model = NaiveBayesModel.materialize(new Path(modelPath), configuration);
		StandardNaiveBayesClassifier classifier = new StandardNaiveBayesClassifier(model);

		// With the classifier, we get one score for each label.The label with
		// the highest score is the one the tweet is more likely to be
		// associated to
		Vector resultVector = classifier.classifyFull(vector);
		double bestScore = -Double.MAX_VALUE;
		int bestCategoryId = -1;
		for (Element element : resultVector.all())
        {
			int categoryId = element.index();
			double score = element.get();
			if(score > bestScore){
				bestScore = score;
				bestCategoryId = categoryId;
			}
			if(categoryId == 0){
				System.out.println("Probability of being it: " + score);
			}else if(categoryId == 1){
				System.out.println("Probability of being jiaoyu: " + score);
			}else if(categoryId == 2){
                System.out.println("Probability of being jinrong: " + score);
            }else if(categoryId == 3){
                System.out.println("Probability of being tongxin: " + score);
            }else{
                System.out.println("Probability of being yiniao: " + score);
            }

		}
		if (bestCategoryId == 0) {
			System.out.println("The yuqing is it");
		} else if(bestCategoryId == 1) {
			System.out.println("The yuqing is jiaoyu ");
		} else if(bestCategoryId == 2){
             System.out.println("The yuqing is jinrong ");
        } else if(bestCategoryId == 3){
             System.out.println("The yuqing is tongxin");
        }else{
             System.out.println("The yuqing is yiniao ");
        }

		analyzer.close();
	}

	public static Map<String, Integer> readDictionary(Configuration conf,Path dictionnaryPath) {
		Map<String, Integer> dictionnary = new HashMap<String, Integer>();
		for (Pair<Text, IntWritable> pair : new SequenceFileIterable<Text, IntWritable>(dictionnaryPath, true, conf)) 
        {
			dictionnary.put(pair.getFirst().toString(), pair.getSecond().get());
		}
		return dictionnary;
	}

	public static Map<Integer, Long> readDocumentFrequency(Configuration conf,Path documentFrequencyPath) {
		Map<Integer, Long> documentFrequency = new HashMap<Integer, Long>();
		for (Pair<IntWritable, LongWritable> pair : new SequenceFileIterable<IntWritable, LongWritable>(
				documentFrequencyPath, true, conf)) 
        {
			documentFrequency.put(pair.getFirst().get(), pair.getSecond().get());
		}
		return documentFrequency;
	}
}
