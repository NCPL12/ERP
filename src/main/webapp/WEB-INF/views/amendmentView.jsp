<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Amended Sales Order</title>
<style>
  body { font-family: Arial, sans-serif; margin: 0; padding: 16px; background: #f4f6f9; color: #222; }
  h2 { margin: 0 0 4px 0; font-size: 20px; }
  .meta { color: #555; margin-bottom: 14px; font-size: 14px; }
  .wrap { overflow-x: auto; background: #fff; border: 1px solid #ddd; }
  table { width: 100%; border-collapse: collapse; font-size: 14px; }
  th, td { padding: 8px 10px; border-bottom: 1px solid #e5e7eb; text-align: left; }
  th { background: #eef1f6; }
  td.num, th.num { text-align: right; }
  tr.amended td { background: #fff3b0; font-weight: 600; }
  .badge { display: inline-block; padding: 2px 8px; border-radius: 10px; background: #f0a500; color: #fff; font-size: 12px; }
  .error { color: #b00020; font-size: 15px; }
</style>
</head>
<body>
<c:choose>
  <c:when test="${not empty error}">
    <p class="error"><c:out value="${error}"/></p>
  </c:when>
  <c:otherwise>
    <h2>Sales Order <c:out value="${so.id}"/> — Amended items</h2>
    <div class="meta">
      Client PO: <c:out value="${so.clientPoNumber}"/>
      &nbsp;|&nbsp; Items highlighted below were amended (AMDQTY).
    </div>
    <div class="wrap">
      <table>
        <thead>
          <tr>
            <th>Sl.No</th>
            <th>Description</th>
            <th class="num">Qty</th>
            <th class="num">AMDQTY</th>
            <th class="num">Total QTY</th>
            <th>Unit</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${items}" var="item">
            <c:set var="amended" value="${item.amendedQuantity != 0}"/>
            <tr class="${amended ? 'amended' : ''}">
              <td><c:out value="${item.slNo}"/></td>
              <td>
                <c:out value="${item.description}"/>
                <c:if test="${amended}"><span class="badge">AMENDED</span></c:if>
              </td>
              <td class="num"><fmt:formatNumber value="${item.quantity - item.amendedQuantity}" maxFractionDigits="2"/></td>
              <td class="num"><fmt:formatNumber value="${item.amendedQuantity}" maxFractionDigits="2"/></td>
              <td class="num"><fmt:formatNumber value="${item.quantity}" maxFractionDigits="2"/></td>
              <td><c:out value="${item.item_units.name}"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </c:otherwise>
</c:choose>
</body>
</html>
