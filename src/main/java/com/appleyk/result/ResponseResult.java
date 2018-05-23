package com.appleyk.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年5月10日-下午3:44:37
 * @param <T>
 */
public class ResponseResult implements Serializable {

	private static final long serialVersionUID = 2719931935414658118L;

	private final Integer status;

	private final String message;

	@JsonInclude(value = Include.NON_NULL)
	private final Object data;

	@JsonInclude(value = Include.NON_EMPTY)
	private final String[] exceptions;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private final Date timestamp;

	public ResponseResult(Integer status, String message) {

		super();
		this.status = status;
		this.message = message;
		this.data = null;
		this.timestamp = new Date();
		this.exceptions = null;

	}

	/**
	 * 处理返回的resultData
	 * @param result
	 */
	public ResponseResult(ResultData<?> result) {

		super();

		this.status = result.getState().getStatus();
		this.message = result.getState().getMessage();
		this.data = result.getData();
		this.timestamp = new Date();
		this.exceptions = null;
	}

	public ResponseResult(ResponseMessage rm){
		super();
		
		this.status = rm.getStatus();
		this.message = rm.getMessage();
		this.data = null;
		this.timestamp = new Date();
		this.exceptions = null;
	}
	
	//UnauthorizedMessage 为自定义的消息---未认证
//	public ResponseResult(UnauthorizedMessage um){
//		super();
//		
//		this.status = um.getStatus();
//		this.message = um.getMessage();
//		this.data = null;
//		this.timestamp = new Date();
//		this.exceptions = null;
//	}
	
	public ResponseResult(Integer status, String message, Object data) {

		super();

		this.status = status;

		this.message = message;

		this.data = data;
		this.timestamp = new Date();
		this.exceptions = null;

	}
	
	/**
	 * 验证失败返回结果
	 * @param responseMessage
	 * @param result
	 */
	public ResponseResult(ResponseMessage responseMessage, BindingResult result) {

		super();
		
		/**
		 * 解析BindingResult，放入map
		 */
		List<Map<String, String>> errors = new ArrayList<Map<String, String>>();
		
		for(int i=0;i<result.getAllErrors().size();i++){
			Map<String, String> map = new HashMap<String, String>();
			DefaultMessageSourceResolvable dm = (DefaultMessageSourceResolvable) result.getAllErrors().get(i).getArguments()[0];
			map.put("field", dm.getDefaultMessage());
			map.put("message", result.getAllErrors().get(i).getDefaultMessage());
			errors.add(map);
		}
		
		this.status = responseMessage.getStatus();

		this.message = responseMessage.getMessage();

		this.data = errors;
		this.timestamp = new Date();
		this.exceptions = null;

	}

	public ResponseResult(Integer status, String message, String key, Object value) {

		super();

		this.status = status;

		this.message = message;

		Map<String, Object> map = new HashMap<String, Object>();

		if (key == null || ("").equals(key)) {
			map.put("key", value);
		} else {
			map.put(key, value);
		}

		this.data = map;

		this.timestamp = new Date();
		this.exceptions = null;

	}

	public ResponseResult(Integer status, Throwable ex) {

		super();

		this.status = status;
		this.message = ex.getMessage();
		this.data = null;
		StackTraceElement[] stackTeanceElement = ex.getStackTrace();
		this.exceptions = new String[stackTeanceElement.length];
		for (int i = 0; i < stackTeanceElement.length; i++) {
			this.exceptions[i] = stackTeanceElement[i].toString();
		}
		this.timestamp = new Date();
	}

	public ResponseResult(Integer status, String message, Throwable ex) {

		super();

		this.status = status;

		this.message = message;

		this.data = null;

		StackTraceElement[] stackTeanceElement = ex.getStackTrace();
		this.exceptions = new String[stackTeanceElement.length];
		for (int i = 0; i < stackTeanceElement.length; i++) {
			this.exceptions[i] = stackTeanceElement[i].toString();
		}

		this.timestamp = new Date();

	}

	public Integer getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public Object getData() {
		return data;
	}

	public String[] getExceptions() {
		return exceptions;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}
