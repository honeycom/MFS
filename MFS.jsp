<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>

<h3>--MFS Server DB---</h3>

<%
request.setCharacterEncoding("euc-kr");
String data = request.getParameter("motion");
String device="";
String motion="";
String x="";
String y="";
String date="";
if(data!=null){
	String[] data2=data.split("/");
	device=data2[0];
	motion=data2[1];
	x=data2[2];
	y=data2[3];
	date=data2[4];}

	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	try{
		ServletContext sc = this.getServletContext();
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/MFSdb",
					"root",
					"1234"); 
		stmt = conn.createStatement();
		
		if(data!=null)
		stmt.executeUpdate("insert into MFStable values ('"+device+"', '"+motion+"', '"+x+"', '"+y+"', '"+date+"');");
		
		rs  = stmt.executeQuery("select * from MFStable;");
		while(rs.next()){
			out.println(rs.getString("User")+" / "+rs.getString("Motion")+" / ("+rs.getString("x")+", "+rs.getString("y")+") / "+rs.getString("time")+"</br>");
		}
	}catch (Exception e) {
		throw new ServletException(e);
		
	} finally {
		try {if (rs != null) rs.close();} catch(Exception e) {}
		try {if (stmt != null) stmt.close();} catch(Exception e) {}
		try {if (conn != null) conn.close();} catch(Exception e) {}
	}
	System.out.println("Server >>"+device+":"+motion);
%>



<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>

<h3>--MFS Server DB---</h3>

<%
request.setCharacterEncoding("euc-kr");
String data = request.getParameter("motion");
String device="";
String motion="";
String x="";
String y="";
String date="";
if(data!=null){
	String[] data2=data.split("/");
	device=data2[0];
	motion=data2[1];
	x=data2[2];
	y=data2[3];
	date=data2[4];}

	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	try{
		ServletContext sc = this.getServletContext();
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/MFSdb",
					"root",
					"1234"); 
		stmt = conn.createStatement();
		
		if(data!=null)
		stmt.executeUpdate("insert into MFStable values ('"+device+"', '"+motion+"', '"+x+"', '"+y+"', '"+date+"');");
		
		rs  = stmt.executeQuery("select * from MFStable;");
		while(rs.next()){
			out.println(rs.getString("User")+" / "+rs.getString("Motion")+" / ("+rs.getString("x")+", "+rs.getString("y")+") / "+rs.getString("time")+"</br>");
		}
	}catch (Exception e) {
		throw new ServletException(e);
		
	} finally {
		try {if (rs != null) rs.close();} catch(Exception e) {}
		try {if (stmt != null) stmt.close();} catch(Exception e) {}
		try {if (conn != null) conn.close();} catch(Exception e) {}
	}
	System.out.println("Server >>"+device+":"+motion);
%>



<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>

<h3>--MFS Server DB---</h3>

<%
request.setCharacterEncoding("euc-kr");
String data = request.getParameter("motion");
String device="";
String motion="";
String x="";
String y="";
String date="";
if(data!=null){
	String[] data2=data.split("/");
	device=data2[0];
	motion=data2[1];
	x=data2[2];
	y=data2[3];
	date=data2[4];}

	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	try{
		ServletContext sc = this.getServletContext();
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/MFSdb",
					"root",
					"1234"); 
		stmt = conn.createStatement();
		
		if(data!=null)
		stmt.executeUpdate("insert into MFStable values ('"+device+"', '"+motion+"', '"+x+"', '"+y+"', '"+date+"');");
		
		rs  = stmt.executeQuery("select * from MFStable;");
		while(rs.next()){
			out.println(rs.getString("User")+" / "+rs.getString("Motion")+" / ("+rs.getString("x")+", "+rs.getString("y")+") / "+rs.getString("time")+"</br>");
		}
	}catch (Exception e) {
		throw new ServletException(e);
		
	} finally {
		try {if (rs != null) rs.close();} catch(Exception e) {}
		try {if (stmt != null) stmt.close();} catch(Exception e) {}
		try {if (conn != null) conn.close();} catch(Exception e) {}
	}
	System.out.println("Server >>"+device+":"+motion);
%>



