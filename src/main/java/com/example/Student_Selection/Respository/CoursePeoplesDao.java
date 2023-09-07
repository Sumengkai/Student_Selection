package com.example.Student_Selection.Respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Student_Selection.Entity.CoursePeoples;

@Repository
public interface CoursePeoplesDao extends JpaRepository<CoursePeoples,Integer>{
	public List<CoursePeoples> findByCourseIdAndCoursePeople(String courseId,String coursePeople);
	public List<CoursePeoples> findByCourseIdIn(List<String> list);
}
