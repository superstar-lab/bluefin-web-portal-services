package com.mcmcg.ico.bluefin.rest.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.BatchUploadService;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/batch-upload")
public class BatchUploadRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadRestController.class);

	@Autowired
	private BatchUploadService batchUploadService;

	@Autowired
	private LegalEntityAppService legalEntityAppService;

	@ApiOperation(value = "getBatchUpload", nickname = "getBatchUpload")
	@GetMapping(value = "{id}", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = BatchUpload.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public BatchUpload get(@PathVariable Long id) {
		LOGGER.debug("Getting batch upload by id = {} ", id);
		return batchUploadService.getBatchUploadById(id);
	}

	@ApiOperation(value = "getBatchUploads", nickname = "getBatchUploads")
	@GetMapping(produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = BatchUpload.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public Iterable<BatchUpload> get(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "15") int size,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "noofdays", required = false) Integer noofdays) {
		LOGGER.debug("Getting all batch uploads size={} ", size);
		if (noofdays == null) {
			return batchUploadService.getAllBatchUploads(page, size, sort);
		} else {
			return batchUploadService.getBatchUploadsFilteredByNoofdays(page, size, sort, noofdays);
		}
	}

	@ApiOperation(value = "createBatchUpload", nickname = "createBatchUpload")
	@PostMapping(produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header"),
			@ApiImplicitParam(name = "LegalEntityName", value = "Legal Entity", required = true, dataType = "string", paramType = "form"),
			@ApiImplicitParam(name = "request", value = "request", required = true, paramType = "body") })
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = BatchUpload.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public BatchUpload upload(@ApiIgnore MultipartHttpServletRequest request, @ApiIgnore Authentication authentication) {
		LOGGER.debug("Uploading new ACF file {}", request.getFileMap());
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		String legalEntityName = request.getParameter("legalEntityName");
		if (StringUtils.isBlank(legalEntityName)) {
			throw new CustomException("Legal Entity name can not be blank");
		}
		if (batchUploadService.checkLegalEntityStatus(legalEntityName)) {
			throw new CustomException(String.format("Legal Entity = [%s] is Inactive", legalEntityName));
		}
		Map<String, MultipartFile> filesMap = request.getFileMap();
		MultipartFile[] filesArray = getFilesArray(filesMap);
		if (filesArray.length != 1) {
			throw new CustomBadRequestException("A file must be uploaded");
		}
		MultipartFile file = filesArray[0];

		byte[] bytes = null;
		int lines = 0;
		try {
			bytes = file.getBytes();
			lines = countLines(bytes);
		} catch (IOException e1) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Failed to stream file", e1);
			}
			throw new CustomBadRequestException("Unable to stream file: " + file.getOriginalFilename());
		}
		String fileName = file.getOriginalFilename();
		if(!containsIgnoreCase(fileName, legalEntityName)){
			throw new CustomException("Either Wrong Legal Entity is selected or Legal Entity name is not mentioned in the file name");
		}
		List<LegalEntityApp> legalEntityList = legalEntityAppService.getActiveLegalEntities(authentication);
		legalEntityList = legalEntityList.stream().filter(le -> le.getIsActiveForBatchUpload() == 1
				&& !le.getLegalEntityAppName().equalsIgnoreCase(legalEntityName)).collect(Collectors.toList());
		for (LegalEntityApp legalEntity : legalEntityList) {
			if(containsIgnoreCase(fileName, legalEntity.getLegalEntityAppName())){
				throw new CustomException("Multiple Legal Entity name is not allowed in single file name");
			}
		}
		LOGGER.info("Encoding file content to send it as stream");
		String stream = new String(Base64.encodeBase64(bytes));
		return batchUploadService.createBatchUpload(authentication.getName(), fileName, stream, lines,
				request.getHeader("X-Auth-Token"), legalEntityName);
	}

	private MultipartFile[] getFilesArray(Map<String, MultipartFile> filesMap) {
		Set<String> keysSet = filesMap.keySet();
		MultipartFile[] fileArray = new MultipartFile[keysSet.size()];
		int i = 0;
		for (String key : keysSet) {
			if (key.indexOf("file") != -1) {
				fileArray[i] = filesMap.get(key);
				i++;
			}
		}
		return fileArray;

	}

	public static int countLines(byte[] fileContent) throws IOException {
		LOGGER.info("Decoding stream body to count file lines");
		InputStream is = new ByteArrayInputStream(fileContent);
		InputStreamReader inR = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(inR);
		int lines = -1;
		String line;
		while ((line = reader.readLine()) != null) {
			if (!"".equals(line.trim())) {
				lines++;
			}
		}
		return lines;
	}
	
	public static boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }
}
