package top.xxpblog.easyChat.api.filter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.xxpblog.easyChat.api.annotation.RequiredPermission;
import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import top.xxpblog.easyChat.api.utils.UserLoginUtils;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect {
    @Pointcut("execution(public * top.xxpblog.easyChat.api.controller..*(..))")
    public void BrokerAspect() {

    }

    @ResponseBody
    @Around("BrokerAspect()")
    public Object RequiredPermission(ProceedingJoinPoint jp) throws Throwable {
        Method targetMethod = getSourceMethod(jp);

        if (targetMethod != null) {
            RequiredPermission oper = targetMethod.getAnnotation(RequiredPermission.class);
            if (oper != null) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                // 验证登录
                if (!UserLoginUtils.check(request)) {
                    return ResultVOUtils.error(ResultEnum.LOGIN_VERIFY_FALL);
                }
            }
        }
        return jp.proceed();

    }


    private Method getSourceMethod(JoinPoint jp) {
        Method proxyMethod = ((MethodSignature) jp.getSignature()).getMethod();
        try {
            return jp.getTarget().getClass().getMethod(proxyMethod.getName(), proxyMethod.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
