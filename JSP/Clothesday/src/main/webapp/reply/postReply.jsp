<%@ page language="java" contentType="text/html charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="reply.ReplyDAO" %> 
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@ page trimDirectiveWhitespaces="true" %>

<%
	ReplyDAO reply = new ReplyDAO();	
	try {
		reply.setRE_PO_ID(Integer.parseInt(request.getParameter("RE_PO_ID")));		
	} catch (Exception ex) {
		ex.printStackTrace();
	}

	response.setContentType("application/json;charset=UTF-8");
	
	JSONArray jarray = reply.getPostReply(reply);	
	
	JSONObject jobj = new JSONObject();	
	
	if (jarray != null) {
		jobj.put("reply", jarray);
	} else {
		jobj.put("success", false);		
	}
	out.print(jobj.toJSONString()); 
	reply.closeConnection();
%>


