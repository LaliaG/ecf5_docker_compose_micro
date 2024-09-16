package com.ut.ecommerce.commondataservice.dao.sql.info;

import com.ut.ecommerce.commondataservice.entity.sql.info.AddressInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressInfoRepository extends JpaRepository<AddressInfo, Integer> {

}
