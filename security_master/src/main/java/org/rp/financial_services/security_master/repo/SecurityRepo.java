package org.rp.financial_services.security_master.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.rp.financial_services.security_master.repo.dao.SecurityEntity;

import java.util.List;


public interface SecurityRepo extends JpaRepository<SecurityEntity,Integer> {

    List<SecurityEntity> findByName(String name);
    List<SecurityEntity> findBySymbol(String symbol);
}
