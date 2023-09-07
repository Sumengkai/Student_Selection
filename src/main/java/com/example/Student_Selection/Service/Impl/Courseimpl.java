package com.example.Student_Selection.Service.Impl;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.Student_Selection.Constants.StudentCourse_Rtncode;
import com.example.Student_Selection.Entity.Course;
import com.example.Student_Selection.Entity.CoursePeoples;
import com.example.Student_Selection.Entity.Student;
import com.example.Student_Selection.Respository.CourseDao;
import com.example.Student_Selection.Respository.CoursePeoplesDao;
import com.example.Student_Selection.Respository.StudentDao;
import com.example.Student_Selection.Service.Face.CourseFace;
import com.example.Student_Selection.Vo.CourseRes;

@Service
public class Courseimpl implements CourseFace {
	@Autowired
	private CourseDao courseDao;
	@Autowired
	private StudentDao studentDao;
	@Autowired
	private CoursePeoplesDao coursePeoplesDao;

//--------------------------------------------------------------------------�p����k
	// �T�{�O�_�ŦX�W��
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

	// ---------------------�T�{�O�_����
	private CourseRes checkListIsNull(List<String> checkNullList) {
		CourseRes res = new CourseRes();
		for (var item : checkNullList) {
			if (!StringUtils.hasText(item)) {
				res.setMessage(StudentCourse_Rtncode.CHECKLISTISNULL.getCode(),
						StudentCourse_Rtncode.CHECKLISTISNULL.getMessage());
				return res;
			}
		}

		return null;

	}

	// ---------------------�P�_�O�_�İ�(�H�κ�Ǥ�)<key, Object> Ps.Object�O�Ҧ����O������
	private Map<String, Object>  checkCourseIdTimeList(List<Course> courseTime, int pointTotal) {
		Map<String, Object> map = new HashMap<>();
		CourseRes res = new CourseRes();

		// ���o�Ĥ@����ƥh���U�@�����,���n����ۤv
		for (int i = 0; i < courseTime.size(); i++) {
			Course courseId1 = courseTime.get(i);
			for (int j = i + 1; j < courseTime.size(); j++) {
				Course courseId2 = courseTime.get(j);

				// �����ۦP�W�٪��ҵ{
				if (courseId1.getCourse_name().equals(courseId2.getCourse_name())) {
					res.setMessage(StudentCourse_Rtncode.CHECKCOURSENAME.getCode(),
							StudentCourse_Rtncode.CHECKCOURSENAME.getMessage());
					map.put("res", res);
					return map;
				}
				/*
				 * �ɶ��P�_:�����P�_���S����week,���ɤ��ɶ�<�ư�> ( ��e"�}�l"�ɶ��b�U�@��Ҫ�"����"�ɶ�"�᭱" <�o��Ʊ���"�_"��> /�ӥB/
				 * ��e"����"�ɶ��b�U�@��Ҫ�"�}�l"�ɶ�"�e��" ) <�o��Ʊ���"�_"��>
				 */
				if (courseId1.getWeek().equals(courseId2.getWeek())) {
					if ((!courseId1.getStart_time().after(courseId2.getEnd_time()))
							&& (!courseId1.getEnd_time().before(courseId2.getStart_time()))) {
						res.setMessage(StudentCourse_Rtncode.CHECKCOURSTIME.getCode(),
								StudentCourse_Rtncode.CHECKCOURSTIME.getMessage());
						map.put("res", res);
						return map;
					}
				}
			}
			pointTotal += courseId1.getCourse_point();
		}
		if (pointTotal > 10) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENTPOINT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENTPOINT.getMessage());
			map.put("res", res);
			return map;
		}
		// �o�̪�res�O�Ū�
		map.put("res", res);
		// key,�����`�M
		map.put("total", pointTotal);
		return map;
	}

//----------------------------------------------------------------------------�p����k
	// ==============================================================================1.�w��ҵ{�s�W+�ק�
	@Override
	public CourseRes addCourse(String courseid, String name, String week, String start, String end, int point) {
		CourseRes res = new CourseRes();
		List<String> checkListIsNull = Arrays.asList(courseid, name, week, start, end);
		CourseRes checkSomeInfo = checkListIsNull(checkListIsNull);
		if (checkSomeInfo != null) {
			return checkSomeInfo;
		}
		// ����X��檺�ɶ������W��F��
		String chickStartAndEnd = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]";
		// �T�{�O�_�ŦX���
		boolean checkStartTime = start.matches(chickStartAndEnd);
		boolean checkEndTime = end.matches(chickStartAndEnd);
		// �򥻨��b
		boolean checkWeekOrPoint = checkWeekAndPoint(week, point);

		// �򥻨��b 1. <�ɶ����P�_> 2. <�P���P�_> 3. <�Ǥ��P�_>
		if (!checkStartTime || !checkEndTime || !checkWeekOrPoint) {
			res.setMessage(StudentCourse_Rtncode.CHECKINFO.getCode(), StudentCourse_Rtncode.CHECKINFO.getMessage());
			return res;
		}

		// �]��Time�|����T�Ӹ`�I ,���q�`�u�|��J��� EX:01:00+:00,���F�����ন�ɶ����A
		start += ":00";
		end += ":00";

		Optional<Course> courseop = courseDao.findById(courseid);

		Course addOrUpdatecourse = new Course();
		// �ন�ɶ����A
		Time timeStart = Time.valueOf(start);
		Time timeEnd = Time.valueOf(end);

		if (timeEnd.before(timeStart)) {
			res.setMessage(StudentCourse_Rtncode.CHECKTIME.getCode(), StudentCourse_Rtncode.CHECKTIME.getMessage());
			return res;
		}

		// ���i��,�N��O�s�W
		if (!courseop.isPresent()) {
			addOrUpdatecourse.setCourse_id(courseid);
			addOrUpdatecourse.setCourse_name(name);
			addOrUpdatecourse.setWeek(week);
			addOrUpdatecourse.setStart_time(timeStart);
			addOrUpdatecourse.setEnd_time(timeEnd);
			addOrUpdatecourse.setCourse_point(point);
			courseDao.save(addOrUpdatecourse);
			return new CourseRes(addOrUpdatecourse, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());
		}

		// �W���S�ױ��O�ק�
		addOrUpdatecourse = courseop.get();
		addOrUpdatecourse.setCourse_name(name);
		addOrUpdatecourse.setWeek(week);
		addOrUpdatecourse.setStart_time(timeStart);
		addOrUpdatecourse.setEnd_time(timeEnd);
		addOrUpdatecourse.setCourse_point(point);
		courseDao.save(addOrUpdatecourse);
		return new CourseRes(addOrUpdatecourse, StudentCourse_Rtncode.UPDATESUCCESSFUL.getMessage());

	}

	// ==============================================================================2.�w��ǥͷs�W+�ק�
	@Override
	public CourseRes addStudent(String studentid, String name) {
		Student studentInfo = new Student();
		List<String> checkListIsNull = Arrays.asList(studentid, name);

		// ��k������b
		CourseRes res = checkListIsNull(checkListIsNull);
		if (res != null) {
			return res;
		}

//		//�W���S�ױ�����res�O�Ū�,�o�̭n���L�s���O����Ŷ�
//		res = new CourseRes(); 
//		
		Optional<Student> courseOp = studentDao.findById(studentid);

		// ���i�ӥN��O�ק�
		if (courseOp.isPresent()) {
			studentInfo = courseOp.get();
			studentInfo.setName(name);
			studentDao.save(studentInfo);
			return new CourseRes(studentInfo, StudentCourse_Rtncode.UPDATESUCCESSFUL.getMessage());
		}

		// �W���S�ױ��s�W
		studentInfo.setStudent_id(studentid);
		studentInfo.setName(name);
		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());
	}

	// ==============================================================================3.�ǥͿ��
	@Override
	public CourseRes addStudentCourse(String studentid, Set<String> list) {
		CourseRes res = new CourseRes();

		// �P�_�O�_���Ū��p����k
		boolean checkList = checkList(list);

		if (checkList || !StringUtils.hasText(studentid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}

		// �i��Ʈw
		Optional<Student> studentOp = studentDao.findById(studentid);

		// �䤣��ǥͪ����b
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}

		// ����,�]���᭱�n���X��ҵ{����T
		Student studentInfo = studentOp.get();

		// ��string���A ���ҵ{����T,(���ɷ|���@�j����) Ex: a01,a02
		String studentCourseId = studentInfo.getCourse_id();

		// �p�G�ǥͭ쥻���ҵ{�Onull,�N��n�����P�_list�쥻����,�p�G���O,�N��n����
		if (studentCourseId != null && studentCourseId.length() != 0) {

			// ���X�ǥͭ쥻�諸�ҵ{,���� Ex:[a01 , a02]
			String[] cutStudentCourseId = studentCourseId.split(",");

			for (String item : cutStudentCourseId) {
				list.add(item.trim()); // �n�h�ť�,���M���X�Ӫ��F��|���ť�(�o�̪�add�Ӳz���|�[�즳�����id,�P��set�L�o����)
			}
		}

		// �i�J��Ʈw
		List<Course> courseTime = courseDao.findAllByCourseidIn(list);

		// ��p�G�L�[�諸�ҵ{���b�ҵ{�W�椤�����b�P�_
		if (list.size() != courseTime.size()) {
			res.setMessage(StudentCourse_Rtncode.CHECKCOURSELIST.getCode(),
					StudentCourse_Rtncode.CHECKCOURSELIST.getMessage());
			return res;
		}

		// �p���`�@�Ǥ���
		int calculatePoint = 0;

		// �i�J�j��,�åB�P�_ 1.���L���ۦP�W�٪��ҵ{ 2.�İ� 3.�j��10�Ǥ� 4.�H�W���S���D,�Ǥ��p��
		Map<String, Object> map = checkCourseIdTimeList(courseTime, calculatePoint);

		// ��key���o��T��
		// �j���૬<��res���O�Ū��ɭ�,�O���~��,�T�n�i�J�P�_,�^�ǰT�����Τ�>
		res = (CourseRes) map.get("res");

		// �j���૬<���X�Ǥ��`�M>
		Integer resultTotal = (Integer) map.get("total");

		// �p�GMessage���F��,�L�|�i��,�åB�^�ǸӨ��b�T��
		if (StringUtils.hasText(res.getMessage())) {
			return res;
		}

		// --------------------�̫�@�B
		String newString = list.toString().substring(1, list.toString().length() - 1);
		studentInfo.setCourse_id(newString);
		studentInfo.setCourse_point(resultTotal);
		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());

	}

	// ==============================================================================4.�ǥͰh��
	public CourseRes deleteCourse(String studentid, Set<String> list) {
		CourseRes res = new CourseRes();

		if (!StringUtils.hasText(studentid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}

		Optional<Student> studentOp = studentDao.findById(studentid);

		// ���i�ӬO�s�W
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}

		// �Φb�T�{�n�R�����ҵ{���S���Q�]�t�b�쥻��ҽd��
		boolean checkDeleteList = false;

		// ����,�]���᭱�n���X��ҵ{����T
		Student studentInfo = studentOp.get();

		// ���쥻�ҵ{��list
		List<String> courseIdList = new ArrayList<>();

		// �w�ƧR�����ҵ{
		List<String> deleteCourseIdList = new ArrayList<>();

		// �̲צs�i�h��list
		List<String> finalSaveList = new ArrayList<>();

		// �N���i�Ӫ��ǥͧR�Ҹ�T�নlist(�i��for�j��N��)
		list.forEach(item -> {
			deleteCourseIdList.add(item.trim());
		});

		// ����r��i�����(�ǥͭ쥻���ҵ{)�åB���X�쥻���ҵ{
		String[] cutString = studentInfo.getCourse_id().split(",");
		for (var courseItem : cutString) {
			courseIdList.add(courseItem.trim());
		}

		// �T�{�n�R�����ҵ{���S���]�t�b��ҽd��
		for (int x = 0; x < deleteCourseIdList.size(); x++) {
			checkDeleteList = courseIdList.contains(deleteCourseIdList.get(x)); // �]�t
		}

		// ��Ǥ���
		int total = 0;

		// �Φb�T�{�n�R�����ҵ{���S���Q�]�t�b�쥻��ҽd��<�����]�t�N�i��>
		if (checkDeleteList) {
			Compare: for (int i = 0; i < courseIdList.size(); i++) { // �����¦��ҵ{
				for (int j = 0; j < deleteCourseIdList.size(); j++) { // �w�ƧR���ҵ{
					if (deleteCourseIdList.get(j).equals(courseIdList.get(i))) {// ���]�t�a��ܭn�h��ӽҵ{,���N���n�s�ifinalSaveList
						continue Compare; // ��J�즳�]�t��,���^���w�j��
					}
				}
				finalSaveList.add(courseIdList.get(i)); // �N�̲׭n�s�i�h���ҵ{��ilist
			}
		}
		// ���i�ӥN��h�諸�ҵ{���s�b�L��Ҫ��d��
		else {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENTCOURSELIST.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENTCOURSELIST.getMessage());
			return res;
		}
		// -------------------------------�p��Ǥ�

		List<Course> savePointList = courseDao.findAllByCourseidIn(finalSaveList);
		for (var pointItem : savePointList) {
			total += pointItem.getCourse_point();
		}

		// ---------------------------------�̫�@�B
		String newString = finalSaveList.toString().substring(1, finalSaveList.toString().length() - 1);
		studentInfo.setCourse_id(newString);
		studentInfo.setCourse_point(total);
		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.DELETESUCCESSFUL.getMessage());
	}

	// ==============================================================5.�ǥѾǸ��d��,��ܽҵ{�N�X,�ҵ{�W�ٵ���T
	@Override
	public CourseRes searchStudentIdandCourseId(String studentId) {
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(studentId)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}
		Optional<Student> studentOp = studentDao.findById(studentId);
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}
		// ���o�ӾǥͿ諸�ҵ{
		List<String> studentCourseList = new ArrayList<>();

		// ���o�ǥ͸�T,�Ǧ����ҵ{id
		Student studentInfo = studentOp.get();

		String studentCourse = studentInfo.getCourse_id();
		String[] cutstring = studentCourse.split(",");

		// ���Φslist
		for (var studentCourseItem : cutstring) {
			studentCourseList.add(studentCourseItem.trim());
		}

		// �ΨӦs�ҵ{��T��list
		List<Course> CourseIdInfoList = new ArrayList<>();

		// �i�J��Ʈw�j���ӽҵ{��T
		List<Course> courseIdInfo = courseDao.findAllByCourseidIn(studentCourseList);
		for (var courseItem : courseIdInfo) {
			CourseIdInfoList.add(courseItem);
		}
		// ��ܾǥ͸�T
		res.setStudent(studentInfo);
		// ��ܽҰ��T
		res.setCourselist(CourseIdInfoList);

		return res;
	}

	// ===================================================================6.�̽ҵ{�N�X�d��

	@Override
	public CourseRes searchCourseId(String courseid) {
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(courseid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}
		Optional<Course> courseOp = courseDao.findById(courseid);

		if (!courseOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CANTFINDCOURSE.getCode(),
					StudentCourse_Rtncode.CANTFINDCOURSE.getMessage());
			return res;
		}

		// �W�����b�S�ױ�,�n���o�ҵ{��T
		Course courseGet = courseOp.get();
		res.setCourse(courseGet);
		return res;
	}

	// ====================================================================7.�̽ҵ{�W�٬d��
	public CourseRes searchByCourseName(String coursename) {
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(coursename)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}
		List<Course> courseInfoList = courseDao.findByCoursename(coursename);

		// �o�̨����O���s�b���ҵ{�W��
		if (courseInfoList.size() == 0) {
			res.setMessage(StudentCourse_Rtncode.CANTFINDCOURSE.getCode(),
					StudentCourse_Rtncode.CANTFINDCOURSE.getMessage());
			return res;
		}

		// �W�����b�S�ױ�,��ܽҵ{��T
		res.setCourselist(courseInfoList);

		return res;

	}

	// --------------------------------���ѴN�R��
	@Override
	public CourseRes addStudentCourseAndLimitPeople(String studentid, Set<String> list) {

		CourseRes res = new CourseRes();

		// �P�_�O�_���Ū��p����k
		boolean checkList = checkList(list);

		if (checkList || !StringUtils.hasText(studentid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}

		// �i��Ʈw
		Optional<Student> studentOp = studentDao.findById(studentid);

		// �䤣��ǥͪ����b
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}

		// ����,�]���᭱�n���X��ҵ{����T
		Student studentInfo = studentOp.get();

		// ��string���A ���ҵ{����T,(���ɷ|���@�j����) Ex: a01,a02
		String studentCourseId = studentInfo.getCourse_id();

		// �p�G�ǥͭ쥻���ҵ{�Onull,�N��n�����P�_list�쥻����,�p�G���O,�N��n����
		if (studentCourseId != null && studentCourseId.length() != 0) {

			// ���X�ǥͭ쥻�諸�ҵ{,���� Ex:[a01 , a02]
			String[] cutStudentCourseId = studentCourseId.split(",");

			for (String item : cutStudentCourseId) {
				list.add(item.trim()); // �n�h�ť�,���M���X�Ӫ��F��|���ť�(�o�̪�add�Ӳz���|�[�즳�����id,�P��set�L�o����)
			}
		}

		// �i�J��Ʈw
		List<Course> courseTime = courseDao.findAllByCourseidIn(list);

		// ��p�G�L�[�諸�ҵ{���b�ҵ{�W�椤�����b�P�_
		if (list.size() != courseTime.size()) {
			res.setMessage(StudentCourse_Rtncode.CHECKCOURSELIST.getCode(),
					StudentCourse_Rtncode.CHECKCOURSELIST.getMessage());
			return res;
		}

		// �p���`�@�Ǥ���
		int calculatePoint = 0;

		// �i�J�j��,�åB�P�_ 1.���L���ۦP�W�٪��ҵ{ 2.�İ� 3.�j��10�Ǥ� 4.�H�W���S���D,�i��Ǥ��p��
		Map<String, Object> map = checkCourseIdTimeList(courseTime, calculatePoint);

		// ��key���o��T��
		// �j���૬<��res���O�Ū��ɭ�,�O���~��,�T�n�i�J�P�_,�^�ǰT�����Τ�>
		res = (CourseRes) map.get("res");

		// �j���૬<���X�Ǥ��`�M>
		Integer resultTotal = (Integer) map.get("total");

		// �p�GMessage���F��,�L�|�i��,�åB�^�ǸӨ��b�T��
		if (StringUtils.hasText(res.getMessage())) {
			return res;
		}
		CoursePeoples peoples = new CoursePeoples();
		CoursePeoples peoples2 = new CoursePeoples();
		// �Ω�P�_�H�ƤW��
		List<CoursePeoples> peoplesListlimit = new ArrayList<>();
//				coursePeoplesDao.findByCourseIdAndCoursePeople(studentCourseId, studentCourseId)�C

		List<String> checkcouresidpeople = new ArrayList<>();
		for (var courseItem : courseTime) {
			checkcouresidpeople.add(courseItem.getCourse_id());
		}
		List<String> peopless = new ArrayList<>();
		
		boolean checkIsEmpty = false;
		List<CoursePeoples> peoplesList = coursePeoplesDao.findByCourseIdIn(checkcouresidpeople); // ex:a01,a02�ҵ{
		if (peoplesList.isEmpty()) {
			for (var courseItem : courseTime) {
				int x = (int)(Math.random()*1000000);
				peoples.setSerialNumber(x);
				peoples.setCourseId(courseItem.getCourse_id());
				peoples.setCoursePeople(studentid);
				coursePeoplesDao.save(peoples); 
//				x++;
				checkIsEmpty = true;
			}
		}
		if (!checkIsEmpty) {
			for (int i = 0; i < peoplesList.size(); i++) {
				peoples = peoplesList.get(i);
				for (int j = i + 1; j < peoplesList.size(); j++) { //�o�̪��j���޿���~
					peoples2 = peoplesList.get(j);
					if (peoples.getCourseId().equals(peoples2.getCourseId())
							&& peoples.getCoursePeople().equals(peoples2.getCoursePeople())) {
						res.setMessage("", "�O��P���ҵ{");
						return res;
					} else if (peoples.getCourseId().equals(peoples2.getCourseId())) {
						peoplesListlimit.add(peoples); // ���o�Ӫ��Ҫ��`�H��ex:a01:3�H/a02:4�H
					}
					if (peoplesListlimit.size() > (peoplesList.size() * 5)) {
						res.setMessage("", "�H�ƨ�F�W��");
						return res;
					}
					peoples.setCourseId(peoples.getCourseId());
					peoples.setCoursePeople(studentid);
					coursePeoplesDao.save(peoples);
				}
			}
		}
		// --------------------�̫�@�B
		String newString = list.toString().substring(1, list.toString().length() - 1);
		studentInfo.setCourse_id(newString);
		studentInfo.setCourse_point(resultTotal);

		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());

	}

}
