package com.cts.fsebkend.companyservice.controllers;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Configuration
@FeignClient(name="stock-service", url = "${LOAD_BALANCER_URL}")
public interface StockServiceProxy {

	@GetMapping("/api/v1.0/market/stock/get/latest-stock-price/{companycode}")
	public Double getLatestStockPriceByCompanyCode(@PathVariable("companycode") String companyCode);
	
	@GetMapping("/api/v1.0/market/stock/delete/{companycode}")
	public Boolean deleteStocksByCompanyCode(@PathVariable("companycode") String companyCode);
}
