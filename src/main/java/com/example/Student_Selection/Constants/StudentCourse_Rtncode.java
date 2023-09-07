package com.example.Student_Selection.Constants;

public enum StudentCourse_Rtncode {
	ADDSUCCESSFUL("200", "Add SUCCESSFUL"), 
	UPDATESUCCESSFUL("200", "Update SUCCESSFUL"),
	DELETESUCCESSFUL("200", "successfully delete"),
	TIMECONFLICT("400", "TIMECONFLICT"), 
	CHECKNULL("400", "Can't Null"),
	CHECKLISTISNULL("400","參數值不得為空"),
	CANTFINDCOURSE("400", "Can't find class code"),
	CHECKINFO("400", "Format EX:time 01:00 and week EX:一~五 and point 1 - 3"),
	CHECKTIME("400", "The end time cannot be less than the start time"), 
	CHECKSTUDENT("400", "Can't find this student"),
	CHECKCOURSELIST("400", "Cannot take courses that are no longer on the list"),
	CHECKCOURSENAME("400", "Courses with the same name cannot be selected"),
	CHECKCOURSTIME("400", "class time conflict"), 
	CHECKSTUDENTPOINT("400", "Credits cannot be greater than 10"),
	CHECKSTUDENTCOURSELIST("400", "You cannot withdraw from courses that you do not have");

	private String message;
	private String code;

	private StudentCourse_Rtncode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}