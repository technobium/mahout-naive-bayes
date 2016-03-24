# mahout-naive-bayes
Apache Mahaout naive Bayes demo. Code exlained here:
http://technobium.com/sentiment-analysis-using-mahout-naive-bayes/

# 新增训练数据input_yuqing
1) 数据分为5类，it，金融，教育，医疗，通信

#修改原始NaiveBayes.class
1）配置相关文件路径
2）输入文档为文件夹下放置文件的格式，添加文件夹转换成序列文件方法。
3）中文数据分类，5个类别，原始案例是2个类别，修改文件输出

#修改pom.xml
1)添加中文分词器，用于新测试文本的分词，以便于后续向量化、分类处理
2)添加mahout-intergrator，将训练文本从文件夹路径的格式转换成序列化格式。

#新增DirectoryFileContentSegment.java  
1）新增舆情数据预处理部分，在做训练前，先将文本进行中文分词
2）将舆情文本数据分词后再做bayes 训练测试，文本分类准确率比利用seq2sparse 中的分词器提高5个百分点。

