package com.ut.ecommerce.commondataservice.dao.sql.info;

import com.ut.ecommerce.commondataservice.entity.sql.info.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContactInfoRepository extends JpaRepository<ContactInfo, Integer> {
}
