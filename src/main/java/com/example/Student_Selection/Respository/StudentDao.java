package com.example.Student_Selection.Respository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Student_Selection.Entity.Student;

@Repository
public interface StudentDao extends JpaRepository<Student,String> {

public List<Student> findAllByCourseidIn(List<String> listset);

}
