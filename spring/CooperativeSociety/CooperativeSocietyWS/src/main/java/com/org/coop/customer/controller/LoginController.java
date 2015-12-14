package com.org.coop.customer.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coop.org.exception.SecurityQuestionNotSetException;
import com.org.coop.admin.service.AdminLoginService;
import com.org.coop.admin.service.UserProfileAdminServiceImpl;
import com.org.coop.admin.validator.UserValidator;
import com.org.coop.bs.util.BusinessConstants;
import com.org.coop.canonical.beans.UserProfile;
import com.org.coop.constants.WebConstants;
 
@Controller
public class LoginController {
 
	private static final Logger log = Logger.getLogger(LoginController.class); 
	
	@Autowired
	private UserValidator validator;
	
	
	@Autowired
	private UserProfileAdminServiceImpl userProfileAdminService;
	
	@Autowired
	private AdminLoginService adminLoginService;
	
    @RequestMapping(value = { "/", "/home" }, method = RequestMethod.GET)
    public String homePage(ModelMap model) {
        model.addAttribute("greeting", "Hi, Welcome to Cooperative society. ");
        return "welcome";
    }
 
    @RequestMapping(value="/logout", method = RequestMethod.GET)
       public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth != null){    
             new SecurityContextLogoutHandler().logout(request, response, auth);
          }
          return "welcome";
       }
 
    @RequestMapping(value = "/Access_Denied", method = RequestMethod.GET)
    public String accessDeniedPage(ModelMap model) {
        model.addAttribute("user", getPrincipal());
        return "accessDenied";
    }
    
    @RequestMapping(value = "/securityCheck", method = {RequestMethod.POST,RequestMethod.GET})
    public String securityCheck(ModelMap model, RedirectAttributes attr, HttpServletRequest request,
			HttpServletResponse response) {
    	String userName = getPrincipal();

    	try {
	    	String loginOption = adminLoginService.checkOTPOrSecurityQuestion(userName);
	    	UserProfile userProfile = new UserProfile();
	    	userProfile.setUserName(userName);
	    	request.getSession().setAttribute(WebConstants.SV_USER_PROFILE, userProfile);
	    	if(BusinessConstants.RULE_LOGIN_OPTION_ENUM.ONE_STEP_LOGIN.name().equals(loginOption)) {
	            return "redirect:" + WebConstants.DASH_BOARD_URL;
			} else if(BusinessConstants.RULE_LOGIN_OPTION_ENUM.OTP_BASED_LOGIN.name().equals(loginOption)) {
				userProfile.setOtpEnabled(true);
				return WebConstants.OTP_BASED_LOGIN;
			}  else if(BusinessConstants.RULE_LOGIN_OPTION_ENUM.SECURITY_QUESTION_BASED_LOGIN.name().equals(loginOption)) {
				Map<String, String> randomSecurityQuesMap = adminLoginService.getRandomSecurityQuestion(userName);
				
				userProfile.setSecurityQuesionAndAnswer(randomSecurityQuesMap);
				attr.addFlashAttribute(WebConstants.SV_USER_PROFILE, userProfile);
	    		return "redirect:" + WebConstants.SECURITY_QUESTION_BASED_LOGIN;
			}
    	} catch (SecurityQuestionNotSetException e) {
    		log.error(e.getMessage());
    		return "redirect:" + WebConstants.SET_SECURITY_QUESTIONS;
    	}
    	
    	return WebConstants.OTP_BASED_LOGIN;
    }
    
    @RequestMapping(value = "/otp", method = RequestMethod.GET)
    public String loginSuccess(ModelMap model) {
    	if (!model.containsAttribute(WebConstants.SV_USER_PROFILE)) {
    		model.addAttribute(WebConstants.SV_USER_PROFILE, new UserProfile());
    	}
        return WebConstants.OTP_BASED_LOGIN;
    }
    
    @RequestMapping(value = "/resendOTP", method = RequestMethod.GET)
    public String resendOTP(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
        model.addAttribute(WebConstants.SV_USER_PROFILE, new UserProfile());
        String userName = getPrincipal();
        boolean isOTPResend = adminLoginService.resendOTP(userName);
        if(!isOTPResend) {
        	request.getSession().invalidate();
    		log.info("User session destroyed. User is redircted into login page");
    		return WebConstants.LOGIN_PAGE;
        } else {
        	return WebConstants.OTP_BASED_LOGIN;
        }
    }
    
    @RequestMapping(value = "/securityQuestion", method = {RequestMethod.POST,RequestMethod.GET})
    public String securityQuestion(@ModelAttribute(WebConstants.SV_USER_PROFILE) UserProfile userProfile, 
			BindingResult result, Errors errors, Model model,RedirectAttributes attr,
			HttpServletRequest request,
			HttpServletResponse response) {
    	if (!model.containsAttribute(WebConstants.SV_USER_PROFILE)) {
    		model.addAttribute(WebConstants.SV_USER_PROFILE, new UserProfile());
    	} else {
    		model.addAttribute(WebConstants.SV_USER_PROFILE, userProfile);
    		if(userProfile == null || userProfile.getSecurityQuesionAndAnswer() == null) {
    			request.getSession().invalidate();
    			return "redirect:" + WebConstants.LOGOUT_PAGE;
    		}
    		System.out.println(userProfile);
    	}
        return WebConstants.SECURITY_QUESTION_BASED_LOGIN;
    }
    
    @RequestMapping(value = "/verifySecurityQuestion", method = RequestMethod.POST)
    public String verifySecurityQuestion(@ModelAttribute(WebConstants.SV_USER_PROFILE) UserProfile userProfile, 
			BindingResult result, Errors errors, Model model,RedirectAttributes attr,
			HttpServletRequest request,
			HttpServletResponse response) {
    	String userName = getPrincipal();
    	Map<String, String> securityQuesionAndAnswer = new HashMap<String, String>();
		securityQuesionAndAnswer.put("What is the name of your best friend", "ashish");
        boolean isSecurityQuestionMatch = adminLoginService.isSecurityQuestionMatched(userName, securityQuesionAndAnswer);
        if(isSecurityQuestionMatch) {
        	return "redirect:" + WebConstants.DASH_BOARD_URL;
        } else {
        	return "redirect:" + WebConstants.SECURITY_CHECK;
        }
    }
    
    @RequestMapping(value = "/verifyOTP", method = RequestMethod.POST)
    public String verifyOTP(@ModelAttribute(WebConstants.SV_USER_PROFILE) UserProfile userProfile, 
    			BindingResult result, Errors errors, Model model,RedirectAttributes attr,
    			HttpServletRequest request,
    			HttpServletResponse response) {
    	validator.validate(userProfile, errors);
    	if(result.hasErrors()) {
    		attr.addFlashAttribute("org.springframework.validation.BindingResult.userProfile", result);
    		attr.addFlashAttribute(WebConstants.SV_USER_PROFILE, userProfile);
    		return "redirect:" + WebConstants.OTP_BASED_LOGIN;
    	}
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        String userName = auth.getName();
        userProfile.setUserName(userName);
//        userProfile.setOtpEnabled(true);
        boolean isOtpMatched = adminLoginService.isOTPMatched(userName, userProfile.getOtp());
        if(!isOtpMatched) {
        	log.debug("OTP is not matched. Redirecting to the" + WebConstants.SECURITY_CHECK + " page. ");
        	return "redirect:" + WebConstants.SECURITY_CHECK;
        }
        userProfile.setOtpMatch(isOtpMatched);
        
        request.getSession().setAttribute(WebConstants.SV_USER_PROFILE, userProfile);
        String redirectUrl = WebConstants.DASH_BOARD_URL;
        return "redirect:" + redirectUrl;
    }
     
    private String getPrincipal(){
        String userName = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
        if (principal instanceof UserDetails) {
            userName = ((UserDetails)principal).getUsername();
        } else {
            userName = principal.toString();
        }
        return userName;
    }
}