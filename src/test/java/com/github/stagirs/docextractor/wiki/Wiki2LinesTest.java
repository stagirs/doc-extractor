/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.stagirs.docextractor.wiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.junit.Test;

/**
 *
 * @author Dmitriy Malakhov
 */
public class Wiki2LinesTest {
    /**
     * Скачиваем дамп русской википедии https://dumps.wikimedia.org/ruwiki/
     * Нам нужны архивы pages-articles1.
     * Разаврхивируем их в папку in
     * Запускаем класс, получаем файл out, в нем в каждой строчке содержится статья,
     * эта строчка может быть обработана классом WikiDocProcessor, в результате получаем объект Document
     */
    public static File in = new File("e:/ru");
    public static File out = new File("e:/docs");
    
    @Test
    public void test() throws IOException {
        out.delete();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "utf-8"));
        try{
            for(File file : in.listFiles()){
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                try{
                    String page = "";
                    String line;
                    while((line = br.readLine()) != null){
                        if(line.trim().equals("<page>")){
                            page += line.replace("\t", " ") + "\t";
                            continue;
                        }
                        if(line.trim().equals("</page>")){
                            bw.append(page + line.replace("\t", " ") + "\n");
                            page = "";
                            continue;
                        }
                        if(!page.isEmpty()){
                            page += line.replace("\t", " ") + "\t";
                        }
                    }
                }finally{
                    br.close();
                }
            }
        }finally{
            bw.close();
        }
    }
}
