package com.ljozefowicz.battleships.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@Order(2)
public class SessionLastAccessTimeUpdateFilter implements Filter
{

    Logger OUT = LoggerFactory.getLogger(SessionLastAccessTimeUpdateFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        OUT.trace("Last access time update filter :: {} :: {}", session, getFullURL(request));

        if (session != null)
        {
            session.setAttribute("lastAccessTime", System.currentTimeMillis());
        }
        chain.doFilter(request, response);
    }

    private static String getFullURL(ServletRequest req)
    {
        HttpServletRequest request = (HttpServletRequest) req;
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();

        if (queryString == null)
        {
            return requestURL.toString();
        }
        else
        {
            return requestURL.append('?').append(queryString).toString();
        }
    }

}
