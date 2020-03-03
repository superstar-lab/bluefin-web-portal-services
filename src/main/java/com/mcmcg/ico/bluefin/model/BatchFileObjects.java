package com.mcmcg.ico.bluefin.model;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class BatchFileObjects {

	File file;
	CSVFormat csvFileFormat;
	FileWriter fileWriter;
	CSVPrinter csvFilePrinter;
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public CSVFormat getCsvFileFormat() {
		return csvFileFormat;
	}
	public void setCsvFileFormat(CSVFormat csvFileFormat) {
		this.csvFileFormat = csvFileFormat;
	}
	public FileWriter getFileWriter() {
		return fileWriter;
	}
	public void setFileWriter(FileWriter fileWriter) {
		this.fileWriter = fileWriter;
	}
	public CSVPrinter getCsvFilePrinter() {
		return csvFilePrinter;
	}
	public void setCsvFilePrinter(CSVPrinter csvFilePrinter) {
		this.csvFilePrinter = csvFilePrinter;
	}
	
	
}
