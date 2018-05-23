package com.appleyk.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import com.appleyk.node.Person;

public interface PersonRepository extends GraphRepository<Person>{
	 List<Person> findByName(@Param("name") String name); 	
}
