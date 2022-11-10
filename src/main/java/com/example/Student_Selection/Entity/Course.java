package com.example.Student_Selection.Entity;

import java.sql.Time;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "course")
public class Course {
	@Id
	@Column(name = "course_id")
	private String courseid;
	@Column(name = "course_name")
	private String coursename;
	@Column(name = "week")
	private String week;
	@Column(name = "start_time")
	private Time starttime;
	@Column(name = "end_time")
	private Time endtime;
	@Column(name = "course_point")
	private int coursepoint;

	public Course(String courseid,String coursename,String week,Time starttime, Time endtime,int coursepoint) {
			this.courseid=courseid;
			this.coursename=coursename;
			this.week=week;
			this.starttime=starttime;
			this.endtime=endtime;
			this.coursepoint=coursepoint;
	}
	public Course() {
	}

	public String getCourse_id() {
		return courseid;
	}

	public void setCourse_id(String course_id) {
		this.courseid = course_id;
	}

	public String getCourse_name() {
		return coursename;
	}

	public void setCourse_name(String course_name) {
		this.coursename = course_name;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public Time getStart_time() {
		return starttime;
	}

	public void setStart_time(Time starttime) {
		this.starttime = starttime;
	}

	public Time getEnd_time() {
		return endtime;
	}

	public void setEnd_time(Time endtime) {
		this.endtime = endtime;
	}

	public int getCourse_point() {
		return coursepoint;
	}

	public void setCourse_point(int course_point) {
		this.coursepoint = course_point;
	}

}
