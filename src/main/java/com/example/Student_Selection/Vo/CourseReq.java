package com.example.Student_Selection.Vo;

import java.util.Set;

public class CourseReq {
	private String course_id; //�Ұ�id
	private String course_name; //�Ұ�W��
	private String week; //§���X
	private String start_time; //�}�l�ɶ� ��time
	private String end_time; //�����ɶ� ��time
	private int course_point;//�Ǥ���
	private Set<String> listset;//��ҥ� API 3
	//------------------------
	private String studentid; //�ǥ͸�T
	private String studentname; //�ǥͦW�l
	
	public String getCourse_id() {
		return course_id;
	}
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
	public int getCourse_point() {
		return course_point;
	}
	public void setCourse_point(int course_point) {
		this.course_point = course_point;
	}
	//----
	public String getStudentid() {
		return studentid;
	}
	public void setStudentid(String studentid) {
		this.studentid = studentid;
	}
	public String getStudentname() {
		return studentname;
	}
	public void setStudentname(String studentname) {
		this.studentname = studentname;
	}
	public Set<String> getListset() {
		return listset;
	}
	public void setListset(Set<String> listset) {
		this.listset = listset;
	}
	
	
	
}
