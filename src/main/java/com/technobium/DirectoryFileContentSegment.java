package com.technobium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.splitWord.analysis.BaseAnalysis;

/*
segment the file of directory input_yuqing
segment:ansj
*/

public class DirectoryFileContentSegment{

 public static void main(String[] args){
      String path="input_seg/";
      DirectoryFileContentSegment dfcs = new DirectoryFileContentSegment();  
      DirectoryFileContentSegment.ReadAllFile(path);
 }

  //读取一个文件夹下所有文件及子文件夹下的所有文件  
    public static void ReadAllFile(String filePath){  
        File f = null;  
        f = new File(filePath);  
        File[] files = f.listFiles(); // 得到f文件夹下面的所有文件。  
      //  List<File> list = new ArrayList<File>();  
        for (File file : files) {  
            if(file.isDirectory()) {  
                 System.out.println("文件夹："+file);
                //如何当前路劲是文件夹，则循环读取这个文件夹下的所有文件  
                ReadAllFile(file.getAbsolutePath());  
            } else if(file.isFile()){  
                System.out.println("文     件："+file);
        //        list.add(file); 
                try{
                     segment(file);
                }catch(IOException e){
                  e.printStackTrace();
                } 
            }  
        }    
    }

     /*
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     * @param filePath
     */
/*    public static void readTxtFile(String filePath){
        try {
                String encoding="utf-8";
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                        System.out.println(lineTxt);
                    }
                    reader.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
    }
*/

    //fenci
     public static void segment(File file) throws IOException {
    //  String fileName = "C:\\Users\\zh\\Desktop\\Ansj\\javatestfile.txt";
    //    File file = new File(fileName);
        BufferedReader reader = null;
    //  FileWriter fw = new FileWriter("C:\\Users\\zh\\Desktop\\Ansj\\javatestfile_fenci.txt",true);
        FileWriter fw = new FileWriter(file.toString()+"_seg",true);
        try {
        //    System.out.println("以行为单位读取文件内容，一次读一整行利用精准分词：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            //int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号+精准分词
                List<Term> parse_to = ToAnalysis.parse(tempString);

                StringBuffer sbuf=new StringBuffer();
                for (int i = 0; i < parse_to.size(); i++)
                {
                    //String[] segword=parse_to.get(i).toString().split("/"); 
                    //System.out.println(segword.toString());
                    sbuf.append(parse_to.get(i));
                    //sbuf.append(segword[0]);
                    sbuf.append(" ");
                }
                    fw.write(sbuf.toString(), 0, sbuf.toString().length());
                    fw.flush();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    fw.close();
                } catch 
                (IOException e1) {
                 e1.printStackTrace();
                }
            }
        }
    } 

}
