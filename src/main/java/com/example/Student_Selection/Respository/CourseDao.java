package com.example.Student_Selection.Respository;


import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Student_Selection.Entity.Course;
import com.example.Student_Selection.Entity.Student;

@Repository
public interface CourseDao extends JpaRepository<Course, String> {
	public List<Course> findAllByCourseidIn(Set<String> listSet);//���
	public List<Course> findAllByCourseidIn(List<String> listSet);//�R�� , �d�߽ҵ{
	public List<Course> findByCoursename(String name);
}
