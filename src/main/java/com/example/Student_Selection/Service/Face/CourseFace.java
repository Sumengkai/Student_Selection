package com.example.Student_Selection.Service.Face;

import java.util.Set;

import com.example.Student_Selection.Entity.Student;
import com.example.Student_Selection.Vo.CourseRes;

public interface CourseFace {
	//--儲存課堂...等資訊
public CourseRes addCourse(String courseid,String coursename,String week,String Start,String end,int point);
	//--儲存學生資訊
public CourseRes addStudent(String studentid,String name ); 
	//--學生選課
public CourseRes addStudentCourse(String id,Set<String>list);
	//--學生刪課
public CourseRes deleteCourse(String studentid, Set<String> list); 
	//--藉由學號查詢,顯示課程代碼、課程名稱
public CourseRes searchStudentIdandCourseId(String studentid);
	//--課程代碼查詢
public CourseRes searchCourseId(String courseid);
	//--課程名稱查詢
public CourseRes searchByCourseName(String coursename);
//--學生選課
public CourseRes addStudentCourseAndLimitPeople(String id,Set<String>list);
}
