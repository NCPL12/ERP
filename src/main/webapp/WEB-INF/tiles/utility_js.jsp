<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<style>
	/* OLD: .dataTables_wrapper { visibility: hidden; } */

	/* NEW: hide the table only while the html element has the dt-hide class (added by plain JS) */
	/* DISABLED: the table is no longer hidden, so the header shows immediately like the GRN page
	.dt-hide .dataTables_wrapper { visibility: hidden; }
	*/

	/* NEW: light grey overlay behind the loader, so the white page is not visible */
	.faderv2 { background: rgba(0,0,0,0.35) !important; }
</style>

<script type="text/javascript">
	/* NEW: plain JS, works even without jQuery */
	// DISABLED: do not hide the table
	// document.documentElement.className += ' dt-hide';

	function revealTables() {
		document.documentElement.className =
			document.documentElement.className.replace(' dt-hide', '');
	}

	/* NEW FIX: stop the loader from spinning twice, delay the hide by 300 ms, cancel it if a show comes in between */
	var hideLoaderTimer = null;

	/* NEW FIX 2: the loader keeps running continuously until the first data draw (no stop in between) */
	var firstDrawDone = false;

	/* NEW: guaranteed safety, even if jQuery or another script fails, the table shows after 10 seconds */
	// OLD: setTimeout(function () { revealTables(); }, 10000);
	setTimeout(function () {
		firstDrawDone = true;
		hideLoader();
		revealTables();
	}, 10000);

	/************** Show loader **********/
	// OLD showLoader (commented, replaced below)
	// function showLoader() {
	// 	if (!window.jQuery) { return; }
	// 	$(".faderv2").fadeIn("fast");
	// 	$(".loaderv2").fadeIn("fast");
	// }
	function showLoader() {
		if (!window.jQuery) { return; }
		clearTimeout(hideLoaderTimer);
		$(".faderv2").stop(true, true).fadeIn("fast");
		$(".loaderv2").stop(true, true).fadeIn("fast");
	}

	/************** hide loader **********/
	// OLD hideLoader (commented, replaced below)
	// function hideLoader() {
	// 	if (window.jQuery) {
	// 		$(".faderv2").fadeOut("fast");
	// 		$(".loaderv2").fadeOut("fast");
	// 	}
	// 	revealTables();
	// }
	function hideLoader() {
		clearTimeout(hideLoaderTimer);
		hideLoaderTimer = setTimeout(function () {
			if (window.jQuery) {
				$(".faderv2").stop(true, true).fadeOut("fast");
				$(".loaderv2").stop(true, true).fadeOut("fast");
			}
			revealTables();
		}, 300);
	}

	/* NEW: bind the events on DOM ready */
	document.addEventListener('DOMContentLoaded', function () {
		if (!window.jQuery) {
			console.error('jQuery is not loaded before this script - fix script order');
			revealTables();
			return;
		}

		showLoader();

		// OLD (commented, replaced below)
		// $(document)
		// 	.on('preXhr.dt', showLoader)
		// 	.on('xhr.dt draw.dt init.dt', hideLoader);
		// $(document).ajaxStart(showLoader).ajaxStop(hideLoader);
		// $(window).on('load', function () {
		// 	if ($.active === 0) { hideLoader(); }
		// });

		$(document)
			.on('preXhr.dt', showLoader)
			.on('draw.dt', function () {
				firstDrawDone = true;   // data has arrived, the loader can be hidden now
				hideLoader();
			});

		$(document).ajaxStart(showLoader).ajaxStop(function () {
			// do not hide the loader on ajaxStop before the first draw
			if (firstDrawDone) { hideLoader(); }
		});

		$(window).on('load', function () {
			if (firstDrawDone) {
				if ($.active === 0) { hideLoader(); }
				return;
			}
			// if this is not a DataTable page, make sure the loader does not get stuck (hide after 1.5 seconds)
			setTimeout(function () {
				if (!firstDrawDone && $.active === 0) {
					firstDrawDone = true;
					hideLoader();
				}
			}, 1500);
		});
	});

	/* OLD (commented, replaced above)
	// showLoader();
	// $(document)
	// 	.on('preXhr.dt', function () { showLoader(); })
	// 	.on('xhr.dt',    function () { hideLoader(); });
	// $(document).ajaxStart(function () { showLoader(); });
	// $(document).ajaxStop(function () { hideLoader(); });
	// $(document).on('init.dt', function () { hideLoader(); });
	// $(window).on('load', function () { hideLoader(); });
	// $(document).on('draw.dt xhr.dt', function () {
	// 	$('.dataTables_wrapper').css('visibility', 'visible');
	// });
	// setTimeout(function () {
	// 	hideLoader();
	// 	$('.dataTables_wrapper').css('visibility', 'visible');
	// }, 10000);
	*/

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