package com.appleyk.result;


/**
 * 
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年5月10日-下午3:44:37
 * @param <T>
 */
public enum ResponseMessage {

	/**
	 * 成功
	 */
	OK(200,"成功"),
	
	/**
	 * 错误的请求
	 */
	BAD_REQUEST(400,"错误的请求"),
	
	/**
	 * 错误的请求
	 */
	NOTNULL_ID(400,"请求ID不能为空"),
	
	/**
	 * 错误的请求
	 */
	NOTNULL_NAME(400,"名称不能为空"),
	
	
	/**
	 * 内部错误
	 */
	INTERNAL_SERVER_ERROR(500, "内部错误"),
	
	
	/**
	 * 操作太頻繁！
	 */
	FREQUENT_FEEDBACK(515, "操作太频繁，请五分钟后再提交！");
	

	
	private final int status;
	
	private final String message;
	
	ResponseMessage(int status, String message){
		this.status = status;
		this.message = message;
	}
	
	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
}
