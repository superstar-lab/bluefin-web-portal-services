package com.mcmcg.ico.bluefin.util;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class FileUtil {
	
	public static File generateCSVFile(String reportPath, Object[] header, Map<Integer, List<String>> data) {
		log.info("Entering to FileUtil -> generateCSVFile, report path: {}, data size: {}", reportPath, data.size());
		File file = createFile(reportPath, Optional.empty());
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		fillCSVData(file, header, data, csvFileFormat);
		return file;
	}
	
	public static File createFile(String reportPath, Optional<String> fileName) {
		log.info("Entering to FileUtil -> createFile, report path: {}", reportPath);
		File file = null;
				
		try {
			String name = fileName.isPresent() ? fileName.get() : UUID.randomUUID().toString();
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, name + ".csv");
			boolean flag = file.createNewFile();
			if (flag) {
				log.info("FileUtil -> createFile. Report file Created {}", file.getName());
			}
		} catch (Exception e) {
			log.error("FileUtil -> createFile. Error creating file in path: {}. Error: {}", reportPath, e);
		}
		
		return file;
	}
	
	public static void fillCSVData(File file, Object[] header, Map<Integer, List<String>> data, CSVFormat csvFileFormat) {
		log.info("Entering to FileUtil -> fillCSVData, file: {}, header: {}, data size: {}, csvFileFormat: {}", file, header, data.size(), csvFileFormat);
		try (FileWriter fileWriter = new FileWriter(file);
				CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {
			// Create CSV file header
			csvFilePrinter.printRecord(header);
			log.info("FileUtil -> fillCSVData. Data result size: {}", data.size());

			// Write a new object list to the CSV file
			for (Entry<Integer, List<String>> entry : data.entrySet()) {
				List<String> dataRecord = new ArrayList<>();
				dataRecord.add(String.valueOf(entry.getKey()));
				entry.getValue().forEach(x -> dataRecord.add(x));
				csvFilePrinter.printRecord(dataRecord);
			}
			log.info("FileUtil -> fillCSVData. CSV file report was created successfully!");
		} catch (IOException e) {
			log.error("FileUtil -> fillCSVData. Error when creating CSV file: {}", e.toString());
		}
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		log.info("Entering to FileUtil -> openInputStream, file: {}", file);
        if (file.exists()) {
            if (file.isDirectory()) {
            	log.error("FileUtil -> openInputStream. File {} exists but is a directory", file);
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
            	log.error("FileUtil -> openInputStream. File {} cannot be read", file);
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
        	log.error("FileUtil -> openInputStream. File {} does not exist", file);
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

	public static int copy(InputStream in, OutputStream out) throws IOException {
		log.info("Entering to FileUtil -> copy, in: {}, out: {}", in, out);
		try {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
			}
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	public static ResponseEntity<String> deleteTempFile(File downloadFile, HttpServletResponse response) {
		log.info("Entering to FileUtil -> deleteTempFile, downloadFile: {}", downloadFile);
		InputStream inputStream = null;
		try {
			inputStream = openInputStream(downloadFile);
			response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
			response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());

			copy(inputStream, response.getOutputStream());
			log.debug("Deleting temp file: {}", downloadFile.getName());
			boolean deleted = Files.deleteIfExists(downloadFile.toPath());
			log.debug("File deleted ? {}",deleted);
			return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
		} catch(Exception e) {
			log.error("FileUtil -> deleteTempFile. An error occurred when removing the report file",e);
			throw new CustomException("An error occurred when removing the report file");
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("An error occurred to close input stream", e);
				}
			}
		}
	}
}
