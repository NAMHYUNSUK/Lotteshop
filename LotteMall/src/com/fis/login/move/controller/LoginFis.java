package com.fis.login.move.controller;

import ibsheet.BaseMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fis.carte.lookup.controller.CarteLookupController;
import com.fis.carte.lookup.service.CarteLookupService;
import com.util.JsonUtil;
import com.vo.CarteReserveInfoVO;
import com.vo.CarteVO;


@Controller
@RequestMapping(value = "/fis/login/move")
public class LoginFis {
	
	public LoginFis() {
		// TODO Auto-generated constructor stub
	}
	
	@RequestMapping(value = "/initLogin.moJson")
	public String initLogin(ModelMap model) {
		String returnJsp = "/fis/login/login";
		model.addAttribute("user_name", "조서현");
		return returnJsp;
	}
	
	/* 오늘의 메뉴 부분 관련 소스 입니다 */
	
	@Autowired
	private CarteLookupService carteLookupService;
	
	public static final Logger log = LoggerFactory.getLogger(CarteLookupController.class); // 동작X
	
	/* User 정보 - 나중에 실제 User 객체 있어야 함 */
	String creatorName = "TESTID"; 
	
	@RequestMapping(value = "/getCarteInfo.moJson")
	public String showCarteInfo ( @ModelAttribute("carteVO") CarteVO carteVO,
				HttpServletRequest req, HttpServletResponse res, ModelMap model) {
					
		/* 현재 사용X top */
		Map etcData = new HashMap(); // 추가적으로 전달할 정보가 있을 때 사용
		boolean isSuccess = true;
		BaseMap dateAndReserveInfo = new BaseMap();
		carteVO = new CarteVO();
		/* 현재 사용X bottom */
		
		dateAndReserveInfo.put("presentYear", getPresentYear());
		dateAndReserveInfo.put("presentMonth", getPresentMonth());
		dateAndReserveInfo.put("presentMon", getDayDate(0)); // 월
		dateAndReserveInfo.put("presentTue", getDayDate(1)); 
		dateAndReserveInfo.put("presentWed", getDayDate(2));
		dateAndReserveInfo.put("presentThu", getDayDate(3));
		dateAndReserveInfo.put("presentFri", getDayDate(4)); // 금
		dateAndReserveInfo.put("presentDate", getPresentDate());
		
		List<BaseMap> selectedCarteList = null; // 실제 select된 식단 정보
		String reserveInfo;
		Integer reserveCount;
		
		String[] reserveKeyName = new String[]{"reserve_yn_mon_dn", "reserve_yn_tue_dn"
				, "reserve_yn_wed_dn" ,"reserve_yn_thu_dn", "reserve_yn_fri_dn"};  
		
		String[] reserveCountKeyName = new String[]{"reserve_count_mon_dn", "reserve_count_tue_dn"
				, "reserve_count_wed_dn" ,"reserve_count_thu_dn", "reserve_count_fri_dn"};  
		
		try{
			/* 실제로 select 하는 부분 */
			selectedCarteList = carteLookupService.selectCarteList(getPresentStartDateStr(), getPresentEndDateStr());
			
			for(int i = 0; i < 5; i++) { // 사용자의 예약 여부 정보 화면에 넘겨준다
				reserveInfo = null;
				reserveInfo = carteLookupService.selectReserveList(getDateStr(i), "DN", creatorName);
				if(reserveInfo == null) dateAndReserveInfo.put(reserveKeyName[i], "null"); // 예약 정보 없음
				if(reserveInfo != null) dateAndReserveInfo.put(reserveKeyName[i], reserveInfo); // 예약 정보 있음
			}
			
			for(int i = 0; i < 5; i++) { // 예약 인원 수 화면에 넘겨준다
				reserveCount = null;
				reserveCount = carteLookupService.selectReserveCount(getDateStr(i), "DN");
				if(reserveCount == null || reserveCount == 0) 
					dateAndReserveInfo.put(reserveCountKeyName[i], "0"); // 예약 인원 없음
				else 
					dateAndReserveInfo.put(reserveCountKeyName[i], reserveCount); // 예약 인원 있음
			}
			
			dateAndReserveInfo.put("reserve_id", getPresentDayName());
			
			if (selectedCarteList.isEmpty() ) {
				System.out.println("기존에 입력된 식단 정보 없음");
			} else {
				System.out.println("기존에 입력된 식단 정보 있음");
			}
			
		} catch (Exception e){
			log.error("getCarteInfo.moJson" , e);
			System.out.println(e);
			isSuccess = false;
		}
		
		JsonUtil.setReturnTable(req, isSuccess, "success getCarteInfo.moJson", "error getCarteInfo.moJson", 
				"mySheet", selectedCarteList, dateAndReserveInfo, etcData);
		
		String returnJsp = "common/ajax/json";
		return returnJsp;
		
	}
	
	@RequestMapping(value = "/getReserveInfo.moJson") 
	public String getReserveInfo ( CarteReserveInfoVO carteReserveInfoVO,
			HttpServletRequest req, HttpServletResponse res, ModelMap model) {
		
		/* 현재 사용X top */
		Map etcData = new HashMap(); // 추가적으로 전달할 정보가 있을 때 사용
		boolean isSuccess = true;
		BaseMap dateAndReserveInfo = new BaseMap();
		List<BaseMap> emptyList = new ArrayList<BaseMap>();
		/* 현재 사용X bottom */

		String reserveInfo; // 예약 여부 Yes or No
		Integer reserveCount; // 예약 수
		
		String selectedDate = req.getParameter("selected_date");
		String empID = req.getParameter("emp_id");
		String carteType = req.getParameter("carte_type");
		String dayName = req.getParameter("day_name");
		
		try{

			// reserve 했는지 안했는지
			reserveInfo = null;
			reserveInfo = carteLookupService.selectReserveList(selectedDate, carteType, empID);
			
			if(reserveInfo == null) dateAndReserveInfo.put("reserve_yn", "null"); // 예약 정보 없음
			if(reserveInfo != null) dateAndReserveInfo.put("reserve_yn", reserveInfo); // 예약 정보 있음
			
			// reserve 인원 수
			reserveCount = null;
			reserveCount = carteLookupService.selectReserveCount(selectedDate, carteType);
			
			if(reserveCount == null || reserveCount == 0) 
				dateAndReserveInfo.put("reserve_count", "0"); // 예약 인원 없음
			else 
				dateAndReserveInfo.put("reserve_count", reserveCount); // 예약 인원 있음
			
			dateAndReserveInfo.put("reserve_id", creatorName);
			dateAndReserveInfo.put("carte_type", carteType);
			dateAndReserveInfo.put("day_name", dayName);
			
			if (emptyList.isEmpty()) {
				System.out.println("Empty");
			} else {
				System.out.println("Not empty");
			}
			
		} catch (Exception e){
			log.error("getReserveInfo.moJson" , e);
			System.out.println(e);
			isSuccess = false;
		}
		
		JsonUtil.setReturnTable(req, isSuccess, "success getReserveInfo.moJson", "error getReserveInfo.moJson", 
				"mySheet", emptyList, dateAndReserveInfo, etcData);
		
//		dateArrayList.clear();
		
		String returnJsp = "common/ajax/json";
		return returnJsp;
		
	}
	
	@RequestMapping(value = "/insertReservation.moJson") 
	public String reserveCarte ( CarteReserveInfoVO carteReserveInfoVO,
			HttpServletRequest req, HttpServletResponse res, ModelMap model) {
				
		/*
		// test 데이터
		carteReserveInfoVO = new CarteReserveInfoVO();
		carteReserveInfoVO.setDate("20160201");
		carteReserveInfoVO.setCarte_type("DN");
		carteReserveInfoVO.setEmp_id(creatorName);
		carteReserveInfoVO.setReserve_yn("Y");
		*/
		
        boolean isSuccess = true;
        Map<String, String> data = new HashMap();
		
		carteReserveInfoVO = new CarteReserveInfoVO();
		
		carteReserveInfoVO.setDate(req.getParameter("date"));
		carteReserveInfoVO.setCarte_type(req.getParameter("carte_type"));
		carteReserveInfoVO.setEmp_id(creatorName);
		carteReserveInfoVO.setReserve_yn(req.getParameter("reserve_yn"));
		
		/* 실제로 insert 하는 부분 */
		try{
			carteLookupService.insertReservation(carteReserveInfoVO);
		} catch (Exception e){
			System.out.println(e);
			isSuccess = false;
		}
		
		JsonUtil.getJsonStructure(req , isSuccess, "success.insert.reservation", "error.insert.reservation", data);
		
		// String returnJsp = "/fis/carte/lookup_carte";
		String returnJsp = "common/ajax/json";
		return returnJsp;
		
	}

	public int getPresentYear() {
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		
		int presentYear = presentMonCal.getInstance().get(Calendar.YEAR);
		return presentYear;
		
	}
	
	public int getPresentMonth() {
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		
		int presentMonth = presentMonCal.getInstance().get(Calendar.MONTH) + 1;
		return presentMonth;
		
	}
	
	private Calendar getNextMonCalendar() {
		
		Calendar nextMonCal = Calendar.getInstance();
		
		int weekday = nextMonCal.get(Calendar.DAY_OF_WEEK);
		
		if (weekday != Calendar.MONDAY)
		{
		    int days = (Calendar.SATURDAY - weekday + 2) % 7;
		    nextMonCal.add(Calendar.DAY_OF_YEAR, days);
		    //System.out.println(nextMonCal.getTime());
		}
		
		return nextMonCal;
		
	}
	
	public String nextWeekText() { // 무조건 다음주 
		
		String nextWeek = "default";
		
		Calendar nextMonCal = getNextMonCalendar();
		
		/* 화면에 찍히는 요일 */
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd E요일");
		
		String startDateStr = dateFormat.format(nextMonCal.getTime());
		//System.out.println(startDateStr);
		
		nextMonCal.add(Calendar.DAY_OF_YEAR, 4);
		String endDateStr = dateFormat.format(nextMonCal.getTime());
		//System.out.println(endDateStr);
		
		nextWeek = startDateStr + " ~ " + endDateStr;
		
		return nextWeek;
		
	}
	
	//public String getEactDate(String fullName) {
	public String getEactDate(String fullName) {
		
		//TEST
		//String fullName = "friL2One";
		
		String strEachDate = "";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar nextMonCal = getNextMonCalendar();
		
		//nextMonCal.add(Calendar.DAY_OF_YEAR, 7);
		
		if(fullName.matches(".*mon.*")) { // 월요일인 경우
			//nextMonCal.add(Calendar.DAY_OF_YEAR, 4);
			strEachDate =  dateFormat.format(nextMonCal.getTime());
		}
		else if(fullName.matches(".*tue.*")) {
			nextMonCal.add(Calendar.DAY_OF_YEAR, 1);
			strEachDate =  dateFormat.format(nextMonCal.getTime());
		}
		else if(fullName.matches(".*wed.*")) {
			nextMonCal.add(Calendar.DAY_OF_YEAR, 2);
			strEachDate =  dateFormat.format(nextMonCal.getTime());
		}
		else if(fullName.matches(".*thu.*")) {
			nextMonCal.add(Calendar.DAY_OF_YEAR, 3);
			strEachDate =  dateFormat.format(nextMonCal.getTime());
		}
		else if(fullName.matches(".*fri.*")) {
			nextMonCal.add(Calendar.DAY_OF_YEAR, 4);
			strEachDate =  dateFormat.format(nextMonCal.getTime());
		}
		
		return strEachDate;
		
	}

	public String getPresentStartDateStr() {
		
		String strStartDate = "";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 월요일로 set
		
		strStartDate =  dateFormat.format(presentMonCal.getTime());
		
		return strStartDate;
		
	}
	
	public String getDateStr(int increment) {
		
		String strDate = "";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 월요일로 set
		presentMonCal.add(Calendar.DAY_OF_YEAR, increment);
		
		strDate =  dateFormat.format(presentMonCal.getTime());
		
		return strDate;
		
	}
	
	public String getPresentEndDateStr() {
		
		String strEndDate = "";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 월요일로 set
		
		presentMonCal.add(Calendar.DAY_OF_YEAR, 4);
		strEndDate =  dateFormat.format(presentMonCal.getTime());
		
		return strEndDate;
		
	}
	
	public int getPresentStartDate() {
		
		int startDate = 0;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 월요일로 set
		
		startDate =  Integer.valueOf(dateFormat.format(presentMonCal.getTime()));
		
		return startDate;
		
	}
	
	public int getDayDate(int day) { // 요일별 날짜 반환 (0~4 : 월~금)
		
		int date = 0;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
		
		Calendar calDate = Calendar.getInstance();
		calDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 월요일로 set
		calDate.add(Calendar.DAY_OF_YEAR, day);
		
		date =  Integer.valueOf(dateFormat.format(calDate.getTime()));
		
		return date;
		
	}
	
	public int getPresentDate() {
		
		Calendar presentMonCal = Calendar.getInstance();
		presentMonCal.add(Calendar.DAY_OF_YEAR, 4);
		int presentDate = presentMonCal.getInstance().get(Calendar.DATE);
		return presentDate;
		
	}
	
	private String getPresentDayName() {
		
		Calendar presentDayCal = Calendar.getInstance();
		
		int weekday = presentDayCal.get(Calendar.DAY_OF_WEEK);
		
		// 무조건 이번주로 계산
		switch (weekday) { 
		
		case 7: // 토요일
			return "sat";
			
		case 1: // 일요일
			return "sun";
					
		case 2: // 월요일
			return "mon";
			
		case 3: // 화요일
			return "tue";
			
		case 4: // 수요일
			return "wed";
			
		case 5: // 목요일
			return "thu";
			
		case 6: // 금요일
			return "fri";

		default:
			return "error";
			
		}
		
	}
	
}
