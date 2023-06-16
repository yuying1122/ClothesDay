<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="user.UserDAO" %> 
<%@ page trimDirectiveWhitespaces="true" %>

<%
	String ME_ID = request.getParameter("UserId");
	String ME_VERIFY = request.getParameter("ME_VERIFY");
	UserDAO auser = new UserDAO();
	
	boolean success = auser.checkId(ME_ID, ME_VERIFY);
			
	auser.closeConnection();		
%><%=success%>