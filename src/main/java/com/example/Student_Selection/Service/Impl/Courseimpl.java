package com.example.Student_Selection.Service.Impl;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.Student_Selection.Entity.Course;
import com.example.Student_Selection.Entity.Student;
import com.example.Student_Selection.Respository.CourseDao;
import com.example.Student_Selection.Respository.StudentDao;
import com.example.Student_Selection.Service.Face.CourseFace;
import com.example.Student_Selection.Vo.CourseRes;

@Service
public class Courseimpl implements CourseFace {
	@Autowired
	private CourseDao courseDao;
	@Autowired
	private StudentDao studentDao;

//--------------------------------------------------------------------------�p����k
	private boolean checkWeekAndPoint(String week, int point) {
		List<String> list = new ArrayList<>(List.of("�@", "�G", "�T", "�|", "��"));
		// ---- foreach
//		boolean hasWeek = false;
//		for(var x:list) {
//			if (x.equals(week)&&point > 0 && point <= 3) {
//				hasWeek = true;
//				break;
//			}
//		}
		// ---- Lambda
		if (list.stream().anyMatch(item -> item.equals(week))) {
			if (point > 0 && point <= 3) {
				return true;
			}
		}
		return false;
	}

	// ---------------------
	// �T�{�}�C�O�_��
	private boolean checkList(Set<String> listset) {
		return listset.isEmpty();
	}

//----------------------------------------------------------------------------�p����k
	// ==============================================================================1.�w��ҵ{�s�W+�ק�
	@Override
	public CourseRes addCourse(String courseid, String name, String week, String start, String end, int point) {
		Optional<Course> courseop = courseDao.findById(courseid);
		Course addandUpdatecoursec = new Course();
		CourseRes res = new CourseRes();
		String chickStartandEnd = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]";
		boolean a = start.matches(chickStartandEnd);
		boolean b = end.matches(chickStartandEnd);
		boolean c = (new Courseimpl().checkWeekAndPoint(week, point));
		start += ":00";
		end += ":00";
		if (!StringUtils.hasText(courseid) || !StringUtils.hasText(name) || start.equals(end)) {
			res.setMessage("����ŪũΤ��༲�ɶ�");
			return res;
		}
		if (!a || !b || !c) {
			res.setMessage("�ɶ��榡ex: 01:00 or week�u�� �@ ~ �� or �Ǥ����o�C��s,�j��T��null");
			return res;
		}
		Time TimeStart = Time.valueOf(start);
		Time TimeEnd = Time.valueOf(end);
		if (TimeEnd.before(TimeStart)) {
			res.setMessage("�����ɶ����i�p��}�l�ɶ�");
			return res;
		}
		if (!courseop.isPresent()) {
			addandUpdatecoursec.setCourse_id(courseid);
			addandUpdatecoursec.setCourse_name(name);
			addandUpdatecoursec.setWeek(week);
			addandUpdatecoursec.setStart_time(TimeStart);
			addandUpdatecoursec.setEnd_time(TimeEnd);
			addandUpdatecoursec.setCourse_point(point);
			courseDao.save(addandUpdatecoursec);
			res.setMessage("�s�W���\");
			return res;
		}
		addandUpdatecoursec = courseop.get();
		addandUpdatecoursec.setCourse_name(name);
		addandUpdatecoursec.setWeek(week);
		addandUpdatecoursec.setStart_time(TimeStart);
		addandUpdatecoursec.setEnd_time(TimeEnd);
		addandUpdatecoursec.setCourse_point(point);
		courseDao.save(addandUpdatecoursec);
		res.setMessage("�ק令�\"); // �ٻݤ@�ӧ�s�ǥ;Ǥ����{��
		return res;
	}

	// ==============================================================================2.�w��ǥͷs�W+�ק�
	@Override
	public CourseRes addStudent(String studentid, String name) {
		Student students = new Student();
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(studentid) || !StringUtils.hasText(name)) {
			res.setMessage("�����");
			return res;
		}
		Optional<Student> courseop = studentDao.findById(studentid);
		if (courseop.isPresent()) {
			students = courseop.get();
			students.setName(name);
			res.setMessage("�ק令�\");
			studentDao.save(students);
			return res;
		}
		students.setStudent_id(studentid);
		students.setName(name);
		res.setMessage("�s�W���\");
		studentDao.save(students);
		return res;
	}

	// ==============================================================================3.�ǥͿ��
	@Override
	public CourseRes addStudentCourse(String studentid, Set<String> list) {
		Optional<Student> studentop = studentDao.findById(studentid); // ���ǥ�
		Student studentinfo = studentop.get(); // ����,�]���᭱�n���X��ҵ{����T
		CourseRes res = new CourseRes();
		boolean checklist = (new Courseimpl().checkList(list)); // �P�_�O�_���Ū��p����k
		if (checklist) {
			res.setMessage("���ର��");
			return res;
		}
		boolean chickisnull = false; // ��ǥ͸̭��쥻�S���ҵ{��,���Τ��ήɮ��ӧP�_�����L��
		String stucourseID = studentinfo.getCourse_id(); // ��string���ҵ{����T,(���ɷ|���@�j����)
		if ((stucourseID == null && studentop.isPresent()) || stucourseID.length() == 0) { // �p�G�쥻���ҵ{�Onull,�N��n�����P�_list�쥻����
			chickisnull = true; // �W���P�_��lengh���O�h�l��
		} else if (stucourseID.length() != 0 && studentop.isPresent()) {// �p�G�쥻���ҵ{�̭�����,�n���X�Ӷi�����
			String[] cutstring = stucourseID.split(","); // ���X�ǥͭ쥻�諸�ҵ{,����
			for (String item : cutstring) { // �s���}�C
				list.add(item.trim()); // �n�h�ť�,���M���X�Ӫ��F��|���ť�(�o�̪�add�Ӳz���|�[�즳�����id,���]��set�L�o����)
			}
		} else {
			res.setMessage("�d�L���H");
			return res;
		}
		int total = 0;// �p���`�@�Ǥ���
		if (studentop.isPresent() || chickisnull == true) {
			// ���o,�i�J�P�_
			List<Course> coursetime = courseDao.findAllByCourseidIn(list);
			// --------------------------------------------------���b
			if (list.size() != coursetime.size()) {
				res.setMessage("����s�W���b�W��W���ҵ{");
				return res;
			}
			// --------------------------------------------------�����o�h�~��i�J�s�����`
			for (int i = 0; i < coursetime.size(); i++) {
				Course courseid = coursetime.get(i);
				for (int j = i + 1; j < coursetime.size(); j++) {
					Course courseid2 = coursetime.get(j);
					if (courseid.getCourse_name().equals(courseid2.getCourse_name())) {
						res.setMessage("�ҵ{����ۦP");
						return res;
					}
					// ----------------
					/*
					 * �ɶ��P�_:�����P�_���S����week,���ɤ��ɶ�<�ư�>
					 * (   ��e"�}�l"�ɶ��b�U�@��Ҫ�"����"�ɶ�"�᭱"       <�o��Ʊ���"�_"��>
					 * 	/�ӥB/  
					 *     ��e"����"�ɶ��b�U�@��Ҫ�"�}�l"�ɶ�"�e��" )	  <�o��Ʊ���"�_"��>	 
					 */
					if (courseid.getWeek().equals(courseid2.getWeek())) {
						if ((!courseid.getStart_time().after(courseid2.getEnd_time()))
								&& (!courseid.getEnd_time().before(courseid2.getStart_time()))) {
							res.setMessage("�İ�");
							return res;
						}
					}
					// ----------------
				}
				total += courseid.getCourse_point();

			}
			if (total > 10) {
				res.setMessage("�j��10�Ǥ�");
				return res;
			}
		}
		// --------------------�̫�@�B
		String newstr = list.toString().substring(1, list.toString().length() - 1);
		studentinfo.setCourse_id(newstr);
		studentinfo.setCourse_point(total);
		studentDao.save(studentinfo);
		res.setMessage("���\");

		return res;
	}

	// ==============================================================================4.�ǥͧR��
	public CourseRes deleteCourse(String studentid, Set<String> list) {
		Optional<Student> studentop = studentDao.findById(studentid);
		CourseRes res = new CourseRes();
		// �T�{���S���o�ӤH,�S���N�����ױ�
		if (!studentop.isPresent() || !StringUtils.hasText(studentid)) {
			res.setMessage("�d�L���H");
			return res;
		}
		boolean checkdeletelistset = false;// �Φb�T�{�n�R�����ҵ{���S���Q�]�t�b�쥻��ҽd��
		Student studentinfo = studentop.get(); // ����,�]���᭱�n���X��ҵ{����T
		List<String> courseidlist = new ArrayList<>(); // ���쥻�ҵ{��list
		List<String> deletelistset = new ArrayList<>(); // �w�ƧR�����ҵ{
		List<String> finalsavelist = new ArrayList<>(); // �̲צs�i�h��list
		list.forEach(item -> {
			deletelistset.add(item.trim()); // �N���i�Ӫ��ǥͧR�Ҹ�T�নlist(�i��for�j��N��)
		});

		String[] cutstring = studentinfo.getCourse_id().split(",");// ����r��i�����
		for (var cutstringfor : cutstring) {
			courseidlist.add(cutstringfor.trim());// ���쥻�����ҵ{
		}
		// ------�T�{�n�R�����ҵ{���S���]�t�b��ҽd��
		for (int x = 0; x < deletelistset.size(); x++) {
			checkdeletelistset = courseidlist.contains(deletelistset.get(x)); // �]�t
		}
		// ------
		int total = 0;// ��Ǥ���
		// ==============================
		// ------�p�G�O�N���i��
		if (checkdeletelistset) {
			Compare: for (int i = 0; i < courseidlist.size(); i++) { // �����¦��ҵ{
				for (int j = 0; j < deletelistset.size(); j++) { // �w�ƧR���ҵ{
					if (deletelistset.get(j).equals(courseidlist.get(i))) {
						continue Compare;
					}
				}
				finalsavelist.add(courseidlist.get(i));
			}
		}
		// ------�_�h�N���L��
		else {
			res.setMessage("����R�����b��ҽd�򤺪��ҵ{");
			return res;
		}
		// -------------------------------�p��Ǥ�
		List<Course> savepointlist = courseDao.findAllByCourseidIn(finalsavelist);
		for (var savepointlistfor : savepointlist) {
			total += savepointlistfor.getCourse_point();
		}
		// ---------------------------------
		String newstr = finalsavelist.toString().substring(1, finalsavelist.toString().length() - 1);
		studentinfo.setCourse_id(newstr);
		studentinfo.setCourse_point(total);
		res.setMessage("�R��");
		studentDao.save(studentinfo);
		return res;
	}

	// ==============================================================5.�ǥѾǸ��d��,��ܽҵ{�N�X,�ҵ{�W�ٵ���T
	@Override
	public CourseRes searchStudentIdandCourseId(String studentid) {
		Optional<Student> studentop = studentDao.findById(studentid);
		CourseRes res = new CourseRes();
		if (!studentop.isPresent()) {
			res.setMessage("�d�L���H");
			return res;
		}
		List<String> studentCourselist = new ArrayList<>();
		Student studentinfo = studentop.get(); // ���o�ǥ͸�T,�Ǧ����ҵ{id
		String studentCourse = studentinfo.getCourse_id();
		String[] cutstring = studentCourse.split(",");
		for (var cutstringfor : cutstring) {
			studentCourselist.add(cutstringfor.trim()); // ���Φslist
		}
		List<Course> courseIdlist2 = new ArrayList<>();
		List<Student> studentlist = new ArrayList<>();
		List<Course> courseIdlist = courseDao.findAllByCourseidIn(studentCourselist);
		for (var courseIdlistfor : courseIdlist) {
			courseIdlist2.add(courseIdlistfor);
		}
		studentlist.add(studentinfo); // ��ܾǥ͸�T
		res.setStudentlist(studentlist);// ��ܾǥ͸�T
		res.setCourselist(courseIdlist2);// ��ܽҰ��T
		return res;
	}

	// ===================================================================6.�̽ҵ{�N�X�d��

	@Override
	public CourseRes searchCourseId(String courseid) {
		Optional<Course> courseOp = courseDao.findById(courseid);
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(courseid) || !courseOp.isPresent()) {
			res.setMessage("�d�L���N�X");
			return res;
		}
		Course courseget = courseOp.get();
		res.setCourse(courseget);
		return res;
	}

	// ====================================================================7.�̽ҵ{�W�٬d��
	public CourseRes searchByCourseName(String coursename) {
		List<Course> courselist = courseDao.findByCoursename(coursename);
		CourseRes res = new CourseRes();
		if (courselist.size() == 0 || !StringUtils.hasText(coursename)) {
			res.setMessage("�d�L���ҵ{");
			return res;
		}
		res.setCourselist(courselist);
		return res;

	}

}
