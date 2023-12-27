package com.th.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

import org.apache.commons.collections4.Get;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.th.entity.Employee;
import com.th.repository.EmployeesRepository;

@Service
public class EmployeeService {

	@Autowired
	private EmployeesRepository employeesRepository;

	public List<Employee> getAllEmployees() {
		return employeesRepository.findAll();
	}

	public Employee addNewEmployee(Employee employee) {
		return employeesRepository.save(employee);
	}
	
	public ResponseEntity<Object> uploadFile(MultipartFile fileToUpload) throws Exception{
		LinkedHashMap<String, Object> res = new LinkedHashMap<>();
		ResponseEntity<Object> responseEntity = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD'T'HH:MM:SS'Z'");
		
		try {
			byte[] objWriteResponse = fileToUpload.getBytes();
			String timeStamp = dateFormat.format(new Date());
			System.out.println("\n\n objWriteResponse: "+objWriteResponse+"\n\n");

			// Get the current working directory
            String uploadDirectory = System.getProperty("user.dir")+"/template/";
			System.out.println("\n\n currentWorkingDir: "+uploadDirectory+"\n\n");
			
			// Create the directory if it doesn't exist
            Path directoryPath = Paths.get(uploadDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
			// Create the file path
			Path filePath = Paths.get(uploadDirectory,fileToUpload.getOriginalFilename());
			System.out.println("\n\n filePath: "+filePath+"\n");
            
			// Write the bytes to the file
			Files.write(filePath, objWriteResponse);
			System.out.println("\n\n objWriteResponse: "+objWriteResponse+"\n\n");
			
			UserDefinedFileAttributeView userView = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
			String empId = "1234";
			String s="5939";
			long epoch = Instant.now().toEpochMilli();
			String trackingId = epoch + "-" + empId;
            userView.write(trackingId,ByteBuffer.wrap(s.getBytes()));
            System.out.println("\n\n trackingId: "+trackingId+"\n\n");
			
			// set response details
			res.put("status", "SUCCESS");
			res.put("message", "Uploaded the file");
			res.put("timeStamp", timeStamp);
			responseEntity = ResponseEntity.status(HttpStatus.OK).body(res);
			System.out.println("Successfully uploaded the excel file");
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
			res.put("status", "FAILED");
			res.put("message", "Could not Upload the file");
			responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
		return responseEntity;
	}
	
	public ResponseEntity<Object> downloadFile(String downloadFileName, String type) {
		ResponseEntity<Object> responseEntity = null;
		Map<String, String> response = new HashMap<>();
		try {
			//byte[] content = getFileInBytes(empId, trackingId, type); //get file in bytes
			byte[] content=null;
			if(type.equals("template")) {
	            String uploadDirectory = System.getProperty("user.dir")+"/template/"+downloadFileName+".xlsx";
				Path filePath = Paths.get(uploadDirectory);
				System.out.println("\nfilePath: "+filePath+"\n\n");
				content=Files.readAllBytes(filePath);
			}
			else if(type.equals("sow")) {
	            String uploadDirectory = System.getProperty("user.dir")+"/sow/"+downloadFileName+".docx";
				Path filePath = Paths.get(uploadDirectory);
				System.out.println("\nfilePath: "+filePath+"\n\n");
				content=Files.readAllBytes(filePath);
			}
			if(content==null) {
				response.put("status", "FAILED");
				response.put("message", "Requested file could not be downloaded. Please check the input provided.");
				responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}else {
				ByteArrayResource resource = new ByteArrayResource(content);
				HttpHeaders headers = new HttpHeaders();
				headers.add("content-disposition", "attachment; filename=\"" + downloadFileName + "\"");
				headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
				headers.add("content-length", content.length+"");
				responseEntity = ResponseEntity.ok().headers(headers).body(resource);
			}
			return responseEntity;
		} catch (Exception e) {
			response.put("status", "FAILED");
			response.put("message", "Requested file could not be downloaded");
			responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			return responseEntity;
		} 
	}
	
	public ResponseEntity<Employee> getEmployeeById(int employeeId) {
		Employee existingEmployee = employeesRepository.findById(employeeId).orElse(null);
		if (existingEmployee==null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok(existingEmployee);

	}

	public String deleteEmployee(int employeeId) {
		employeesRepository.deleteById(employeeId);
		return "Employee deleted with employeeID " + employeeId;
	}

	/*
	 * @SuppressWarnings("unchecked") public Map<String, Object>
	 * validateTemplate(InputStream inputStream) throws IOException { templateMap =
	 * new HashMap<String, Object>(); validateExcel(inputStream);
	 * if(templateMap.get("is_missing").equals("no")) { //log the SOW sheet details
	 * logSowSheet((LinkedHashMap<String, String>) templateMap.get("dataMap")); }
	 * templateMap.remove("dataMap"); inputStream.close(); return templateMap; }
	 * 
	 * public void validateExcel(InputStream inputStream) throws IOException{
	 * 
	 * Workbook workBook = null; Sheet sowSheet = null; Sheet deliverablesSheet =
	 * null; boolean isTemplateValid = false;
	 * 
	 * try { inputStream = new BufferedInputStream(inputStream); }catch(Exception e)
	 * { LOGGER.info("File not found"); } //check if the input file is excel
	 * workBook = checkFileIsExcel(inputStream,workBook); if(workBook == null) {
	 * LOGGER.info("Input file is not an excel"); } // read sheet if input file is
	 * valid else { sowSheet = validateSheet(sowSheet, workBook, "SOW");
	 * deliverablesSheet = validateSheet(deliverablesSheet, workBook,
	 * "Deliverables - Milestones"); }
	 * 
	 * //check if sheet is valid if(sowSheet != null && deliverablesSheet != null) {
	 * LOGGER.info("Template file is validated"); isTemplateValid = true; }
	 * 
	 * //check if input sheet is valid if(isTemplateValid && workBook!=null) {
	 * validateSowSheet(sowSheet,workBook); templateMap.put("is_file_correct",
	 * "yes"); workBook.close(); }else { templateMap.put("is_file_correct", "no");
	 * templateMap.put("is_missing", " "); templateMap.put("missing_keys"," "); }
	 * 
	 * }
	 * 
	 * public Workbook checkFileIsExcel(InputStream inputStream, Workbook workBook)
	 * { try { workBook = new XSSFWorkbook(inputStream); }catch(Exception e) {
	 * LOGGER.info("Incorrect type of file uploaded "); LOGGER.info("error: " +
	 * e.getMessage()); } return workBook; }
	 * 
	 * public Sheet validateSheet(Sheet sheet, Workbook workBook, String
	 * excelFileName) { try { sheet = workBook.getSheet(excelFileName);
	 * }catch(Exception e) { LOGGER.info("Incorrect excel"); } return sheet; }
	 * 
	 * public void validateSowSheet(Sheet sheet, Workbook workBook) throws
	 * IOException {
	 * 
	 * LOGGER.info("File processed for key validation"); Iterator<Row> iterator =
	 * sheet.iterator(); int startingPoint=0; ArrayList<String> sowKeysList = new
	 * ArrayList<>(); LinkedHashMap<String,String> dataMap= new
	 * LinkedHashMap<String,String>();
	 * 
	 * 
	 * // iterate the content in SOW sheet while (iterator.hasNext()) { Row row =
	 * iterator.next(); Iterator<Cell> cellIterator = row.cellIterator(); //get all
	 * cells in a row int cellNumber=0; String cellAData = null; String cellBData =
	 * null;
	 * 
	 * // iterate each cell in a row while (cellIterator.hasNext()) {
	 * 
	 * Cell cell = cellIterator.next(); cellNumber++; //set the starting point to
	 * read the sheet as SOW Type
	 * if(cell.toString().equalsIgnoreCase(BTExcelConfig.SOW_SHEET_STARTING)) {
	 * 
	 * startingPoint++; }
	 * 
	 * //get values of column one if(startingPoint>0&&cellNumber==1) { //format the
	 * string in column cellAData = cell.toString(); cellAData =
	 * cellAData.toUpperCase(); cellAData = cellAData.trim();
	 * sowKeysList.add(cellAData); } else if(startingPoint>0&&cellNumber==2) {
	 * cellBData=cell.toString(); } //populate the Map with column 1 & 2 details
	 * if(startingPoint>0) { dataMap.put(cellAData, cellBData);
	 * if(cellAData.equals("PROJECT NAME")) { templateMap.put("project_name",
	 * cellBData); } else if(cellAData.equals("FILE NAME")) {
	 * templateMap.put("file_name", cellBData); } }
	 * 
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * //remove all null keys with null values from map dataMap.remove("");
	 * dataMap.remove(null); //call method to validate the SOW sheet details
	 * List<String> missingKeys = validateSowKeys(sowKeysList); //check if the sheet
	 * contains any missing values checkMissingSowKeys(missingKeys, dataMap); }
	 * public List<String> validateSowKeys(ArrayList<String> sowKeysBeforeVal) {
	 * 
	 * LOGGER.info("Validating SOW Keys"); SOWDetailsInterface
	 * templateSOWSheetKeysModel = new BTTemplateKeysModel(); ArrayList<String>
	 * sowKeysList = templateSOWSheetKeysModel.getSowSheetKeys(); List<String>
	 * missingKeys = new ArrayList<String>();
	 * 
	 * //check if the uploaded SOW details are present in the sowKeysList for(int
	 * i=0; i<sowKeysList.size(); i++) {
	 * if(!sowKeysBeforeVal.contains(sowKeysList.get(i))) {
	 * missingKeys.add(sowKeysList.get(i)); } } return missingKeys; }
	 */
}
