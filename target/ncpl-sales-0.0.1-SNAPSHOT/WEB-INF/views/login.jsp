<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>


<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<%
request.getSession().invalidate();
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title><tiles:insertAttribute name="title" /></title>
	<tiles:insertAttribute name="header-resources" />
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
	<style>
body {
			font-family: 'Poppins', 'Segoe UI', sans-serif;
			background: radial-gradient(circle at top, #f7f9ff, #e7efff 55%, #fefefe 100%);
			min-height: 100vh;
			margin: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			color: #162447;
		}

		.login-shell {
			width: 100%;
			max-width: 960px;
			display: grid;
			grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
			gap: 0;
			background: rgba(255, 255, 255, 0.9);
			border-radius: 28px;
			overflow: hidden;
			box-shadow: 0 35px 90px rgba(20, 37, 78, 0.15), inset 0 0 1px rgba(20, 37, 78, 0.05);
			backdrop-filter: blur(8px);
		}

		.brand-panel {
			position: relative;
			padding: 56px 48px;
			background: linear-gradient(135deg, #eef3ff, #fdf7ed);
			color: #1f2a44;
		}

		.brand-panel::after {
			content: '';
			position: absolute;
			inset: 20px;
			border-radius: 22px;
			border: 1px solid rgba(14, 41, 84, 0.12);
			pointer-events: none;
		}

		.brand-panel h1 {
			font-size: 2.2rem;
			font-weight: 600;
			margin-bottom: 1.2rem;
		}

		.brand-panel p {
			font-size: 0.98rem;
			line-height: 1.6;
			color: #4c5972;
		}

		.brand-badges {
			display: flex;
			gap: 12px;
			flex-wrap: wrap;
			margin-top: 24px;
		}

		.brand-badges span {
			font-size: 0.78rem;
			text-transform: uppercase;
			letter-spacing: 1px;
			padding: 6px 14px;
			border-radius: 99px;
			border: 1px solid rgba(46, 69, 115, 0.2);
			color: #32405d;
			background: rgba(255, 255, 255, 0.9);
		}

		.form-panel {
			padding: 48px;
			background: #ffffff;
		}

		.panel-heading {
			margin-bottom: 32px;
		}

		.panel-heading h2 {
			font-size: 1.65rem;
			margin: 0 0 0.35rem;
			color: #121f3f;
		}

		.panel-heading p {
			color: #7d869f;
			margin: 0;
		}

		.form-control,
		.input-group-text {
			background: #f7f9fd;
			border: 1px solid #cad6f5;
			color: #1f2a44;
		}

		.form-control:focus {
			border-color: #4a7dff;
			box-shadow: 0 0 0 0.2rem rgba(74, 125, 255, 0.15);
			background: #ffffff;
		}

		.btn-primary {
			border: none;
			background: linear-gradient(135deg, #4a7dff, #8ed4ff);
			font-weight: 600;
			padding: 0.8rem 0;
			color: #fff;
		}

		.btn-primary:hover {
			opacity: 0.95;
		}

		#errorBanner {
			margin-bottom: 18px;
			border-radius: 10px;
			padding: 12px 16px;
			display: none;
			background: rgba(255, 166, 173, 0.16);
			border: 1px solid rgba(255, 115, 128, 0.35);
			color: #c62840;
			font-size: 0.9rem;
		}

		#errorBanner.active {
			display: block;
		}

		.meta-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-top: 24px;
			font-size: 0.85rem;
			color: #6d7898;
		}

		@media (max-width: 768px) {
			body {
				padding: 16px;
			}

			.brand-panel {
				text-align: center;
			}

			.brand-panel::after {
				inset: 14px;
			}
		}
	</style>
</head>
<body>
	<div class="login-shell">
		<section class="brand-panel">
			<h1>ERP Portal</h1>
			<p>Unlock inventory insights, manage orders, and keep your teams aligned with a single secured entrance.</p>
			<!-- <div class="brand-badges">
				<span>Secure</span>
				<span>24x7 Access</span>
				<span>Role-based</span>
			</div> -->
		</section>
		<section class="form-panel">
			<div class="panel-heading">
				<h2>Welcome back</h2>
				<p>Sign in to start your session</p>
			</div>
			<div id="errorBanner" class="${not empty error ? 'active' : ''}">
				<span>${error}</span>
			</div>
			<form action="${pageContext.request.contextPath}/login" method="post" role="form" id="loginForm">
				<div class="input-group mb-4">
					<div class="input-group-prepend">
						<div class="input-group-text">
							<span class="fas fa-user"></span>
						</div>
					</div>
					<input type="text" name="username" class="form-control" placeholder="User Name" autocomplete="username" required>
				</div>
				<div class="input-group mb-4">
					<div class="input-group-prepend">
						<div class="input-group-text">
							<span class="fas fa-lock"></span>
						</div>
					</div>
					<input type="password" name="password" class="form-control" placeholder="Password" autocomplete="current-password" required>
				</div>
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
				<button type="submit" class="btn btn-primary btn-block">Sign In</button>
				<!-- <div class="meta-row">
					<span>Need help? Contact support@ncpl.in</span>
					<span>&copy; ${pageContext.request.serverName}</span>
				</div> -->
			</form>
		</section>
	</div>
</body>


</body>
</html>
