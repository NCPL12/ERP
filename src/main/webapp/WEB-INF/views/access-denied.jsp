<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Access Denied</title>
<style>
body {
	margin: 0;
	height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f4f6f9;
	font-family: -apple-system, "Segoe UI", Roboto, Arial, sans-serif;
}
.ad-box {
	background: #fff;
	border: 1px solid #e5e7eb;
	border-radius: 8px;
	padding: 40px 44px;
	max-width: 420px;
	text-align: center;
	box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.ad-icon {
	font-size: 2.5rem;
	color: #e11d48;
	margin-bottom: 12px;
}
.ad-box h1 {
	font-size: 1.2rem;
	margin: 0 0 8px;
	color: #111827;
}
.ad-box p {
	color: #6b7280;
	font-size: 0.9rem;
	margin: 0 0 24px;
}
.ad-actions a {
	display: inline-block;
	padding: 8px 18px;
	border-radius: 5px;
	font-size: 0.85rem;
	text-decoration: none;
	margin: 0 6px;
}
.ad-btn-primary {
	background: #2563eb;
	color: #fff;
}
.ad-btn-outline {
	background: #fff;
	color: #111827;
	border: 1px solid #d1d5db;
}
</style>
</head>
<body>
	<div class="ad-box">
		<div class="ad-icon">&#9888;</div>
		<h1>Access Denied</h1>
		<p>You don't have permission to view this page. If you think this is a mistake, contact your administrator.</p>
		<div class="ad-actions">
			<a class="ad-btn-primary" href="${pageContext.request.contextPath}/dashboard">Go Home</a>
			<a class="ad-btn-outline" href="${pageContext.request.contextPath}/logout">Logout</a>
		</div>
	</div>
</body>
</html>
