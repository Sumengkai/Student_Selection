package com.example.Student_Selection.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name = "course_people")
public class CoursePeoples {
	@Id
	@Column(name = "serial_number")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer serialNumber;
	@Column(name = "course_id")
	private String courseId;
	@Column(name = "course_people")
	private String coursePeople;

	public Integer getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getCoursePeople() {
		return coursePeople;
	}

	public void setCoursePeople(String coursePeople) {
		this.coursePeople = coursePeople;
	}

}
