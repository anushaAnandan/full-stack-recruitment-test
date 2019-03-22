package com.rakuten.fullstackrecruitmenttest.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MainController {

	HashMap<String, JSONObject> empObjMap = null;
	String fileName = null;

	@SuppressWarnings("unchecked")
	private JSONObject validateRecord(JSONObject obj) {
		ArrayList<String> desig = new ArrayList<>();
		desig.add("Developer");
		desig.add("Senior Developer");
		desig.add("Manager");
		desig.add("Team Lead");
		desig.add("VP");
		desig.add("CEO");
		boolean hasError = false;
		String errorMsg = "";
		String employeeID = (String) obj.get("employee id");
		String name = (String) obj.get("name");
		String dept = (String) obj.get("department");
		String designation = (String) obj.get("designation");

		if (!employeeID.matches("^[a-zA-Z0-9_*]+$")) {
			hasError = true;
			errorMsg = errorMsg + "Employee ID has to be alphanumeric";
		}
		if (!name.matches("^[a-zA-Z]+$")) {
			hasError = true;
			errorMsg = ";name can contain only alphabets";
		}

		if (!dept.matches("^[a-zA-Z0-9_*]+$")) {
			hasError = true;
			errorMsg = errorMsg + ";department is alphanumeric and can contian only * and _";
		}

		if (!desig.contains(designation)) {
			hasError = true;
			errorMsg = errorMsg + ";designation is invalid";
		}

		obj.put("hasError", hasError);
		obj.put("ErrorMsg", errorMsg);
		return obj;
	}

	// parse uploaded csv file, validate and return json records
	@SuppressWarnings("unchecked")
	@PostMapping("/file")
	@CrossOrigin(origins = "*")
	public ResponseEntity<JSONObject> getFile(@RequestParam("file") MultipartFile file,
			@RequestParam("name") String filename) {
		fileName = filename;
		empObjMap = new HashMap<>();
		ArrayList<String> desig = new ArrayList<>();
		desig.add("Developer");
		desig.add("Senior Developer");
		desig.add("Manager");
		desig.add("Team Lead");
		desig.add("VP");
		desig.add("CEO");
		try {
			Reader reader = new InputStreamReader(file.getInputStream());
			@SuppressWarnings("resource")
			CSVParser csvParser = new CSVParser(reader,
					CSVFormat.DEFAULT
					.withHeader("employee id", "name", "department", "designation", "salary", "joining date")
					.withIgnoreHeaderCase().withTrim());
			for (CSVRecord record : csvParser) {

				JSONObject obj = new JSONObject();
				obj.put("employee id", record.get("employee id"));
				obj.put("name", record.get("name"));
				obj.put("designation", record.get("designation"));
				obj.put("department", record.get("department"));
				obj.put("salary", record.get("salary"));
				obj.put("joining date", record.get("joining date"));

				obj = validateRecord(obj);

				empObjMap.put((String) obj.get("employee id"), obj);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject resultObj = new JSONObject();
		if (empObjMap != null && fileName != null) {
			resultObj.put("FileName", fileName);
			JSONArray jsonRecords = new JSONArray();
			jsonRecords.addAll(empObjMap.values());
			resultObj.put("records", jsonRecords);
		}

		return new ResponseEntity<JSONObject>(resultObj, HttpStatus.OK);
	}

	// update modified records - validation handled in UI
	@SuppressWarnings("unchecked")
	@PutMapping("/employees")
	@CrossOrigin(origins = "*")
	public ResponseEntity<JSONObject> updateRecords(@RequestBody JSONObject obj) {
		if (empObjMap.containsKey(obj.get("employee id"))) {
			obj = validateRecord(obj);
			empObjMap.put((String) obj.get("employee id"), obj);
		}
		JSONObject resultObj = new JSONObject();
		if (empObjMap != null && fileName != null) {
			resultObj.put("FileName", fileName);
			JSONArray jsonRecords = new JSONArray();
			jsonRecords.addAll(empObjMap.values());
			resultObj.put("records", jsonRecords);
		}
		return new ResponseEntity<JSONObject>(resultObj, HttpStatus.OK);
	}

	// fetch data on reload or refresh
	@SuppressWarnings("unchecked")
	@GetMapping("/file")
	@CrossOrigin(origins = "*")
	public ResponseEntity<JSONObject> getRecords() {
		JSONObject resultObj = new JSONObject();
		if (empObjMap != null && fileName != null) {
			resultObj.put("FileName", fileName);
			JSONArray jsonRecords = new JSONArray();
			jsonRecords.addAll(empObjMap.values());
			resultObj.put("records", jsonRecords);
		}
		return new ResponseEntity<JSONObject>(resultObj, HttpStatus.OK);
	}

	// add records
	@SuppressWarnings("unchecked")
	@PostMapping("/employees")
	@CrossOrigin(origins = "*")
	public ResponseEntity<JSONObject> insertRecords(@RequestBody JSONObject obj) {
		obj = validateRecord(obj);
		empObjMap.put((String) obj.get("employee id"), obj);
		JSONObject resultObj = new JSONObject();
		if (empObjMap != null && fileName != null) {
			resultObj.put("FileName", fileName);
			JSONArray jsonRecords = new JSONArray();
			jsonRecords.addAll(empObjMap.values());
			resultObj.put("records", jsonRecords);
		}
		return new ResponseEntity<JSONObject>(resultObj, HttpStatus.OK);
	}

	// remove records deleted from UI
	@SuppressWarnings("unchecked")
	@DeleteMapping("/employees")
	@CrossOrigin(origins = "*")
	public ResponseEntity<JSONObject> deleteRecords(@RequestBody List<String> rowKeys) {
		for (String key : rowKeys) {
			if (empObjMap.containsKey(key))
				empObjMap.remove(key);
		}
		JSONObject resultObj = new JSONObject();
		if (empObjMap != null && fileName != null) {
			resultObj.put("FileName", fileName);
			JSONArray jsonRecords = new JSONArray();
			jsonRecords.addAll(empObjMap.values());
			resultObj.put("records", jsonRecords);
		}

		return new ResponseEntity<JSONObject>(resultObj, HttpStatus.OK);
	}
}
