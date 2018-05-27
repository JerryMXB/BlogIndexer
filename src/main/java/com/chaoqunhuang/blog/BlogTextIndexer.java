package com.chaoqunhuang.blog;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BlogTextIndexer {
    private static ArrayList<File> files = new ArrayList<>();
    private static StandardAnalyzer analyzer = new StandardAnalyzer();
    private static IndexWriter writer;

    public static void main(String args[]) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        System.out.println("Enter the path where the index will be created: (e.g. /tmp/index)");
        String indexPath = br.readLine();
        System.out.println("Enter the path where the html or text files to be indexed resides: (e.g. /tmp/blog/html)");
        String filesPath = br.readLine();

        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, config);

        indexFileOrDirectory(filesPath);
        writer.close();
        System.out.println("Indexing finished");
    }

    /**
     * Indexes a file or directory
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public static void indexFileOrDirectory(String fileName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : files) {
            try {
                Document doc = new Document();

                /*
                ===================================================
                add contents of file
                ===================================================
                */
                doc.add(new TextField("contents", extractTextFromHtml(f), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            }
        }
    }

    /**
     * Add file or files in directory to list to be indexed
     * @param file file or directory path
     */
    private static void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            //===================================================
            // Only index text files
            //===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")) {
                files.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Extract the text from html file using jsoup
     * @param input file path
     * @return Extracted text
     * @throws IOException
     */
    private static String extractTextFromHtml(File input) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.parse(input, "UTF-8");
        System.out.println(doc.text());
        return doc.text();
    }
}
