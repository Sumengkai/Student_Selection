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

//--------------------------------------------------------------------------私有方法
	// 確認是否符合規格
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

	// ---------------------確認是否為空
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

	// ---------------------判斷是否衝堂(以及算學分)<key, Object> Ps.Object是所有型別的父親
	private Map<String, Object>  checkCourseIdTimeList(List<Course> courseTime, int pointTotal) {
		Map<String, Object> map = new HashMap<>();
		CourseRes res = new CourseRes();

		// 取得第一筆資料去比對下一筆資料,不要比對到自己
		for (int i = 0; i < courseTime.size(); i++) {
			Course courseId1 = courseTime.get(i);
			for (int j = i + 1; j < courseTime.size(); j++) {
				Course courseId2 = courseTime.get(j);

				// 不能選相同名稱的課程
				if (courseId1.getCourse_name().equals(courseId2.getCourse_name())) {
					res.setMessage(StudentCourse_Rtncode.CHECKCOURSENAME.getCode(),
							StudentCourse_Rtncode.CHECKCOURSENAME.getMessage());
					map.put("res", res);
					return map;
				}
				/*
				 * 時間判斷:首先判斷有沒有撞week,撞時比對時間<排除> ( 當前"開始"時間在下一堂課的"結束"時間"後面" <這件事情為"否"時> /而且/
				 * 當前"結束"時間在下一堂課的"開始"時間"前面" ) <這件事情為"否"時>
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
		// 這裡的res是空的
		map.put("res", res);
		// key,分數總和
		map.put("total", pointTotal);
		return map;
	}

//----------------------------------------------------------------------------私有方法
	// ==============================================================================1.針對課程新增+修改
	@Override
	public CourseRes addCourse(String courseid, String name, String week, String start, String end, int point) {
		CourseRes res = new CourseRes();
		List<String> checkListIsNull = Arrays.asList(courseid, name, week, start, end);
		CourseRes checkSomeInfo = checkListIsNull(checkListIsNull);
		if (checkSomeInfo != null) {
			return checkSomeInfo;
		}
		// 防止不合資格的時間的正規表達式
		String chickStartAndEnd = "(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]";
		// 確認是否符合資格
		boolean checkStartTime = start.matches(chickStartAndEnd);
		boolean checkEndTime = end.matches(chickStartAndEnd);
		// 基本防呆
		boolean checkWeekOrPoint = checkWeekAndPoint(week, point);

		// 基本防呆 1. <時間資格判斷> 2. <星期判斷> 3. <學分判斷>
		if (!checkStartTime || !checkEndTime || !checkWeekOrPoint) {
			res.setMessage(StudentCourse_Rtncode.CHECKINFO.getCode(), StudentCourse_Rtncode.CHECKINFO.getMessage());
			return res;
		}

		// 因為Time會取到三個節點 ,但通常只會輸入兩個 EX:01:00+:00,為了之後轉成時間型態
		start += ":00";
		end += ":00";

		Optional<Course> courseop = courseDao.findById(courseid);

		Course addOrUpdatecourse = new Course();
		// 轉成時間型態
		Time timeStart = Time.valueOf(start);
		Time timeEnd = Time.valueOf(end);

		if (timeEnd.before(timeStart)) {
			res.setMessage(StudentCourse_Rtncode.CHECKTIME.getCode(), StudentCourse_Rtncode.CHECKTIME.getMessage());
			return res;
		}

		// 掉進來,代表是新增
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

		// 上面沒擋掉是修改
		addOrUpdatecourse = courseop.get();
		addOrUpdatecourse.setCourse_name(name);
		addOrUpdatecourse.setWeek(week);
		addOrUpdatecourse.setStart_time(timeStart);
		addOrUpdatecourse.setEnd_time(timeEnd);
		addOrUpdatecourse.setCourse_point(point);
		courseDao.save(addOrUpdatecourse);
		return new CourseRes(addOrUpdatecourse, StudentCourse_Rtncode.UPDATESUCCESSFUL.getMessage());

	}

	// ==============================================================================2.針對學生新增+修改
	@Override
	public CourseRes addStudent(String studentid, String name) {
		Student studentInfo = new Student();
		List<String> checkListIsNull = Arrays.asList(studentid, name);

		// 方法抽取防呆
		CourseRes res = checkListIsNull(checkListIsNull);
		if (res != null) {
			return res;
		}

//		//上面沒擋掉的話res是空的,這裡要給他新的記憶體空間
//		res = new CourseRes(); 
//		
		Optional<Student> courseOp = studentDao.findById(studentid);

		// 掉進來代表是修改
		if (courseOp.isPresent()) {
			studentInfo = courseOp.get();
			studentInfo.setName(name);
			studentDao.save(studentInfo);
			return new CourseRes(studentInfo, StudentCourse_Rtncode.UPDATESUCCESSFUL.getMessage());
		}

		// 上面沒擋掉新增
		studentInfo.setStudent_id(studentid);
		studentInfo.setName(name);
		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());
	}

	// ==============================================================================3.學生選課
	@Override
	public CourseRes addStudentCourse(String studentid, Set<String> list) {
		CourseRes res = new CourseRes();

		// 判斷是否為空的私有方法
		boolean checkList = checkList(list);

		if (checkList || !StringUtils.hasText(studentid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}

		// 進資料庫
		Optional<Student> studentOp = studentDao.findById(studentid);

		// 找不到學生的防呆
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}

		// 取值,因為後面要拿出選課程的資訊
		Student studentInfo = studentOp.get();

		// 用string型態 找到課程的資訊,(此時會有一大長串) Ex: a01,a02
		String studentCourseId = studentInfo.getCourse_id();

		// 如果學生原本的課程是null,代表要直接判斷list原本的值,如果不是,代表要切割
		if (studentCourseId != null && studentCourseId.length() != 0) {

			// 拿出學生原本選的課程,切割 Ex:[a01 , a02]
			String[] cutStudentCourseId = studentCourseId.split(",");

			for (String item : cutStudentCourseId) {
				list.add(item.trim()); // 要去空白,不然切出來的東西會有空白(這裡的add照理說會加原有的選課id,同時set過濾重複)
			}
		}

		// 進入資料庫
		List<Course> courseTime = courseDao.findAllByCourseidIn(list);

		// 當如果他加選的課程不在課程名單中的防呆判斷
		if (list.size() != courseTime.size()) {
			res.setMessage(StudentCourse_Rtncode.CHECKCOURSELIST.getCode(),
					StudentCourse_Rtncode.CHECKCOURSELIST.getMessage());
			return res;
		}

		// 計算總共學分數
		int calculatePoint = 0;

		// 進入迴圈,並且判斷 1.有無選到相同名稱的課程 2.衝堂 3.大於10學分 4.以上都沒問題,學分計算
		Map<String, Object> map = checkCourseIdTimeList(courseTime, calculatePoint);

		// 用key取得到訊息
		// 強制轉型<當res不是空的時候,是錯誤的,固要進入判斷,回傳訊息給用戶>
		res = (CourseRes) map.get("res");

		// 強制轉型<取出學分總和>
		Integer resultTotal = (Integer) map.get("total");

		// 如果Message有東西,他會進來,並且回傳該防呆訊息
		if (StringUtils.hasText(res.getMessage())) {
			return res;
		}

		// --------------------最後一步
		String newString = list.toString().substring(1, list.toString().length() - 1);
		studentInfo.setCourse_id(newString);
		studentInfo.setCourse_point(resultTotal);
		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());

	}

	// ==============================================================================4.學生退選
	public CourseRes deleteCourse(String studentid, Set<String> list) {
		CourseRes res = new CourseRes();

		if (!StringUtils.hasText(studentid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}

		Optional<Student> studentOp = studentDao.findById(studentid);

		// 掉進來是新增
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}

		// 用在確認要刪除的課程有沒有被包含在原本選課範圍內
		boolean checkDeleteList = false;

		// 取值,因為後面要拿出選課程的資訊
		Student studentInfo = studentOp.get();

		// 接原本課程的list
		List<String> courseIdList = new ArrayList<>();

		// 預備刪除的課程
		List<String> deleteCourseIdList = new ArrayList<>();

		// 最終存進去的list
		List<String> finalSaveList = new ArrayList<>();

		// 將接進來的學生刪課資訊轉成list(可用for迴圈代替)
		list.forEach(item -> {
			deleteCourseIdList.add(item.trim());
		});

		// 對長字串進行切割(學生原本的課程)並且接出原本的課程
		String[] cutString = studentInfo.getCourse_id().split(",");
		for (var courseItem : cutString) {
			courseIdList.add(courseItem.trim());
		}

		// 確認要刪除的課程有沒有包含在選課範圍內
		for (int x = 0; x < deleteCourseIdList.size(); x++) {
			checkDeleteList = courseIdList.contains(deleteCourseIdList.get(x)); // 包含
		}

		// 算學分用
		int total = 0;

		// 用在確認要刪除的課程有沒有被包含在原本選課範圍內<都有包含就進來>
		if (checkDeleteList) {
			Compare: for (int i = 0; i < courseIdList.size(); i++) { // 全部舊有課程
				for (int j = 0; j < deleteCourseIdList.size(); j++) { // 預備刪除課程
					if (deleteCourseIdList.get(j).equals(courseIdList.get(i))) {// 有包含帶表示要退選該課程,那就不要存進finalSaveList
						continue Compare; // 當遇到有包含時,跳回指定迴圈
					}
				}
				finalSaveList.add(courseIdList.get(i)); // 將最終要存進去的課程放進list
			}
		}
		// 掉進來代表退選的課程不存在他選課的範圍
		else {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENTCOURSELIST.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENTCOURSELIST.getMessage());
			return res;
		}
		// -------------------------------計算學分

		List<Course> savePointList = courseDao.findAllByCourseidIn(finalSaveList);
		for (var pointItem : savePointList) {
			total += pointItem.getCourse_point();
		}

		// ---------------------------------最後一步
		String newString = finalSaveList.toString().substring(1, finalSaveList.toString().length() - 1);
		studentInfo.setCourse_id(newString);
		studentInfo.setCourse_point(total);
		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.DELETESUCCESSFUL.getMessage());
	}

	// ==============================================================5.藉由學號查詢,顯示課程代碼,課程名稱等資訊
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
		// 取得該學生選的課程
		List<String> studentCourseList = new ArrayList<>();

		// 取得學生資訊,藉此比對課程id
		Student studentInfo = studentOp.get();

		String studentCourse = studentInfo.getCourse_id();
		String[] cutstring = studentCourse.split(",");

		// 切割存list
		for (var studentCourseItem : cutstring) {
			studentCourseList.add(studentCourseItem.trim());
		}

		// 用來存課程資訊的list
		List<Course> CourseIdInfoList = new ArrayList<>();

		// 進入資料庫搜索該課程資訊
		List<Course> courseIdInfo = courseDao.findAllByCourseidIn(studentCourseList);
		for (var courseItem : courseIdInfo) {
			CourseIdInfoList.add(courseItem);
		}
		// 顯示學生資訊
		res.setStudent(studentInfo);
		// 顯示課堂資訊
		res.setCourselist(CourseIdInfoList);

		return res;
	}

	// ===================================================================6.依課程代碼查詢

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

		// 上面防呆沒擋掉,要取得課程資訊
		Course courseGet = courseOp.get();
		res.setCourse(courseGet);
		return res;
	}

	// ====================================================================7.依課程名稱查詢
	public CourseRes searchByCourseName(String coursename) {
		CourseRes res = new CourseRes();
		if (!StringUtils.hasText(coursename)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}
		List<Course> courseInfoList = courseDao.findByCoursename(coursename);

		// 這裡防的是不存在的課程名稱
		if (courseInfoList.size() == 0) {
			res.setMessage(StudentCourse_Rtncode.CANTFINDCOURSE.getCode(),
					StudentCourse_Rtncode.CANTFINDCOURSE.getMessage());
			return res;
		}

		// 上面防呆沒擋掉,顯示課程資訊
		res.setCourselist(courseInfoList);

		return res;

	}

	// --------------------------------失敗就刪掉
	@Override
	public CourseRes addStudentCourseAndLimitPeople(String studentid, Set<String> list) {

		CourseRes res = new CourseRes();

		// 判斷是否為空的私有方法
		boolean checkList = checkList(list);

		if (checkList || !StringUtils.hasText(studentid)) {
			res.setMessage(StudentCourse_Rtncode.CHECKNULL.getCode(), StudentCourse_Rtncode.CHECKNULL.getMessage());
			return res;
		}

		// 進資料庫
		Optional<Student> studentOp = studentDao.findById(studentid);

		// 找不到學生的防呆
		if (!studentOp.isPresent()) {
			res.setMessage(StudentCourse_Rtncode.CHECKSTUDENT.getCode(),
					StudentCourse_Rtncode.CHECKSTUDENT.getMessage());
			return res;
		}

		// 取值,因為後面要拿出選課程的資訊
		Student studentInfo = studentOp.get();

		// 用string型態 找到課程的資訊,(此時會有一大長串) Ex: a01,a02
		String studentCourseId = studentInfo.getCourse_id();

		// 如果學生原本的課程是null,代表要直接判斷list原本的值,如果不是,代表要切割
		if (studentCourseId != null && studentCourseId.length() != 0) {

			// 拿出學生原本選的課程,切割 Ex:[a01 , a02]
			String[] cutStudentCourseId = studentCourseId.split(",");

			for (String item : cutStudentCourseId) {
				list.add(item.trim()); // 要去空白,不然切出來的東西會有空白(這裡的add照理說會加原有的選課id,同時set過濾重複)
			}
		}

		// 進入資料庫
		List<Course> courseTime = courseDao.findAllByCourseidIn(list);

		// 當如果他加選的課程不在課程名單中的防呆判斷
		if (list.size() != courseTime.size()) {
			res.setMessage(StudentCourse_Rtncode.CHECKCOURSELIST.getCode(),
					StudentCourse_Rtncode.CHECKCOURSELIST.getMessage());
			return res;
		}

		// 計算總共學分數
		int calculatePoint = 0;

		// 進入迴圈,並且判斷 1.有無選到相同名稱的課程 2.衝堂 3.大於10學分 4.以上都沒問題,進行學分計算
		Map<String, Object> map = checkCourseIdTimeList(courseTime, calculatePoint);

		// 用key取得到訊息
		// 強制轉型<當res不是空的時候,是錯誤的,固要進入判斷,回傳訊息給用戶>
		res = (CourseRes) map.get("res");

		// 強制轉型<取出學分總和>
		Integer resultTotal = (Integer) map.get("total");

		// 如果Message有東西,他會進來,並且回傳該防呆訊息
		if (StringUtils.hasText(res.getMessage())) {
			return res;
		}
		CoursePeoples peoples = new CoursePeoples();
		CoursePeoples peoples2 = new CoursePeoples();
		// 用於判斷人數上限
		List<CoursePeoples> peoplesListlimit = new ArrayList<>();
//				coursePeoplesDao.findByCourseIdAndCoursePeople(studentCourseId, studentCourseId)。

		List<String> checkcouresidpeople = new ArrayList<>();
		for (var courseItem : courseTime) {
			checkcouresidpeople.add(courseItem.getCourse_id());
		}
		List<String> peopless = new ArrayList<>();
		
		boolean checkIsEmpty = false;
		List<CoursePeoples> peoplesList = coursePeoplesDao.findByCourseIdIn(checkcouresidpeople); // ex:a01,a02課程
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
				for (int j = i + 1; j < peoplesList.size(); j++) { //這裡的迴圈邏輯錯誤
					peoples2 = peoplesList.get(j);
					if (peoples.getCourseId().equals(peoples2.getCourseId())
							&& peoples.getCoursePeople().equals(peoples2.getCoursePeople())) {
						res.setMessage("", "別選同門課程");
						return res;
					} else if (peoples.getCourseId().equals(peoples2.getCourseId())) {
						peoplesListlimit.add(peoples); // 取得該門課的總人數ex:a01:3人/a02:4人
					}
					if (peoplesListlimit.size() > (peoplesList.size() * 5)) {
						res.setMessage("", "人數到達上限");
						return res;
					}
					peoples.setCourseId(peoples.getCourseId());
					peoples.setCoursePeople(studentid);
					coursePeoplesDao.save(peoples);
				}
			}
		}
		// --------------------最後一步
		String newString = list.toString().substring(1, list.toString().length() - 1);
		studentInfo.setCourse_id(newString);
		studentInfo.setCourse_point(resultTotal);

		studentDao.save(studentInfo);
		return new CourseRes(studentInfo, StudentCourse_Rtncode.ADDSUCCESSFUL.getMessage());

	}

}
