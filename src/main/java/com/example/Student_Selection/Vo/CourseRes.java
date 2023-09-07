package com.example.Student_Selection.Vo;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;

import com.example.Student_Selection.Entity.Course;
import com.example.Student_Selection.Entity.Student;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseRes {
	@JsonProperty("Course_info")
	private String course_id;
	private String course_name;
	private String week;
	private String start_time;
	private String end_time;
	private Integer course_point;
	private String code;
	private String message;
	@JsonProperty("Student_info")
	private Student student;

	private List<Student> studentlist;// api 5
	private List<Course> courselist; // api 5

	// -------------------------
	private Course course;

	public String getCourse_id() {
		return course_id;
	}

	// -------------------------
	public CourseRes() {
	}
	public CourseRes(Student student, String message) {
	this.student = student;
	this.message =message;
	}
	public CourseRes(Course course, String message) {
		this.course = course;
		this.message =message;
		}

	public List<Course> getCourselist() {
		return courselist;
	}

	public List<Student> getStudentlist() {
		return studentlist;
	}

	public void setStudentlist(List<Student> studentlist) {
		this.studentlist = studentlist;
	}

	public void setCourselist(List<Course> courselist) {
		this.courselist = courselist;
	}

	// -------------------------
	public void setCourse_id(String course_id) {
		this.course_id = course_id;
	}

	public String getCourse_name() {
		return course_name;
	}

	public void setCourse_name(String course_name) {
		this.course_name = course_name;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

	public Integer getCourse_point() {
		return course_point;
	}

	public void setCourse_point(Integer course_point) {
		this.course_point = course_point;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	

}
