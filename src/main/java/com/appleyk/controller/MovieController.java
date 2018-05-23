package com.appleyk.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appleyk.node.Movie;
import com.appleyk.repository.MovieRepository;
import com.appleyk.result.ResponseMessage;
import com.appleyk.result.ResponseResult;
import com.appleyk.result.ResultData;

@RestController
@RequestMapping("/rest/appleyk/movie") //restful风格的api接口  
public class MovieController {
   
	@Autowired  
    MovieRepository movieRepository;  
      
	/**
	 * 根据电影名查询电影实体
	 * @param title
	 * @return
	 */
    @RequestMapping("/get")  
    public List<Movie> getMovies(@RequestParam(value="title") String title){             
    	return movieRepository.findByTitle(title);
    }  
      
    /**
     * 创建一个电影节点
     * @param genre
     * @return
     */
    @PostMapping("/save")  
    public ResponseResult saveMovie(@RequestBody Movie movie){      	
    	movieRepository.save(movie);
    	return new ResponseResult(ResponseMessage.OK);
    }  
	
    
    @RequestMapping("/query")  
    public ResponseResult queryMovieTiles(){      	
    	List<String> movieTiles = movieRepository.getMovieTiles();
    	ResultData<String> result = new ResultData<String>(ResponseMessage.OK, movieTiles);
    	return new ResponseResult(result);
    } 
}
