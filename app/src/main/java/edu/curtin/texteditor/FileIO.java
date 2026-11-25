package edu.curtin.texteditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

// Handles file input and output
public class FileIO
{
    private static final int BUFFER_SIZE = 4096;
    public FileIO() {}

    //Reads the entire file using UTF-8, UTF-16 or UTF-32
    public String load(File file, String encoding) throws IOException
    {
        Charset charset = Charset.forName(encoding);
        StringBuilder sb = new StringBuilder();
        try(InputStream is = new FileInputStream(file);
            Reader reader = new InputStreamReader(is, charset))
        {
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead = reader.read(buffer);
            while(charsRead != -1)
            {
                sb.append(buffer, 0, charsRead);
                charsRead = reader.read(buffer);
            }
        }
        return sb.toString();
    }

    //Writes the entire file using UTF-8, UTF-16 or UTF-32
    public void save(File file, String text, String encoding) throws IOException
    {
        Charset charset = Charset.forName(encoding);
        try(OutputStream os = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(os, charset))
        {
            writer.write(text);
        }
    }
}