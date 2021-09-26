package com.cts.fsebkend.companyservice.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.fsebkend.companyservice.models.Company;
import com.cts.fsebkend.companyservice.repositories.CompanyRepository;

@Service
public class CompanyService {
	
	@Autowired
	private CompanyRepository companyRepository;

	public Company register(Company company) {
		return companyRepository.save(company);
	}

	public Company findByCompanyCode(String companyCode) {
		Company selectedCompany = null;
		List<Company> companyList = getAll();
		for(Company company : companyList) {
			if(company != null && company.getCompanyCode().equalsIgnoreCase(companyCode)) {
				selectedCompany = company;
				break;
			}
		}
		return selectedCompany;
	}

	public List<Company> getAll() {
		List<Company> compList = companyRepository.findAll();
		Collections.sort(compList, new SortByCompCode());
		return compList;
	}

	public void deleteCompanyByCompanyCode(String companyCode) {
		Company deletedCompany = null;
		List<Company> companyList = getAll();
		for(Company company : companyList) {
			if(company != null && company.getCompanyCode().equalsIgnoreCase(companyCode)) {
				deletedCompany = company;
				break;
			}
		}
		if(null != deletedCompany)
			companyRepository.delete(deletedCompany);
	}

	public boolean isUniqueCompCode(String companyCode) {
		boolean isUniqueCompCode = false;
		Company selectedCompany = findByCompanyCode(companyCode);
		if(selectedCompany == null) {
			isUniqueCompCode = true;
		}
		return isUniqueCompCode;
	}
}

class SortByCompCode implements Comparator<Company> {

	@Override
	public int compare(Company a, Company b) {
		return a.getCompanyCode().compareTo(b.getCompanyCode());
	}
}
