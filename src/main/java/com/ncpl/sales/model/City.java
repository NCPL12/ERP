package com.ncpl.sales.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "city")
public class City implements Comparable<City>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private Integer id;
	private String name;
	private String code;
	private String areaCode;

	
	@ManyToOne
	@JsonManagedReference
	private State state;
		
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String code) {
		this.code = areaCode;
	}
	@Override
	public String toString() {
		return "City [id=" + id + ", name=" + name + ", code=" + code + ", state=" + state + "]";
	}
	@Override
	public int compareTo(City city) {
		return this.name.compareToIgnoreCase(city.name);
	}

	
	
}
