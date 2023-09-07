package com.example.Student_Selection.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "student")

public class Student {
	@Id
	@Column(name = "student_id")
	private String studentid;
	@Column(name = "name")
	private String name;
	@Column(name = "course_id")
	private String courseid;
	@Column(name = "course_point")
	private int coursepoint;

	public Student() {
	}
	public Student(String studentid,String name,int coursepoint) {
		this.studentid=studentid;
		this.name=name;
		this.coursepoint=coursepoint;
	}
	public Student(String studentid,String name,String courseid,int coursepoint) {
		this.studentid=studentid;
		this.name=name;
		this.courseid=courseid;
		this.coursepoint=coursepoint;
	}


	public String getStudent_id() {
		return studentid;
	}

	public void setStudent_id(String student_id) {
		this.studentid = student_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCourse_id() {
		return courseid;
	}

	public void setCourse_id(String course_id) {
		this.courseid = course_id;
	}

	public int getCourse_point() {
		return coursepoint;
	}

	public void setCourse_point(int course_point) {
		this.coursepoint = course_point;
	}
	

}
