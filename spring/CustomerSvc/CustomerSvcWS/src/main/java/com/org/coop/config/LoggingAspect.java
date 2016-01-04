package com.org.coop.config;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
public class LoggingAspect {
	private static final Logger log = Logger.getLogger(LoggingAspect.class);

//	@Before("execution(* com.org.coop.controller..*(..)) || execution(* com.org.coop.service..*(..))")
//	public void logBefore(JoinPoint joinPoint) {
//		log.debug(">>>>>>>>>>" + joinPoint.getSignature().toShortString() + " START >>>>>>>>>>>>");
//	}
	
	@Around("execution(* com.org.coop.customer.controller..*(..)) || "
			+ "execution(* com.org.coop.security.service..*(..)) || "
			+ "execution(* com.org.coop.customer.service..*(..))")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        log.debug(">>>>>>>>>>" + pjp.getSignature().toShortString() + " START >>>>>>>>>>>>");
        Object retVal = pjp.proceed();
        long end = System.currentTimeMillis();
        log.debug("[" + pjp.getSignature().toShortString() + "] method Execution Time: " + (end - start) + " ms.");
        log.debug( "<<<<<<<<" + pjp.getSignature().toShortString() + " END <<<<<<<<");
        return retVal;
    }

//	@After("execution(* com.org.coop.controller..*(..)) || execution(* com.org.coop.service..*(..))")
//	public void logAfter(JoinPoint joinPoint) {
//		log.debug( "<<<<<<<<" + joinPoint.getSignature().toShortString() + " END <<<<<<<<");
//	}
}
