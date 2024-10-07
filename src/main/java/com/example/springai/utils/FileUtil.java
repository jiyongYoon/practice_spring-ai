package com.example.springai.utils;

import java.io.*;

public class FileUtil {
    public static void save(String data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(data);
        } catch (IOException e) {
            System.out.println("An error occurred while saving the file.");
            e.printStackTrace();
        }
    }

    public static String read(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("해당 파일을 찾을 수 없습니다. filePath = %s", filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }
}
