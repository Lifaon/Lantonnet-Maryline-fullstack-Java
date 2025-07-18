package com.paymybuddy.app.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    static public void logRequestResponse(Logger logger, HttpServletRequest request, HttpServletResponse response) {

        int status = response.getStatus();

        String s_info = status + ": " + request.getMethod();
        s_info += " " + request.getRequestURI();
        if (request.getQueryString() != null) {
            s_info += "?" + request.getQueryString();
        }

        if (status < 400)
            logger.info(s_info);
        else
            logger.error(s_info);
    }

    static private Logger _getLogger(@NotNull Object object) {
        if (object instanceof HandlerMethod) {
            final Class<?> controllerClass = ((HandlerMethod) object).getBeanType();
            return LogManager.getLogger(controllerClass);
        }
        return LogManager.getLogger();
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object object) {

        if (object instanceof HandlerMethod) {
            final String methodName = ((HandlerMethod) object).getMethod().getName();
            _getLogger(object).debug("Call '{}()'", methodName);
        }
        return true;
    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView model) {
//        System.out.println("postHandle");
//    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object object, Exception exception) {

        Logger logger = _getLogger(object);

        if (exception != null) {
            response.setStatus(500);
            logger.error(exception);
        }

        logRequestResponse(logger, request, response);

    }
}