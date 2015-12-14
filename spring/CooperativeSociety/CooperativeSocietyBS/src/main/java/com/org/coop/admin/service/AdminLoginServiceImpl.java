package com.org.coop.admin.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coop.org.exception.SecurityQuestionNotSetException;
import com.org.coop.bs.util.BusinessConstants;
import com.org.coop.bs.util.BusinessConstants.RULE_LOGIN_OPTION_ENUM;
import com.org.coop.society.data.admin.entities.BranchRule;
import com.org.coop.society.data.admin.entities.RolePermission;
import com.org.coop.society.data.admin.entities.User;
import com.org.coop.society.data.admin.entities.UserRole;
import com.org.coop.society.data.admin.entities.UserSequrityQuestion;
import com.org.coop.society.data.admin.repositories.UserAdminRepository;

@Service
public class AdminLoginServiceImpl implements AdminLoginService {
	private static final Logger log = Logger.getLogger(AdminLoginServiceImpl.class); 

	@Autowired
	private UserAdminRepository userAdminRepository;
	
	@Autowired
	private CommonAdminServiceImpl commonAdminServiceImpl;
	
	/**
	 * This method returns list of roles for a given user name
	 */
	@Transactional(value="adminTransactionManager")
	public List<String> getRole(String username) {
		List<String> roleList = new ArrayList<String>();
		User user = userAdminRepository.findByUserName(username);
		if(user != null) {
			for(UserRole userRole : user.getUserRoles()) {
				roleList.add(userRole.getRoleMaster().getRoleName().toUpperCase());
			}
		}
		return roleList;
	}
	
	/**
	 * This method returns list of permissions for a given user name
	 */
	@Transactional(value="adminTransactionManager")
	public List<String> getRolePermissions(String username) {
		List<String> permissionList = new ArrayList<String>();
		User user = userAdminRepository.findByUserName(username);
		if(user != null) {
			for(UserRole userRole : user.getUserRoles()) {
				for(RolePermission rolePerm : userRole.getRoleMaster().getRolePermissions()) {
					String permission = rolePerm.getPermissionMaster().getModuleMaster().getModuleName() + "_" + rolePerm.getPermissionMaster().getPermission();
					permissionList.add(permission);
				}
			}
		}
		return permissionList;
	}
	
	/**
	 * Once login is successful this method resets the counter
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public void successfulLogin(String username) {
		if(username != null) {
			User user = userAdminRepository.findByUserName(username);
			user.getUserCredential().setUnsuccessfulLoginCount(0);
			user.getUserCredential().setLastLogin(new Timestamp(System.currentTimeMillis()));
			user.getUserCredential().setUpdateUser(username);
		}
	}
	
	/**
	 * Once login is successful this method resets the counter
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public void unsuccessfulLogin(String username) {
		if(username != null) {
			User user = userAdminRepository.findByUserName(username);
			int unsuccessfulLoginCounter = user.getUserCredential().getUnsuccessfulLoginCount();
			user.getUserCredential().setUnsuccessfulLoginCount(++unsuccessfulLoginCounter);
			user.getUserCredential().setLastUnsuccessfulLogin(new Timestamp(System.currentTimeMillis()));
			user.getUserCredential().setUpdateUser(username);
		}
	}
	
	
	/**
	 * Once login is successful this method will set OTP
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public void setOTP(String username) {
		User user = userAdminRepository.findByUserName(username);
		long expTime = user.getUserCredentialOtp().getEndDate().getTime();
		long currentTime = System.currentTimeMillis();
		
		if(currentTime - expTime < BusinessConstants.OTP_VALIDITY) {
			log.debug("OTP has been generated within : " + BusinessConstants.OTP_VALIDITY + " ms for user: " + username);
			return;
		}
		
		Random rnd = new Random();
		int otp = 100000 + rnd.nextInt(900000);
		user.getUserCredentialOtp().setOtp(String.valueOf(otp));
		user.getUserCredentialOtp().setStartDate(new Timestamp(System.currentTimeMillis()));
		user.getUserCredentialOtp().setEndDate(new Timestamp(System.currentTimeMillis() + (BusinessConstants.OTP_VALIDITY)));
	}
	
	/**
	 * Once login is successful this method will validate entered OTP
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public boolean isOTPMatched(String username, String otp) {
		User user = userAdminRepository.findByUserName(username);
		long expTime = user.getUserCredentialOtp().getEndDate().getTime();
		long currentTime = System.currentTimeMillis();
		
		if(currentTime - expTime > BusinessConstants.OTP_VALIDITY) {
			log.debug("OTP has expired for user: " + username);
			return false;
		}
		if(otp != null && user.getUserCredentialOtp().getOtp().equals(otp)) {
			user.getUserCredential().setUnsuccessfulLoginCount(0);
			log.debug("OTP has matched for user: " + username);
			return true;
		} else {
			unsuccessfulLogin(username);
			log.debug("OTP does not for user: " + username);
			return false;
		}
	}
	
	
	/**
	 * OTP will be resent based on the maximum threshold value subscribed by the branch. 
	 * If it reaches threshold value then it will return false.
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public boolean resendOTP(String username) {
		User user = userAdminRepository.findByUserName(username);
		int noOfTimesOtpToBeResend = BusinessConstants.NUMBER_OF_TIMES_OTP_TO_BE_RESEND_VAL;
		
		String ruleVal = commonAdminServiceImpl.getBranchRuleValueByKey(username, BusinessConstants.RULE_NUMBER_OF_TIMES_OTP_TO_BE_RESEND);
		noOfTimesOtpToBeResend = Integer.valueOf(ruleVal);
		
		int otpResendCounter = user.getUserCredentialOtp().getOtpResendCounter();
		if(otpResendCounter < noOfTimesOtpToBeResend) {
			user.getUserCredentialOtp().setOtpResendCounter(++otpResendCounter);
			return true;
		} else {
			user.getUserCredentialOtp().setOtpResendCounter(0);
		}
		return false;
	}
	
	/**
	 * This method decides if single step login/otp based/security based question is subscribed for the branch 
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public String checkOTPOrSecurityQuestion(String username) throws SecurityQuestionNotSetException {
		String loginOption = commonAdminServiceImpl.getBranchRuleValueByKey(username, BusinessConstants.RULE_LOGIN_OPTION);
		log.debug("Login option: " + loginOption);
		
		if(BusinessConstants.RULE_LOGIN_OPTION_ENUM.OTP_BASED_LOGIN.name().equals(loginOption)) {
			setOTP(username);
		} else if(BusinessConstants.RULE_LOGIN_OPTION_ENUM.SECURITY_QUESTION_BASED_LOGIN.name().equals(loginOption)) {
			// Check if security question is set for the user.
			User user = userAdminRepository.findByUserName(username);
			List<UserSequrityQuestion> userSecQues = user.getUserSequrityQuestions();
			if(userSecQues == null || userSecQues.size() == 0) {
				log.debug("Security question is not set for the user : " + username);
				throw new SecurityQuestionNotSetException("Security question is not set for the user : " + username);
			}
		}
		return loginOption;
	}
	
	/**
	 * Once login is successful this method will validate entered Security questions
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public boolean isSecurityQuestionMatched(String username, Map<String, String> securityQuesionAndAnswer) {
		if(securityQuesionAndAnswer == null || securityQuesionAndAnswer.size() == 0) {
			log.debug("User has not answered");
			return false;
		}
		User user = userAdminRepository.findByUserName(username);
		List<UserSequrityQuestion> userSecQues = user.getUserSequrityQuestions();
		if(userSecQues != null && userSecQues.size() > 0 ) {
			for(UserSequrityQuestion secQues : userSecQues) {
				String ques = secQues.getSecurityQuestion().getQuestion();
				String dbAnswer = secQues.getAnswer();
				String userAnswer = securityQuesionAndAnswer.get(ques);
				if(dbAnswer.equals(userAnswer)) {
					log.debug("User answer matches");
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Once login is successful this method will get random question from the questions have been set
	 * @param username
	 */
	@Transactional(value="adminTransactionManager")
	public Map<String, String> getRandomSecurityQuestion(String username) {
		Map<String, String> securityQuesAnswer = new HashMap<String, String>();
		User user = userAdminRepository.findByUserName(username);
		List<UserSequrityQuestion> userSecQues = user.getUserSequrityQuestions();
		if(userSecQues != null && userSecQues.size() > 0) {
			Random rnd = new Random();
			int randomNum = 100 + rnd.nextInt(900);
			randomNum = randomNum % userSecQues.size();
			UserSequrityQuestion secQues = userSecQues.get(randomNum);
			securityQuesAnswer.put(secQues.getSecurityQuestion().getQuestion(), secQues.getAnswer());
		}
		return securityQuesAnswer;
	}
}