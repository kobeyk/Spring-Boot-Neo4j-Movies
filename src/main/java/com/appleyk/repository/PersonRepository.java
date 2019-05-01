package com.appleyk.repository;

import com.appleyk.node.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends Neo4jRepository<Person, Long>{
	 List<Person> findByName(@Param("name") String name); 	
}
