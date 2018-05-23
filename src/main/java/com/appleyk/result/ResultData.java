/**
 * 
 */
package com.appleyk.result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年5月10日-下午3:44:37
 * @param <T>
 */
public class ResultData<T> {
	
	private ResponseMessage state;
	private Object data;
	private List<T> dataList;
	

	public ResultData(ResponseMessage state) {

		this.state = state;
	}

	public ResultData(ResponseMessage state, Object data) {
		this.data = data;
		this.state = state;
	}
	
	public ResultData(ResponseMessage state, long data, boolean isId){
		this.state = state;
		Map<String, Long> map = new HashMap<String, Long>();
		map.put("id", data);
		this.data = map;
	}
	
	public ResultData(ResponseMessage state, long id){
		this.state = state;
		Map<String, Long> map = new HashMap<String, Long>();
		map.put("id", id);
		this.data = map;
	}
	
	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}
	
	public ResponseMessage getState(){
		return state;
	}
	
	public List<T> getDataList(){
		return dataList;
	}

}
