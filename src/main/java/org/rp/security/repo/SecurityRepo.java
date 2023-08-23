package org.rp.security.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.rp.security.repo.dao.SecurityEntity;

import java.util.List;


public interface SecurityRepo extends JpaRepository<SecurityEntity,Integer> {

    List<SecurityEntity> findByName(String name);
    List<SecurityEntity> findBySymbol(String symbol);
}
