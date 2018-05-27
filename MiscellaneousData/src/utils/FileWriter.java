package utils;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;

public class FileWriter {

	public static void writeUTF8File(String filename, String s) {
	    try (BufferedWriter bw = Files.newBufferedWriter(new File(filename).toPath(), StandardCharsets.UTF_8)) {
			bw.write(s, 0, s.length());
			bw.newLine();
			System.out.println("Produced output file " + filename);
	    }
	    catch(Exception e) {
	        System.err.println("Failed to write to: " + filename + " " + e.getMessage());
	    }
	}
}
