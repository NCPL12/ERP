package com.ncpl.sales.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	/*Logger log = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Pointcut(value="execution(* com.ncpl.sales.service.*.*(..) )")
	public void myPointcut() {
		
	}
	@Around("myPointcut()")
	public Object applicationLogger(ProceedingJoinPoint pjp) throws Throwable {
		ObjectMapper mapper = new ObjectMapper();
		String methodName = pjp.getSignature().getName();
		String className = pjp.getTarget().getClass().toString();
		Object[] array = pjp.getArgs();
		log.info(" Method Invoked "+ className +" : " + methodName +"()"+" argumnets "+ mapper.writeValueAsString(array));
		Object object = pjp.proceed();
		log.info(className+":"+methodName+"()"+"Response"+mapper.writeValueAsString(object));
		return object;
	}
*/
}
