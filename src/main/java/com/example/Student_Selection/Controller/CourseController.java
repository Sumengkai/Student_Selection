package com.example.Student_Selection.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Student_Selection.Service.Face.CourseFace;
import com.example.Student_Selection.Vo.CourseReq;
import com.example.Student_Selection.Vo.CourseRes;
@CrossOrigin
@RestController
public class CourseController {
	@Autowired
	private CourseFace courseFace;

	// -------------------------------------1.�s�W�ҵ{
	@PostMapping(value = "/api/Course1")
	public CourseRes addCourse(@RequestBody CourseReq req) {
		CourseRes res = courseFace.addCourse(req.getCourse_id(), req.getCourse_name(), req.getWeek(),
				req.getStart_time(), req.getEnd_time(), req.getCourse_point());
		return res;
	}

	// -------------------------------------2.�s�W�ǥ�
	@PostMapping(value = "/api/Course2")
	public CourseRes addStudents(@RequestBody CourseReq req) {
		CourseRes res = courseFace.addStudent(req.getStudentid(), req.getStudentname());
		return res;
	}

	// -------------------------------------3.�ǥͿ��
	@PostMapping(value = "/api/Course3")
	public CourseRes addStudentsCourse(@RequestBody CourseReq req) {
		CourseRes res = courseFace.addStudentCourse(req.getStudentid(), req.getListset());
		return res;

	}

	// -------------------------------------4.�h��
	@PostMapping(value = "/api/Course4")
	public CourseRes deleteCourse(@RequestBody CourseReq req) {
		CourseRes res = courseFace.deleteCourse(req.getStudentid(), req.getListset());
		return res;
	}

	// -------------------------------------5.�H��W���ҵ{�d��,��ܾǥ͸�T�H�νҵ{��T
	@PostMapping(value = "/api/Course5")
	public CourseRes searchStudentIdandCourseId(@RequestBody CourseReq req) {
		CourseRes res = courseFace.searchStudentIdandCourseId(req.getStudentid());
		return res;
	}

	// -------------------------------------6.�ҵ{�N�X�d��
	@PostMapping(value = "/api/Course6")
	public CourseRes searchCourseId(@RequestBody CourseReq req) {
		CourseRes res = courseFace.searchCourseId(req.getCourse_id());
		return res;
	}

	// -------------------------------------7.�ҵ{�W�٬d��
	@PostMapping(value = "/api/Course7")
	public CourseRes srarchCourseByName(@RequestBody CourseReq req) {
		CourseRes res = courseFace.searchByCourseName(req.getCourse_name());
		return res;
	}
	// -------------------------------------8.����H��<�|�R>
	@PostMapping(value = "/api/Course8")
	public CourseRes addStudentCourseAndLimitPeople(@RequestBody CourseReq req) {
		CourseRes res = courseFace.addStudentCourseAndLimitPeople(req.getStudentid(), req.getListset());
		return res;
	}
}
