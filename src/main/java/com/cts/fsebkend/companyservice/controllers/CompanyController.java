package com.cts.fsebkend.companyservice.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cts.fsebkend.companyservice.models.Company;
import com.cts.fsebkend.companyservice.response.AddCompanyResponse;
import com.cts.fsebkend.companyservice.services.CompanyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@RestController
@RequestMapping("/api/v1.0/market/company")
@Api(value="company-service", description="Company Service APIs for E Stock Market")
public class CompanyController {

	Logger log = LoggerFactory.getLogger(CompanyController.class);
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private StockServiceProxy stockServiceProxy;
	
	@ApiOperation(value = "View the list of all available companies registered in the E Stock Market portal")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    }
    )
	@GetMapping("/getall")
	public ResponseEntity<?> getAllCompanies() {
		log.debug("within getAllCompanies..");
		try {
			List<Company> allCompanies = companyService.getAll();
			if(null == allCompanies || allCompanies.isEmpty()) {
				log.debug("No Company exists in the portal!!");
				return new ResponseEntity<>(allCompanies, HttpStatus.NO_CONTENT);
			}
			List<Company> finalCompanyList = new ArrayList<>();
			for(Company company : allCompanies) {
				double latestStockPrice = stockServiceProxy.getLatestStockPriceByCompanyCode(company.getCompanyCode());
				log.debug("latestStockPrice for company code: "+company.getCompanyCode()+" is: "+latestStockPrice);
				company.setLatestStockPrice(latestStockPrice);
				finalCompanyList.add(company);
			}
			return new ResponseEntity<>(finalCompanyList, HttpStatus.OK);
		}catch(Exception ex) {
			log.error("An exception occurred while fetching all the company details >> "+ex.getMessage());
			ResponseEntity<String> resp = new ResponseEntity<>("An exception occurred while fetching all the company details!!", 
					HttpStatus.BAD_GATEWAY);
			return resp;
		}
	}
	
	@ApiOperation(value = "Register a brand new company to E Stock Market portal")
	@PostMapping("/register")
	public ResponseEntity<?> registerCompany(@RequestBody Company company) {
		log.debug("within registerCompany..");
		AddCompanyResponse resp = null;
		//input validation
		validateRegisterCompanyInputs(company);
		try {
			Company registeredCompany = companyService.register(company);
			resp = new AddCompanyResponse();
			resp.setCompany(registeredCompany);
			return new ResponseEntity<>(resp, HttpStatus.CREATED);
		}catch(Exception ex) {
			resp = new AddCompanyResponse();
			log.error("An error occurred while registering company in E-Stock Market Portal >> "+ex.getMessage());
			resp.setErrorMsg("An error occurred while registering company in E-Stock Market Portal!!");
			return new ResponseEntity<>(resp, HttpStatus.BAD_GATEWAY);
		}
	}
	
	@ApiOperation(value = "View the company details using company code")
	@GetMapping("/info/{companycode}")
	public ResponseEntity<?> getCompanyByCompanyCode(@PathVariable("companycode") String companyCode) {
		log.debug("within getCompanyByCompanyCode..");
		try {
			Company company = companyService.findByCompanyCode(companyCode);
			if(company == null) {
				log.debug("Company having companyCode: {} not exists!!", companyCode);
				return new ResponseEntity<>(company, HttpStatus.NO_CONTENT);
			}
			double latestStockPrice = stockServiceProxy.getLatestStockPriceByCompanyCode(companyCode);
			log.debug("latestStockPrice for company code: "+companyCode+" is: "+latestStockPrice);
			company.setLatestStockPrice(latestStockPrice);
			return new ResponseEntity<>(company, HttpStatus.OK);
		}catch(Exception ex) {
			log.error("An exception occurred while fetching the company details >> "+ex.getMessage());
			ResponseEntity<String> resp = new ResponseEntity<>("An exception occurred while fetching the company details!!", 
					HttpStatus.BAD_GATEWAY);
			return resp;
		}
	}
	
	@ApiOperation(value = "View the company details using company name")
	@GetMapping("/name/{companycode}")
	public String getCompanyNameByCompanyCode(@PathVariable("companycode") String companyCode) {
		log.debug("within getCompanyNameByCompanyCode..");
		String companyName = null;
		Company company = companyService.findByCompanyCode(companyCode);
		if(null != company) {
			companyName = company.getCompanyName();
		}
		return companyName;
	}
	
	@ApiOperation(value = "Delete a company using company code from E Stock Market portal")
	@GetMapping("/delete/{companycode}")
	public ResponseEntity<String> deleteCompanyByCompanyCode(@PathVariable("companycode") String companyCode) {
		log.debug("within deleteCompanyByCompanyCode..");
		ResponseEntity<String> resp = null;
		try {
			companyService.deleteCompanyByCompanyCode(companyCode);
			boolean isStocksDeleted = stockServiceProxy.deleteStocksByCompanyCode(companyCode);
			if(!isStocksDeleted) {
				log.error("An exception occurred while deleting the stocks by companyCode!!");
				resp = new ResponseEntity<>("Company having mentioned company code successfully deleted. "
						+ "However, an exception occurred while deleting the stocks by companyCode!!", HttpStatus.OK);
			}else {
				resp = new ResponseEntity<>("Company having mentioned company code along with all its stocks "
						+ "have successfully deleted..", HttpStatus.OK);
			}
		}catch(Exception ex) {
			log.error("An exception occurred while deleting the company by companyCode >> "+ex.getMessage());
			resp = new ResponseEntity<>("Unable to delete company having mentioned company code!!", HttpStatus.BAD_GATEWAY);
		}
		return resp;
	}
	
	private void validateRegisterCompanyInputs(Company company) {

		if(company == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company object cannot be null!!");
		}
		if(StringUtils.isBlank(company.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Id cannot be null or empty!!");
		}
		if(StringUtils.isBlank(company.getCompanyCode())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Code cannot be null or empty!!");
		}
		if(!companyService.isUniqueCompCode(company.getCompanyCode())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Code must be unique!!");
		}
		if(StringUtils.isBlank(company.getCompanyName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Name cannot be null or empty!!");
		}
		if(StringUtils.isBlank(company.getCompanyCeoName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company CEO Name cannot be null or empty!!");
		}
		if(StringUtils.isBlank(company.getCompanyStockExchange())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Stock Exchange cannot be null or empty!!");
		}
		if(StringUtils.isBlank(company.getCompanyWebsite())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Website cannot be null or empty!!");
		}
		if(company.getCompanyTurnover() == 0 || company.getCompanyTurnover() <= 100000000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company Turnover must be greater than 10Cr!!");
		}
	}
}
