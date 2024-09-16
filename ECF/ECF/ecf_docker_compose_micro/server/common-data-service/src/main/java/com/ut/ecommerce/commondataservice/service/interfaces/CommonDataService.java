package com.ut.ecommerce.commondataservice.service.interfaces;

import com.ut.ecommerce.commondataservice.dto.ProductInfoDTO;
import com.ut.ecommerce.commondataservice.entity.sql.info.ProductInfo;
import com.ut.ecommerce.commondataservice.model.FilterAttributesResponse;
import com.ut.ecommerce.commondataservice.model.HomeTabsDataResponse;
import com.ut.ecommerce.commondataservice.model.MainScreenResponse;
import com.ut.ecommerce.commondataservice.model.SearchSuggestionResponse;

import java.util.HashMap;

public interface CommonDataService {

    MainScreenResponse getHomeScreenData(String apiName);

    FilterAttributesResponse getFilterAttributesByProducts(String queryParams);

    ProductInfoDTO getProductsByCategories(String queryParams);

    HashMap<Integer, ProductInfo> getProductsById(String queryParams);

    HomeTabsDataResponse getBrandsAndApparelsByGender(String apiName);

    SearchSuggestionResponse getSearchSuggestionList();
}

