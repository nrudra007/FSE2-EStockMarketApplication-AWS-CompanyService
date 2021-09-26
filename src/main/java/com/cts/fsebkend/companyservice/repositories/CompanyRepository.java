package com.cts.fsebkend.companyservice.repositories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.cts.fsebkend.companyservice.models.Company;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Repository
public class CompanyRepository {

	Logger log = LoggerFactory.getLogger(CompanyRepository.class);

	private static final String REDIS_INDEX_KEY = "E_STOCK_MARKET_COMPANY_KEY";

	@Autowired
	RedisTemplate<String, Company> redisTemplate;

	public List<Company> findAll() {
		log.debug("*******within findAll..");
		List<Company> allCompanies = new ArrayList<Company>();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Gson gson = new Gson();
			Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_INDEX_KEY);
			for(Entry<Object, Object> entry : entries.entrySet()){
				log.debug("*******company entry: "+entry.getValue());
		        String json = gson.toJson(entry.getValue(), LinkedHashMap.class);
				log.debug("*******company JSON string: "+json);
				Company company = objectMapper.readValue(json, Company.class);
				allCompanies.add(company);
			}
		}catch(Exception ex) {
			log.error("An exception occurred >> "+ex.getMessage());
		}
		
		return allCompanies;
	}
	
	public Company save(Company newCompany) {
		log.debug("*******within save().. "+newCompany);
		redisTemplate.opsForHash().put(REDIS_INDEX_KEY, newCompany.getId(), newCompany);
		log.debug("*******company added successfully: "+newCompany);
		ObjectMapper objectMapper = new ObjectMapper();
		Gson gson = new Gson();
		String json = gson.toJson(redisTemplate.opsForHash().get(REDIS_INDEX_KEY, newCompany.getId()), LinkedHashMap.class);
		Company company = null;
		try {
			company = objectMapper.readValue(json, Company.class);
		} catch (JsonProcessingException ex) {
			log.error("An exception occurred >> "+ex.getMessage());
		}
		return company;
	}

	public void delete(Company deletedCompany) {
		redisTemplate.opsForHash().delete(REDIS_INDEX_KEY, deletedCompany.getId());
	}
}
