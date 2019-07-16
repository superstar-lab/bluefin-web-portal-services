package com.mcmcg.ico.bluefin.model;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import lombok.Data;

@Data
public class BatchFileObjects {

	File file;
	CSVFormat csvFileFormat;
	FileWriter fileWriter;
	CSVPrinter csvFilePrinter;
}
