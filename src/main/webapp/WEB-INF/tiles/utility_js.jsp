<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>

<script type="text/javascript">
	/* $(function(){
		$('#logoutBtn').on('click', function (){
			$('#logoutForm').submit();
		});	
		//$(".table>tbody>tr>td").has("span").css("text-align","center");
	})
 */
	/************** Show loader **********/
	function showLoader() {
		var height = $("body").height();
		$(".faderv2").height(height);
		$(".faderv2").fadeIn("slow");
		$(".loaderv2").fadeIn("slow");
		/*$(".loaderv2-message").html("Processing...");*/
	}



	/************** hide loader **********/
	function hideLoader() {
		$(".faderv2").fadeOut("slow");
		$(".loaderv2").fadeOut("show");
	}

	

	/* // Jquery Validation
	$.validator.addMethod("lettersonly", function(value, element) {
		return this.optional(element) || /^[a-zA-Z\s]*$/i.test(value);
		// /^[a-z]+$/
	}, "Only alphabets allowed");

	$.validator.addMethod("lettersandnumbers", function(value, element) {
		return this.optional(element) || /^[a-zA-Z0-9\s]*$/i.test(value);
		// /^[a-z]+$/
	}, "Only alphabets and numbers allowed");
	
	
	$.validator.addMethod("fieldxMailId", function(emailId, element) {
		var reg = /^((?!@).)([a-zA-Z1-9._])*$/g;		
		return  reg.test(emailId);
	}, "@ is not allowed");
	
	var checkSessionTimeout = function(jqXHR, textStatus, errorThrown) {
		console.log(jqXHR, textStatus, errorThrown);
		hideLoader();			
		if ((jqXHR.getResponseHeader('Content-Type') == 'text/html;charset=UTF-8')) {
			$.alert("Session has been timeout. Please login again..", function(){
				window.location.href = contextRoot;					
			});							
		} else {
			$.alert("Couldn't find the server", function(){
				window.location.href = contextRoot;					
			});						
		}
	}; */
</script>



































