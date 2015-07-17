package com.fleet.domain;

import com.baidu.mapapi.model.LatLng;

public class LocationOfCar {
	private String name;
	private String time;
	private LatLng location;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}
	
	public LocationOfCar(String name, String time, LatLng location) {
		super();
		this.name = name;
		this.time = time;
		this.location = location;
	}

	public LocationOfCar(){
		name = "";
		time = "";
		location = null;
	}
	
}
