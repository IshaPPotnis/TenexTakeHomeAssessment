package com.tenextakehome.repository;

import com.tenextakehome.model.ParserDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParserRepository extends CrudRepository<ParserDefinition, Long> {
}
