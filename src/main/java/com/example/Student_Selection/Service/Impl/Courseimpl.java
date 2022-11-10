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

//--------------------------------------------------------------------------私有方法
	private boolean checkWeekAndPoint(String week, int point) {
		List<String> list = new ArrayList<>(List.of("一", "二", "三", "四", "五"));
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
	// 確認陣列是否空
	private boolean checkList(Set<String> listset) {
		return listset.isEmpty();
	}

//----------------------------------------------------------------------------私有方法
	// ==============================================================================1.針對課程新增+修改
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
			res.setMessage("不能空空或不能撞時間");
			return res;
		}
		if (!a || !b || !c) {
			res.setMessage("時間格式ex: 01:00 or week只能 一 ~ 五 or 學分不得低於零,大於三或null");
			return res;
		}
		Time TimeStart = Time.valueOf(start);
		Time TimeEnd = Time.valueOf(end);
		if (TimeEnd.before(TimeStart)) {
			res.setMessage("結束時間不可小於開始時間");
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
			res.setMessage("新增成功");
			return res;
		}
		addandUpdatecoursec = courseop.get();
		addandUpdatecoursec.setCourse_name(name);
		addandUpdatecoursec.setWeek(week);
		addandUpdatecoursec.setStart_time(TimeStart);
		addandUpdatecoursec.setEnd_time(TimeEnd);
		addandUpdatecoursec.setCourse_point(point);
		courseDao.save(addandUpdatecoursec);
		res.setMessage("修改成功"); // 還需一個更新學生學分的程式
		return res;
	}

	// ==============================================================================2.針對學生新增+修改
	@Override
	public CourseRes addStudent(String studentid, String name) {
		Student students = new Student();
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(studentid) || !StringUtils.hasText(name)) {
			res.setMessage("不能空");
			return res;
		}
		Optional<Student> courseop = studentDao.findById(studentid);
		if (courseop.isPresent()) {
			students = courseop.get();
			students.setName(name);
			res.setMessage("修改成功");
			studentDao.save(students);
			return res;
		}
		students.setStudent_id(studentid);
		students.setName(name);
		res.setMessage("新增成功");
		studentDao.save(students);
		return res;
	}

	// ==============================================================================3.學生選課
	@Override
	public CourseRes addStudentCourse(String studentid, Set<String> list) {
		Optional<Student> studentop = studentDao.findById(studentid); // 找到學生
		Student studentinfo = studentop.get(); // 取值,因為後面要拿出選課程的資訊
		CourseRes res = new CourseRes();
		boolean checklist = (new Courseimpl().checkList(list)); // 判斷是否為空的私有方法
		if (checklist) {
			res.setMessage("不能為空");
			return res;
		}
		boolean chickisnull = false; // 當學生裡面原本沒有課程時,不用切割時拿來判斷的布林值
		String stucourseID = studentinfo.getCourse_id(); // 用string找到課程的資訊,(此時會有一大長串)
		if ((stucourseID == null && studentop.isPresent()) || stucourseID.length() == 0) { // 如果原本的課程是null,代表要直接判斷list原本的值
			chickisnull = true; // 上面判斷的lengh其實是多餘的
		} else if (stucourseID.length() != 0 && studentop.isPresent()) {// 如果原本的課程裡面有值,要拿出來進行切割
			String[] cutstring = stucourseID.split(","); // 拿出學生原本選的課程,切割
			for (String item : cutstring) { // 編歷陣列
				list.add(item.trim()); // 要去空白,不然切出來的東西會有空白(這裡的add照理說會加原有的選課id,但因為set過濾重複)
			}
		} else {
			res.setMessage("查無此人");
			return res;
		}
		int total = 0;// 計算總共學分數
		if (studentop.isPresent() || chickisnull == true) {
			// 取得,進入判斷
			List<Course> coursetime = courseDao.findAllByCourseidIn(list);
			// --------------------------------------------------防呆
			if (list.size() != coursetime.size()) {
				res.setMessage("不能新增不在名單上的課程");
				return res;
			}
			// --------------------------------------------------防完這層才能進入編歷環節
			for (int i = 0; i < coursetime.size(); i++) {
				Course courseid = coursetime.get(i);
				for (int j = i + 1; j < coursetime.size(); j++) {
					Course courseid2 = coursetime.get(j);
					if (courseid.getCourse_name().equals(courseid2.getCourse_name())) {
						res.setMessage("課程不能相同");
						return res;
					}
					// ----------------
					/*
					 * 時間判斷:首先判斷有沒有撞week,撞時比對時間<排除>
					 * (   當前"開始"時間在下一堂課的"結束"時間"後面"       <這件事情為"否"時>
					 * 	/而且/  
					 *     當前"結束"時間在下一堂課的"開始"時間"前面" )	  <這件事情為"否"時>	 
					 */
					if (courseid.getWeek().equals(courseid2.getWeek())) {
						if ((!courseid.getStart_time().after(courseid2.getEnd_time()))
								&& (!courseid.getEnd_time().before(courseid2.getStart_time()))) {
							res.setMessage("衝堂");
							return res;
						}
					}
					// ----------------
				}
				total += courseid.getCourse_point();

			}
			if (total > 10) {
				res.setMessage("大於10學分");
				return res;
			}
		}
		// --------------------最後一步
		String newstr = list.toString().substring(1, list.toString().length() - 1);
		studentinfo.setCourse_id(newstr);
		studentinfo.setCourse_point(total);
		studentDao.save(studentinfo);
		res.setMessage("成功");

		return res;
	}

	// ==============================================================================4.學生刪課
	public CourseRes deleteCourse(String studentid, Set<String> list) {
		Optional<Student> studentop = studentDao.findById(studentid);
		CourseRes res = new CourseRes();
		// 確認有沒有這個人,沒有就直接擋掉
		if (!studentop.isPresent() || !StringUtils.hasText(studentid)) {
			res.setMessage("查無此人");
			return res;
		}
		boolean checkdeletelistset = false;// 用在確認要刪除的課程有沒有被包含在原本選課範圍內
		Student studentinfo = studentop.get(); // 取值,因為後面要拿出選課程的資訊
		List<String> courseidlist = new ArrayList<>(); // 接原本課程的list
		List<String> deletelistset = new ArrayList<>(); // 預備刪除的課程
		List<String> finalsavelist = new ArrayList<>(); // 最終存進去的list
		list.forEach(item -> {
			deletelistset.add(item.trim()); // 將接進來的學生刪課資訊轉成list(可用for迴圈代替)
		});

		String[] cutstring = studentinfo.getCourse_id().split(",");// 對長字串進行切割
		for (var cutstringfor : cutstring) {
			courseidlist.add(cutstringfor.trim());// 接原本有的課程
		}
		// ------確認要刪除的課程有沒有包含在選課範圍內
		for (int x = 0; x < deletelistset.size(); x++) {
			checkdeletelistset = courseidlist.contains(deletelistset.get(x)); // 包含
		}
		// ------
		int total = 0;// 算學分用
		// ==============================
		// ------如果是就掉進來
		if (checkdeletelistset) {
			Compare: for (int i = 0; i < courseidlist.size(); i++) { // 全部舊有課程
				for (int j = 0; j < deletelistset.size(); j++) { // 預備刪除課程
					if (deletelistset.get(j).equals(courseidlist.get(i))) {
						continue Compare;
					}
				}
				finalsavelist.add(courseidlist.get(i));
			}
		}
		// ------否則就掉過來
		else {
			res.setMessage("不能刪除不在選課範圍內的課程");
			return res;
		}
		// -------------------------------計算學分
		List<Course> savepointlist = courseDao.findAllByCourseidIn(finalsavelist);
		for (var savepointlistfor : savepointlist) {
			total += savepointlistfor.getCourse_point();
		}
		// ---------------------------------
		String newstr = finalsavelist.toString().substring(1, finalsavelist.toString().length() - 1);
		studentinfo.setCourse_id(newstr);
		studentinfo.setCourse_point(total);
		res.setMessage("刪除");
		studentDao.save(studentinfo);
		return res;
	}

	// ==============================================================5.藉由學號查詢,顯示課程代碼,課程名稱等資訊
	@Override
	public CourseRes searchStudentIdandCourseId(String studentid) {
		Optional<Student> studentop = studentDao.findById(studentid);
		CourseRes res = new CourseRes();
		if (!studentop.isPresent()) {
			res.setMessage("查無此人");
			return res;
		}
		List<String> studentCourselist = new ArrayList<>();
		Student studentinfo = studentop.get(); // 取得學生資訊,藉此比對課程id
		String studentCourse = studentinfo.getCourse_id();
		String[] cutstring = studentCourse.split(",");
		for (var cutstringfor : cutstring) {
			studentCourselist.add(cutstringfor.trim()); // 切割存list
		}
		List<Course> courseIdlist2 = new ArrayList<>();
		List<Student> studentlist = new ArrayList<>();
		List<Course> courseIdlist = courseDao.findAllByCourseidIn(studentCourselist);
		for (var courseIdlistfor : courseIdlist) {
			courseIdlist2.add(courseIdlistfor);
		}
		studentlist.add(studentinfo); // 顯示學生資訊
		res.setStudentlist(studentlist);// 顯示學生資訊
		res.setCourselist(courseIdlist2);// 顯示課堂資訊
		return res;
	}

	// ===================================================================6.依課程代碼查詢

	@Override
	public CourseRes searchCourseId(String courseid) {
		Optional<Course> courseOp = courseDao.findById(courseid);
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(courseid) || !courseOp.isPresent()) {
			res.setMessage("查無此代碼");
			return res;
		}
		Course courseget = courseOp.get();
		res.setCourse(courseget);
		return res;
	}

	// ====================================================================7.依課程名稱查詢
	public CourseRes searchByCourseName(String coursename) {
		List<Course> courselist = courseDao.findByCoursename(coursename);
		CourseRes res = new CourseRes();
		if (courselist.size() == 0 || !StringUtils.hasText(coursename)) {
			res.setMessage("查無此課程");
			return res;
		}
		res.setCourselist(courselist);
		return res;

	}

}
