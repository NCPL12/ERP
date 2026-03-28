<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>Sales Order Audit Trail</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <style>
        .audit-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 0;
            margin-bottom: 30px;
        }
        .audit-table {
            background: white;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        .audit-table th {
            background-color: #f8f9fa;
            border-bottom: 2px solid #dee2e6;
            font-weight: 600;
            color: #495057;
        }
        .action-badge {
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
        }
        .action-CREATE { background-color: #d4edda; color: #155724; }
        .action-UPDATE { background-color: #cce5ff; color: #004085; }
        .action-ARCHIVE { background-color: #fff3cd; color: #856404; }
        .action-UNARCHIVE { background-color: #d1ecf1; color: #0c5460; }
        .action-DELETE_ITEM { background-color: #f8d7da; color: #721c24; }
        .search-panel {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            padding: 20px;
            margin-bottom: 20px;
        }
        .json-data {
            max-width: 200px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            cursor: pointer;
        }
        .json-modal pre {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 4px;
            padding: 15px;
            max-height: 400px;
            overflow-y: auto;
        }
    </style>
</head>
<body>
    <div class="audit-header">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-6">
                    <h1><i class="fas fa-history"></i> Sales Order Audit Trail</h1>
                    <p class="mb-0">Track all sales order operations and changes</p>
                </div>
                <div class="col-md-6 text-right">
                    <button class="btn btn-light" onclick="exportToExcel()">
                        <i class="fas fa-file-excel"></i> Export to Excel
                    </button>
                    <button class="btn btn-light" onclick="refreshAuditLogs()">
                        <i class="fas fa-sync-alt"></i> Refresh
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div class="container">
        <!-- Search Panel -->
        <div class="search-panel">
            <h5><i class="fas fa-search"></i> Search Filters</h5>
            <form id="searchForm">
                <div class="row">
                    <div class="col-md-3">
                        <label for="salesOrderId">Sales Order ID:</label>
                        <input type="text" class="form-control" id="salesOrderId" placeholder="SO-XXXX">
                    </div>
                    <div class="col-md-3">
                        <label for="performedBy">Performed By:</label>
                        <input type="text" class="form-control" id="performedBy" placeholder="Username">
                    </div>
                    <div class="col-md-2">
                        <label for="action">Action:</label>
                        <select class="form-control" id="action">
                            <option value="">All Actions</option>
                            <c:forEach items="${actions}" var="action">
                                <option value="${action}">${action}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label for="startDate">From Date:</label>
                        <input type="date" class="form-control" id="startDate">
                    </div>
                    <div class="col-md-2">
                        <label for="endDate">To Date:</label>
                        <input type="date" class="form-control" id="endDate">
                    </div>
                </div>
                <div class="row mt-3">
                    <div class="col-md-12">
                        <button type="button" class="btn btn-primary" onclick="searchAuditLogs()">
                            <i class="fas fa-search"></i> Search
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="clearFilters()">
                            <i class="fas fa-times"></i> Clear
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <!-- Audit Table -->
        <div class="audit-table">
            <table class="table table-hover mb-0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Sales Order ID</th>
                        <th>Action</th>
                        <th>Performed By</th>
                        <th>Date & Time</th>
                        <th>Description</th>
                        <th>Old Values</th>
                        <th>New Values</th>
                        <th>IP Address</th>
                    </tr>
                </thead>
                <tbody id="auditTableBody">
                    <c:forEach items="${audits}" var="audit">
                        <tr>
                            <td>${audit.id}</td>
                            <td><strong>${audit.salesOrderId}</strong></td>
                            <td>
                                <span class="action-badge action-${audit.action}">
                                    ${audit.action}
                                </span>
                            </td>
                            <td>${audit.performedBy}</td>
                            <td>${audit.actionPerformed}</td>
                            <td>${audit.description}</td>
                            <td>
                                <c:if test="${not empty audit.oldValues}">
                                    <span class="json-data" onclick="showJsonData('${audit.oldValues}', 'Old Values')">
                                        ${audit.oldValues}
                                    </span>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${not empty audit.newValues}">
                                    <span class="json-data" onclick="showJsonData('${audit.newValues}', 'New Values')">
                                        ${audit.newValues}
                                    </span>
                                </c:if>
                            </td>
                            <td>${audit.ipAddress}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <nav aria-label="Audit logs pagination" class="mt-4">
            <ul class="pagination justify-content-center">
                <li class="page-item disabled">
                    <a class="page-link" href="#" tabindex="-1">Previous</a>
                </li>
                <li class="page-item active"><a class="page-link" href="#">1</a></li>
                <li class="page-item"><a class="page-link" href="#">2</a></li>
                <li class="page-item"><a class="page-link" href="#">3</a></li>
                <li class="page-item">
                    <a class="page-link" href="#">Next</a>
                </li>
            </ul>
        </nav>
    </div>

    <!-- JSON Data Modal -->
    <div class="modal fade" id="jsonModal" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="jsonModalTitle">JSON Data</h5>
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <pre id="jsonContent"></pre>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    
    <script>
        function searchAuditLogs() {
            const salesOrderId = document.getElementById('salesOrderId').value;
            const performedBy = document.getElementById('performedBy').value;
            const action = document.getElementById('action').value;
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;
            
            const params = new URLSearchParams({
                salesOrderId: salesOrderId,
                performedBy: performedBy,
                action: action,
                startDate: startDate,
                endDate: endDate
            });
            
            fetch('/ncpl-sales/api/audit/sales-order/search?' + params.toString())
                .then(response => response.json())
                .then(data => {
                    updateAuditTable(data);
                })
                .catch(error => {
                    console.error('Error searching audit logs:', error);
                    alert('Error searching audit logs. Please try again.');
                });
        }
        
        function updateAuditTable(audits) {
            const tbody = document.getElementById('auditTableBody');
            tbody.innerHTML = '';
            
            audits.forEach(audit => {
                const row = document.createElement('tr');
                const formattedDate = new Date(audit.actionPerformed).toLocaleString();
                
                // Build HTML content properly
                let oldValuesHtml = '';
                if (audit.oldValues) {
                    const escapedOldValues = audit.oldValues.replace(/'/g, "\\'");
                    oldValuesHtml = '<span class="json-data" onclick="showJsonData(\'' + escapedOldValues + '\', \'Old Values\')">' + audit.oldValues + '</span>';
                }
                
                let newValuesHtml = '';
                if (audit.newValues) {
                    const escapedNewValues = audit.newValues.replace(/'/g, "\\'");
                    newValuesHtml = '<span class="json-data" onclick="showJsonData(\'' + escapedNewValues + '\', \'New Values\')">' + audit.newValues + '</span>';
                }
                
                row.innerHTML = 
                    '<td>' + audit.id + '</td>' +
                    '<td><strong>' + audit.salesOrderId + '</strong></td>' +
                    '<td><span class="action-badge action-' + audit.action + '">' + audit.action + '</span></td>' +
                    '<td>' + audit.performedBy + '</td>' +
                    '<td>' + formattedDate + '</td>' +
                    '<td>' + audit.description + '</td>' +
                    '<td>' + oldValuesHtml + '</td>' +
                    '<td>' + newValuesHtml + '</td>' +
                    '<td>' + (audit.ipAddress || 'N/A') + '</td>';
                
                tbody.appendChild(row);
            });
        }
        
        function clearFilters() {
            document.getElementById('searchForm').reset();
            refreshAuditLogs();
        }
        
        function refreshAuditLogs() {
            fetch('/ncpl-sales/api/audit/sales-order/all')
                .then(response => response.json())
                .then(data => {
                    updateAuditTable(data);
                })
                .catch(error => {
                    console.error('Error refreshing audit logs:', error);
                });
        }
        
        function showJsonData(jsonString, title) {
            try {
                const jsonData = JSON.parse(jsonString);
                document.getElementById('jsonModalTitle').textContent = title;
                document.getElementById('jsonContent').textContent = JSON.stringify(jsonData, null, 2);
                $('#jsonModal').modal('show');
            } catch (e) {
                document.getElementById('jsonModalTitle').textContent = title;
                document.getElementById('jsonContent').textContent = jsonString;
                $('#jsonModal').modal('show');
            }
        }
        
        function exportToExcel() {
            // Simple CSV export
            fetch('/ncpl-sales/api/audit/sales-order/all')
                .then(response => response.json())
                .then(data => {
                    let csv = 'ID,Sales Order ID,Action,Performed By,Date & Time,Description,Old Values,New Values,IP Address\n';
                    
                    data.forEach(audit => {
                        csv += `${audit.id},"${audit.salesOrderId}","${audit.action}","${audit.performedBy}","${audit.actionPerformed}","${audit.description}","${audit.oldValues || ''}","${audit.newValues || ''}","${audit.ipAddress || ''}"\n`;
                    });
                    
                    const blob = new Blob([csv], { type: 'text/csv' });
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = 'sales-order-audit.csv';
                    a.click();
                    window.URL.revokeObjectURL(url);
                })
                .catch(error => {
                    console.error('Error exporting to Excel:', error);
                    alert('Error exporting data. Please try again.');
                });
        }
        
        // Auto-refresh every 5 minutes
        setInterval(refreshAuditLogs, 300000);
        
        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            // Set today's date as default end date
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('endDate').value = today;
            
            // Set 30 days ago as default start date
            const thirtyDaysAgo = new Date();
            thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
            document.getElementById('startDate').value = thirtyDaysAgo.toISOString().split('T')[0];
        });
    </script>
</body>
</html>
