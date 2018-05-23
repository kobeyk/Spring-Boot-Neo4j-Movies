package com.appleyk.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import com.appleyk.node.Genre;

public interface GenreRepository extends GraphRepository<Genre>{
	
	 @Query("MATCH (n:Genre) where n.name='日本AV' return n")
	 List<Genre> getGenres(@Param("name") String name);
	
	 List<Genre> findByName(@Param("name") String name); 
}
