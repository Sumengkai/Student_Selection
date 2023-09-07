package com.example.Student_Selection.Service.Face;

import java.util.Set;

import com.example.Student_Selection.Entity.Student;
import com.example.Student_Selection.Vo.CourseRes;

public interface CourseFace {
	//--�x�s�Ұ�...����T
public CourseRes addCourse(String courseid,String coursename,String week,String Start,String end,int point);
	//--�x�s�ǥ͸�T
public CourseRes addStudent(String studentid,String name ); 
	//--�ǥͿ��
public CourseRes addStudentCourse(String id,Set<String>list);
	//--�ǥͧR��
public CourseRes deleteCourse(String studentid, Set<String> list); 
	//--�ǥѾǸ��d��,��ܽҵ{�N�X�B�ҵ{�W��
public CourseRes searchStudentIdandCourseId(String studentid);
	//--�ҵ{�N�X�d��
public CourseRes searchCourseId(String courseid);
	//--�ҵ{�W�٬d��
public CourseRes searchByCourseName(String coursename);
//--�ǥͿ��
public CourseRes addStudentCourseAndLimitPeople(String id,Set<String>list);
}
