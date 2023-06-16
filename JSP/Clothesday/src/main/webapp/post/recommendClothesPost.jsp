<%@ page language="java" contentType="text/html charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="post.PostDAO" %> 
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@ page trimDirectiveWhitespaces="true" %>

<%	
	post.PostDAO post = new post.PostDAO();	
	post.setPO_TAG(request.getParameter("PO_TAG"));

	response.setContentType("application/json;charset=UTF-8");	

	JSONArray jarray = post.getRecommendPost(post);	
	
	JSONObject jobj = new JSONObject();
	
	if (jarray != null) {
		jobj.put("post", jarray);		
	} else {
		jobj.put("success", false);
	}
	out.print(jobj.toJSONString()); 
	post.closeConnection();
%>