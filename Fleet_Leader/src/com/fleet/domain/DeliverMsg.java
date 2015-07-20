package com.fleet.domain;

public class DeliverMsg {
	private String message_type;
	private String src_tag;
	private String src_id;
	private String attr;
	private String location;
	private String content;
	
	public DeliverMsg () {
		this.message_type = "";
		this.src_tag = "";
		this.src_id = "";
		this.attr = "";
		this.location = "";
		this.content = "";
	}
	
	public DeliverMsg(String message_type, String src_tag, String src_id,
			String attr, String location, String content) {
		super();
		this.message_type = message_type;
		this.src_tag = src_tag;
		this.src_id = src_id;
		this.attr = attr;
		this.location = location;
		this.content = content;
	}
	public String getMessage_type() {
		return message_type;
	}
	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}
	public String getSrc_tag() {
		return src_tag;
	}
	public void setSrc_tag(String src_tag) {
		this.src_tag = src_tag;
	}
	public String getSrc_id() {
		return src_id;
	}
	public void setSrc_id(String src_id) {
		this.src_id = src_id;
	}
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
