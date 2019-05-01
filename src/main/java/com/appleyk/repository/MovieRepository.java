package com.appleyk.repository;

import com.appleyk.node.Movie;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends Neo4jRepository<Movie, Long>{

	 List<Movie> findByTitle(@Param("title") String title);
	 @Query("match(n:Person)-[:actedin]->(m:Movie) where n.name='章子怡' return m.title")
	 List<String> getMovieTiles();
}
