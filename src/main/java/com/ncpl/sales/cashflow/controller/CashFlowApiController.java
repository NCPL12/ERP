package com.ncpl.sales.cashflow.controller;

import com.ncpl.sales.cashflow.dto.AnalyticsDTO;
import com.ncpl.sales.cashflow.dto.ExecutiveSummaryDTO;
import com.ncpl.sales.cashflow.dto.OverviewAnalysisDTO;
import com.ncpl.sales.cashflow.dto.OverdueInvoiceDTO;
import com.ncpl.sales.cashflow.dto.ReconciliationDTO;
import com.ncpl.sales.cashflow.dto.TopPerformanceDTO;
import com.ncpl.sales.cashflow.dto.UpcomingInvoiceDTO;
import com.ncpl.sales.cashflow.model.CashFlowSummary;
import com.ncpl.sales.cashflow.model.Invoice;
import com.ncpl.sales.cashflow.service.AnalyticsService;
import com.ncpl.sales.cashflow.service.CashFlowAnalysisService;
import com.ncpl.sales.cashflow.service.ExcelReaderService;
import com.ncpl.sales.cashflow.service.AgingAnalysisService;
import com.ncpl.sales.cashflow.service.CustomerVendorAnalysisService;
import com.ncpl.sales.cashflow.service.ExcelExportService;
import com.ncpl.sales.cashflow.service.InsightsService;
import com.ncpl.sales.cashflow.service.InvoicePipelineService;
import com.ncpl.sales.cashflow.service.SessionDataService;
import com.ncpl.sales.cashflow.service.TallySyncService;
import com.ncpl.sales.cashflow.service.RecurringTransactionManagementService;
import com.ncpl.sales.cashflow.util.InvoiceFilterUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cashflow-analyzer/api/cashflow")
public class CashFlowApiController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CashFlowApiController.class);

    private final ExcelReaderService excelReaderService;
    private final CashFlowAnalysisService analysisService;
    private final AnalyticsService analyticsService;
    private final AgingAnalysisService agingAnalysisService;
    private final CustomerVendorAnalysisService customerVendorAnalysisService;
    private final SessionDataService sessionDataService;
    private final InsightsService insightsService;
    private final ExcelExportService excelExportService;
    private final TallySyncService tallySyncService;
    private final InvoicePipelineService invoicePipelineService;
    private final RecurringTransactionManagementService recurringTransactionService;
    private final com.ncpl.sales.cashflow.repository.InvoiceRepository invoiceRepository;
    private final com.ncpl.sales.cashflow.repository.RecurringTransactionRepository recurringTransactionRepository;

    @Value("${cashflow.file.upload-dir:src/main/uploads}")
    private String uploadDir;

    public CashFlowApiController(ExcelReaderService excelReaderService,
            CashFlowAnalysisService analysisService,
            AnalyticsService analyticsService,
            AgingAnalysisService agingAnalysisService,
            CustomerVendorAnalysisService customerVendorAnalysisService,
            SessionDataService sessionDataService,
            InsightsService insightsService,
            ExcelExportService excelExportService,
            TallySyncService tallySyncService,
            InvoicePipelineService invoicePipelineService,
            RecurringTransactionManagementService recurringTransactionService,
            com.ncpl.sales.cashflow.repository.InvoiceRepository invoiceRepository,
            com.ncpl.sales.cashflow.repository.RecurringTransactionRepository recurringTransactionRepository) {
        this.excelReaderService = excelReaderService;
        this.analysisService = analysisService;
        this.analyticsService = analyticsService;
        this.agingAnalysisService = agingAnalysisService;
        this.customerVendorAnalysisService = customerVendorAnalysisService;
        this.sessionDataService = sessionDataService;
        this.insightsService = insightsService;
        this.excelExportService = excelExportService;
        this.tallySyncService = tallySyncService;
        this.invoicePipelineService = invoicePipelineService;
        this.recurringTransactionService = recurringTransactionService;
        this.invoiceRepository = invoiceRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> analyze(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file == null || file.isEmpty()) {
            response.put("success", false);
            response.put("message", "No file uploaded");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String cleanedOriginal = (originalName != null) ? StringUtils.cleanPath(originalName) : "";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = (!StringUtils.hasText(cleanedOriginal))
                    ? ("upload-" + timestamp + ".xlsx")
                    : (timestamp + "-" + cleanedOriginal);
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target);

            List<Invoice> invoices = excelReaderService.readExcelFile(target.toString());
            CashFlowSummary summary = analysisService.analyzeCashFlow(invoices);

            response.put("success", true);
            response.put("data", summary);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverviewData() {
        try {
            OverviewAnalysisDTO cachedData = sessionDataService.getOverviewData();
            if (cachedData == null) {
                return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data available"));
            }
            return ResponseEntity.ok(cachedData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to retrieve data: " + e.getMessage()));
        }
    }

    @Transactional
    @PostMapping(value = "/overview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeOverview(@RequestParam("file") MultipartFile file) {
        // Only allow Excel upload in Excel mode
        if (!sessionDataService.isExcelMode()) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Excel upload is only available in Excel mode"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No file uploaded"));
        }

        String originalName = file.getOriginalFilename();
        String cleanedOriginal = (originalName != null) ? StringUtils.cleanPath(originalName) : "";
        String lowerFilename = StringUtils.hasText(cleanedOriginal)
                ? cleanedOriginal.toLowerCase(Locale.ENGLISH)
                : "";
        if (!lowerFilename.endsWith(".xlsx") && !lowerFilename.endsWith(".xls")) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Only Excel files (.xlsx, .xls) are supported"));
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = (!StringUtils.hasText(cleanedOriginal))
                    ? ("upload-" + timestamp + ".xlsx")
                    : (timestamp + "-" + cleanedOriginal);
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target);

            List<Invoice> invoices = excelReaderService.readExcelFile(target.toString());

            // PERSIST TO DATABASE
            try {
                logger.info("💾 Persistence: Deleting old EXCEL records...");
                invoiceRepository.deleteBySource("EXCEL");
                
                logger.info("💾 Persistence: Mapping {} invoices to entities...", invoices.size());
                List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities = invoices.stream()
                        .filter(inv -> inv != null && inv.getCustomerName() != null)
                        .map(inv -> {
                            com.ncpl.sales.cashflow.entity.InvoiceEntity entity = new com.ncpl.sales.cashflow.entity.InvoiceEntity(
                                    inv.getCustomerName(),
                                    inv.getInvoiceNumber(),
                                    inv.getInvoiceDate(),
                                    inv.getDueDate(),
                                    BigDecimal.valueOf(inv.getInvoiceValue()),
                                    com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.valueOf(inv.getType().name()),
                                    "EXCEL");
                            entity.setReconciled(inv.isReconciled());
                            return entity;
                        })
                        .collect(Collectors.toList());
                
                logger.info("💾 Persistence: Saving {} entities to MySQL...", entities.size());
                invoiceRepository.saveAll(entities);
                invoiceRepository.flush();
                logger.info("✅ Persistence: Successful sync to MySQL");
            } catch (Exception dbEx) {
                logger.error("❌ Persistence Error: Failed to save to MySQL - {}", dbEx.getMessage(), dbEx);
            }

            OverviewAnalysisDTO analysis = analysisService.analyzeForOverview(invoices);
            applyBankBalance(analysis);

            sessionDataService.clearAll();
            sessionDataService.setUploadedFileName(originalName);
            sessionDataService.setSavedFilePath(target.toString());
            sessionDataService.setInvoices(invoices);
            sessionDataService.setOverviewData(analysis);

            return ResponseEntity.ok(analysis);
        } catch (IOException e) {
            logger.error("Excel upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during Excel upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @Transactional
    @PostMapping("/reconcile")
    public ResponseEntity<?> updateReconciliation(@RequestBody Map<String, Object> payload) {
        try {
            Object idObj = payload.get("id");
            Long id = null;
            if (idObj != null) {
                try {
                    id = Long.valueOf(idObj.toString());
                } catch (NumberFormatException nfe) {
                    logger.warn("Reconciliation: Non-numeric ID received: {}. Will fallback to name/number matching.", idObj);
                }
            }
            String invoiceNumber = (String) payload.get("invoiceNumber");
            String customerName = (String) payload.get("customerName");
            boolean reconciled = (boolean) payload.get("reconciled");

            boolean updated = false;

            if (id != null) {
                Optional<com.ncpl.sales.cashflow.entity.InvoiceEntity> entityOpt = invoiceRepository.findById(id);
                if (entityOpt.isPresent()) {
                    com.ncpl.sales.cashflow.entity.InvoiceEntity entity = entityOpt.get();
                    entity.setReconciled(reconciled);
                    invoiceRepository.save(entity);
                    updated = true;
                }
            }

            if (!updated) {
                List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities = invoiceRepository
                        .findByCustomerNameContainingIgnoreCase(customerName);
                for (com.ncpl.sales.cashflow.entity.InvoiceEntity entity : entities) {
                    if (((entity.getInvoiceNumber() == null && invoiceNumber == null) ||
                            (entity.getInvoiceNumber() != null && entity.getInvoiceNumber().equals(invoiceNumber)))) {
                        entity.setReconciled(reconciled);
                        invoiceRepository.save(entity);
                        updated = true;
                    }
                }
            }

            List<Invoice> sessionInvoices = sessionDataService.getInvoices();
            if (sessionInvoices != null) {
                for (Invoice inv : sessionInvoices) {
                    boolean match = (id != null && id.equals(inv.getId())) ||
                                    (id == null && inv.getCustomerName().equals(customerName) && 
                                     ((inv.getInvoiceNumber() == null && invoiceNumber == null) ||
                                      (inv.getInvoiceNumber() != null && inv.getInvoiceNumber().equals(invoiceNumber))));
                    if (match) {
                        inv.setReconciled(reconciled);
                    }
                }
            }

            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invoice not found in database"));
            }

            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map("success", true, "message", "Reconciliation status updated in MySQL"));
        } catch (Exception e) {
            logger.error("Error updating reconciliation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/analytics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeAnalytics(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No file uploaded"));
        }

        if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Only Excel files (.xlsx, .xls) are supported"));
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String cleanedOriginal = (originalName != null) ? StringUtils.cleanPath(originalName) : "";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = (!StringUtils.hasText(cleanedOriginal))
                    ? ("upload-" + timestamp + ".xlsx")
                    : (timestamp + "-" + cleanedOriginal);
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target);

            List<Invoice> invoices = excelReaderService.readExcelFile(target.toString());
            AnalyticsDTO analytics = analyticsService.analyzeMonthlyAndWeekly(invoices);

            sessionDataService.setUploadedFileName(originalName);
            sessionDataService.setSavedFilePath(target.toString());
            sessionDataService.setInvoices(invoices);
            sessionDataService.setAnalyticsData(analytics);

            return ResponseEntity.ok(analytics);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/aging-analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeAging(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No file uploaded"));
        }

        if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Only Excel files (.xlsx, .xls) are supported"));
        }

        try {
            AgingAnalysisService.AgingAnalysisDTO aging = agingAnalysisService.analyzeAging(file);

            sessionDataService.setUploadedFileName(file.getOriginalFilename());
            sessionDataService.setAgingAnalysisData(aging);

            return ResponseEntity.ok(aging);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/customer-vendor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeCustomerVendor(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No file uploaded"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Only Excel files (.xlsx, .xls) are supported"));
        }

        try {
            CustomerVendorAnalysisService.CustomerVendorAnalysisDTO analysis = customerVendorAnalysisService
                    .analyze(file);

            sessionDataService.setUploadedFileName(filename);
            sessionDataService.setCustomerVendorData(analysis);

            return ResponseEntity.ok(analysis);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * NEW: Analyze ONLY OVERDUE invoices (due date < today)
     * Used by: Overdue Payments tab
     * FIXED: Now properly saves file to disk for session persistence
     */
    @PostMapping(value = "/overdue-only", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeOverdueOnly(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No file uploaded"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Only Excel files (.xlsx, .xls) are supported"));
        }

        try {
            // SAVE FILE TO DISK (THIS WAS MISSING!)
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String cleanedOriginal = StringUtils.cleanPath(filename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String savedFilename = timestamp + "-" + cleanedOriginal;
            Path target = uploadPath.resolve(savedFilename);
            Files.copy(file.getInputStream(), target);

            // SAVE TO DATABASE FOR MULTI-USER SYNCHRONIZATION
            List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities = customerVendorAnalysisService.parseToEntities(file);
            if (!entities.isEmpty()) {
                logger.info("💾 Persistence: Saving {} Excel records to MySQL for global sharing...", entities.size());
                invoiceRepository.deleteBySource("EXCEL"); // Refresh for this source
                invoiceRepository.saveAll(entities);
            }

            // Generate analysis for response (Overdue Only)
            List<Invoice> modelInvoices = convertEntitiesToInvoices(entities);
            CustomerVendorAnalysisService.CustomerVendorAnalysisDTO analysis = customerVendorAnalysisService
                    .analyzeOverdueOnly(modelInvoices);

            // Save to session WITH file path
            sessionDataService.setUploadedFileName(filename);
            sessionDataService.setSavedFilePath(target.toString());
            sessionDataService.setOverdueData(analysis);
            sessionDataService.setInvoices(modelInvoices); // Sync memory too

            return ResponseEntity.ok(analysis);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo() {
        boolean hasData = sessionDataService.hasData();
        String fileName = sessionDataService.getUploadedFileName();
        String mode = sessionDataService.getSelectedMode();

        // Check database if session is empty to support multi-user sharing
        if (!hasData) {
            String source = sessionDataService.isTallyMode() ? "TALLY" : "EXCEL";
            Long dbCount = invoiceRepository.countBySource(source);
            logger.info("🔍 Session Check: No session data, checking DB for source '{}'... Found: {}", source, dbCount);
            
            if (dbCount > 0) {
                hasData = true;
                fileName = sessionDataService.isTallyMode() ? "Tally 6.2 Sync" : "Shared Database Records";
                // Crucial: Set the filename in session so hasUploadedFile() returns true for subsequent calls
                sessionDataService.setUploadedFileName(fileName);
                logger.info("🔄 Session Check: Restored data status from shared database for record: {}", fileName);
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = null;
        if (authentication != null && authentication.isAuthenticated()) {
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("hasData", hasData);
        response.put("fileName", fileName);
        response.put("mode", mode);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bank-balance")
    public ResponseEntity<?> getBankBalanceValue() {
        return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                "amount", sessionDataService.getBankBalance()));
    }

    @PostMapping("/bank-balance")
    public ResponseEntity<?> updateBankBalance(@RequestParam("amount") BigDecimal amount) {
        sessionDataService.setBankBalance(amount);
        OverviewAnalysisDTO cached = sessionDataService.getOverviewData();
        if (cached != null) {
            applyBankBalance(cached);
        }
        return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                "amount", sessionDataService.getBankBalance()));
    }

    @GetMapping("/overview/cached")
    public ResponseEntity<?> getCachedOverview(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);
            boolean filtered = month != null || week != null || fromDate != null || toDate != null;

            if (!filtered) {
                OverviewAnalysisDTO cached = sessionDataService.getOverviewData();
                if (cached != null) {
                    applyBankBalance(cached);
                    return ResponseEntity.ok(cached);
                }
            }

            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            List<Invoice> scoped = filterInvoices(invoices, month, week, fromDate, toDate);
            OverviewAnalysisDTO analysis = analysisService.analyzeForOverview(scoped);

            if (!filtered) {
                sessionDataService.setOverviewData(analysis);
            }
            applyBankBalance(analysis);
            return ResponseEntity.ok(analysis);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid date format. Use yyyy-MM-dd."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/cached")
    public ResponseEntity<?> getCachedAnalytics(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);
            boolean filtered = month != null || week != null || fromDate != null || toDate != null;

            if (!filtered) {
                AnalyticsDTO cached = sessionDataService.getAnalyticsData();
                if (cached != null) {
                    return ResponseEntity.ok(cached);
                }
            }

            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            List<Invoice> scoped = filterInvoices(invoices, month, week, fromDate, toDate);
            AnalyticsDTO analytics = analyticsService.analyzeMonthlyAndWeekly(scoped);

            if (!filtered) {
                sessionDataService.setAnalyticsData(analytics);
            }
            return ResponseEntity.ok(analytics);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid month format. Use yyyy-MM."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/customer-vendor/cached")
    public ResponseEntity<?> getCachedCustomerVendor(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam) {
        try {
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            boolean filtered = month != null || week != null;

            if (!filtered) {
                CustomerVendorAnalysisService.CustomerVendorAnalysisDTO cached = sessionDataService
                        .getCustomerVendorData();
                if (cached != null) {
                    return ResponseEntity.ok(cached);
                }
            }

            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            List<Invoice> scoped = (month != null || week != null)
                    ? InvoiceFilterUtil.filterByDueDate(invoices, month, week)
                    : invoices;
            CustomerVendorAnalysisService.CustomerVendorAnalysisDTO analysis = customerVendorAnalysisService
                    .analyze(scoped);

            if (!filtered) {
                sessionDataService.setCustomerVendorData(analysis);
            }
            return ResponseEntity.ok(analysis);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid month format. Use yyyy-MM."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    /**
     * NEW: Get cached overdue-only data from session
     * This endpoint serves the Overdue Payments tab which shows only overdue
     * invoices
     */
    @GetMapping("/overdue-only/cached")
    public ResponseEntity<?> getCachedOverdueOnly(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam) {
        try {
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            boolean filtered = month != null || week != null;

            if (!filtered) {
                CustomerVendorAnalysisService.CustomerVendorAnalysisDTO cached = sessionDataService.getOverdueData();
                if (cached != null) {
                    return ResponseEntity.ok(cached);
                }
            }

            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            List<Invoice> scoped = (month != null || week != null)
                    ? InvoiceFilterUtil.filterByDueDate(invoices, month, week)
                    : invoices;
            CustomerVendorAnalysisService.CustomerVendorAnalysisDTO analysis = customerVendorAnalysisService
                    .analyzeOverdueOnly(scoped);

            if (!filtered) {
                sessionDataService.setOverdueData(analysis);
            }
            return ResponseEntity.ok(analysis);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid date format. Use yyyy-MM-dd."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/aging-analysis/cached")
    public ResponseEntity<?> getCachedAgingAnalysis(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);
            boolean filtered = month != null || week != null || fromDate != null || toDate != null;

            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            if (!filtered) {
                AgingAnalysisService.AgingAnalysisDTO cached = sessionDataService.getAgingAnalysisData();
                if (cached != null) {
                    return ResponseEntity.ok(cached);
                }
            }

            // DEBUG: Log invoice data
            System.out.println("=== AGING ANALYSIS DEBUG ===");
            System.out.println("Total invoices: " + invoices.size());
            System.out.println(
                    "Inflows: " + invoices.stream().filter(i -> i.getType() == Invoice.InvoiceType.INFLOW).count());
            System.out.println(
                    "Outflows: " + invoices.stream().filter(i -> i.getType() == Invoice.InvoiceType.OUTFLOW).count());
            invoices.forEach(inv -> {
                System.out.printf("Invoice: %s, Type: %s, Due: %s, Value: %.2f%n",
                        inv.getInvoiceNumber(), inv.getType(), inv.getDueDate(), inv.getInvoiceValue());
            });

            List<Invoice> scoped;
            if (fromDate != null || toDate != null) {
                scoped = InvoiceFilterUtil.filterByDueDateRange(invoices, fromDate, toDate);
            } else if (month != null || week != null) {
                scoped = InvoiceFilterUtil.filterByDueDate(invoices, month, week);
            } else {
                scoped = invoices;
            }

            System.out.println("Scoped invoices: " + scoped.size());
            AgingAnalysisService.AgingAnalysisDTO analysis = agingAnalysisService.analyzeAging(scoped);
            System.out.println("Analysis result: " + (analysis != null ? "NOT NULL" : "NULL"));

            if (!filtered) {
                sessionDataService.setAgingAnalysisData(analysis);
            }
            return ResponseEntity.ok(analysis);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid date format. Use yyyy-MM-dd."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/aging-analysis/download")
    public ResponseEntity<?> downloadAgingReport(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);

            AgingAnalysisService.AgingAnalysisDTO data = sessionDataService.getAgingAnalysisData();

            if (data == null || month != null || week != null || fromDate != null || toDate != null) {
                List<Invoice> invoices = resolveInvoices();
                if (invoices == null || invoices.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
                }
                List<Invoice> scoped;
                if (fromDate != null || toDate != null) {
                    scoped = InvoiceFilterUtil.filterByDueDateRange(invoices, fromDate, toDate);
                } else if (month != null || week != null) {
                    scoped = InvoiceFilterUtil.filterByDueDate(invoices, month, week);
                } else {
                    scoped = invoices;
                }
                data = agingAnalysisService.analyzeAging(scoped);
            }

            byte[] report = agingAnalysisService.generateDetailedReport(data);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Aging-Analysis-Report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(report);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid date format. Use yyyy-MM-dd."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to build aging report: " + e.getMessage()));
        }
    }

    /**
     * Get cached due date tracker data from session
     * This endpoint serves the Due Date Tracker tab which shows ALL invoices by due
     * date
     */
    @GetMapping("/due-date-tracker/cached")
    public ResponseEntity<?> getCachedDueDateTracker(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam) {
        return getCachedCustomerVendor(monthParam, weekParam);
    }

    @GetMapping("/top-performers")
    public ResponseEntity<?> getTopPerformers(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam) {
        try {
            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            TopPerformanceDTO dto = insightsService.buildTopPerformance(invoices, month, week);
            return ResponseEntity.ok(dto);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid month format. Use yyyy-MM."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/debtors-creditors")
    public ResponseEntity<?> getDebtorsCreditors(
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);

            Map<String, Object> result = new HashMap<>();

            // Calculate highest debtors (receivables). Use overdue receivables if present,
            // otherwise fall back to all receivables.
            List<Invoice> overdueReceivables = invoices.stream()
                    .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                    .filter(Invoice::isOverdue)
                    .collect(Collectors.toList());
            if (overdueReceivables.isEmpty()) {
                overdueReceivables = invoices.stream()
                        .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                        .collect(Collectors.toList());
            }
            List<Invoice> selectedReceivables = overdueReceivables;

            Map<String, Double> debtorAmounts = selectedReceivables.stream()
                    .collect(Collectors.groupingBy(
                            Invoice::getCustomerName,
                            Collectors.summingDouble(Invoice::getInvoiceValue)));

            List<Map<String, Object>> highestDebtors = debtorAmounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> debtor = new HashMap<>();
                        debtor.put("name", entry.getKey());
                        debtor.put("amount", entry.getValue());
                        // Calculate average days overdue for this debtor
                        double avgDays = selectedReceivables.stream()
                                .filter(inv -> entry.getKey().equals(inv.getCustomerName()))
                                .mapToLong(Invoice::getDaysOverdue)
                                .average()
                                .orElse(0.0);
                        debtor.put("daysOverdue", (int) Math.round(avgDays));
                        return debtor;
                    })
                    .collect(Collectors.toList());

            // Calculate highest creditors (payables)
            List<Invoice> payables = invoices.stream()
                    .filter(inv -> inv.getType() == Invoice.InvoiceType.OUTFLOW)
                    .collect(Collectors.toList());

            Map<String, Double> creditorAmounts = payables.stream()
                    .collect(Collectors.groupingBy(
                            Invoice::getCustomerName,
                            Collectors.summingDouble(Invoice::getInvoiceValue)));

            List<Map<String, Object>> highestCreditors = creditorAmounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> creditor = new HashMap<>();
                        creditor.put("name", entry.getKey());
                        creditor.put("amount", entry.getValue());
                        // Calculate average days until due for this creditor
                        double avgDays = payables.stream()
                                .filter(inv -> entry.getKey().equals(inv.getCustomerName()))
                                .mapToLong(inv -> inv.getDaysUntilDue())
                                .average()
                                .orElse(0.0);
                        creditor.put("daysUntilDue", (int) Math.round(avgDays));
                        return creditor;
                    })
                    .collect(Collectors.toList());

            result.put("highestDebtors", highestDebtors);
            result.put("highestCreditors", highestCreditors);
            result.put("totalReceivables", debtorAmounts.values().stream().mapToDouble(Double::doubleValue).sum());
            result.put("totalPayables", creditorAmounts.values().stream().mapToDouble(Double::doubleValue).sum());

            return ResponseEntity.ok(result);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid date format. Use yyyy-MM-dd."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to load debtors and creditors data: " + e.getMessage()));
        }
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<?> getReconciliation(
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam,
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam) {
        try {
            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            LocalDate fromDate = null;
            LocalDate toDate = null;
            if (fromDateParam != null && !fromDateParam.codePoints().allMatch(Character::isWhitespace)) {
                fromDate = LocalDate.parse(fromDateParam);
            }
            if (toDateParam != null && !toDateParam.codePoints().allMatch(Character::isWhitespace)) {
                toDate = LocalDate.parse(toDateParam);
            }

            ReconciliationDTO dto;
            if (fromDate != null || toDate != null) {
                dto = insightsService.buildReconciliation(invoices, fromDate, toDate);
            } else {
                YearMonth month = parseYearMonth(monthParam);
                Integer week = normalizeWeek(weekParam);
                dto = insightsService.buildReconciliation(invoices, month, week);
            }
            return ResponseEntity.ok(dto);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error",
                            "Invalid date format. Use yyyy-MM-dd for fromDate/toDate or yyyy-MM for month."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @PostMapping("/reconciliation/export")
    public ResponseEntity<byte[]> exportReconciliation(@RequestBody Map<String, Object> requestData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> selectedReceivables = (List<Map<String, String>>) requestData
                    .get("selectedReceivables");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> selectedPayables = (List<Map<String, String>>) requestData
                    .get("selectedPayables");
            String adjustedBankBalance = (String) requestData.get("adjustedBankBalance");
            String exportDate = (String) requestData.get("exportDate");

            byte[] excelData = excelExportService.exportReconciliation(
                    selectedReceivables != null ? selectedReceivables : new ArrayList<>(),
                    selectedPayables != null ? selectedPayables : new ArrayList<>(),
                    adjustedBankBalance != null ? adjustedBankBalance : "₹0",
                    exportDate != null ? exportDate : LocalDateTime.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reconciliation_" + LocalDate.now() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/executive-summary")
    public ResponseEntity<?> getExecutiveSummary(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam) {
        try {
            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }
            YearMonth month = parseYearMonth(monthParam);
            Integer week = normalizeWeek(weekParam);
            ExecutiveSummaryDTO dto = insightsService.buildExecutiveSummary(invoices, month, week);
            return ResponseEntity.ok(dto);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid month format. Use yyyy-MM."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to reload invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/overview/export-overdue")
    public ResponseEntity<?> exportOverdueInvoices(@RequestParam(value = "type", defaultValue = "all") String type) {
        try {
            OverviewAnalysisDTO data = sessionDataService.getOverviewData();
            if (data == null) {
                List<Invoice> invoices = resolveInvoices();
                if (invoices == null || invoices.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
                }
                data = analysisService.analyzeForOverview(invoices);
            }

            List<OverdueInvoiceDTO> receivables = data.getOverdueReceivables() != null ? data.getOverdueReceivables()
                    : com.ncpl.sales.cashflow.util.Java8Collections.list();
            List<OverdueInvoiceDTO> payables = data.getOverduePayables() != null ? data.getOverduePayables()
                    : com.ncpl.sales.cashflow.util.Java8Collections.list();

            byte[] excel = excelExportService.exportOverdueInvoices(receivables, payables, type);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Overview-Overdue-Invoices.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export Excel: " + e.getMessage()));
        }
    }

    @GetMapping("/overview/export-due")
    public ResponseEntity<?> exportDueInvoices(@RequestParam(value = "type", defaultValue = "all") String type) {
        try {
            OverviewAnalysisDTO data = sessionDataService.getOverviewData();
            if (data == null) {
                List<Invoice> invoices = resolveInvoices();
                if (invoices == null || invoices.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
                }
                data = analysisService.analyzeForOverview(invoices);
            }

            List<UpcomingInvoiceDTO> receivables = data.getDueReceivables() != null ? data.getDueReceivables()
                    : com.ncpl.sales.cashflow.util.Java8Collections.list();
            List<UpcomingInvoiceDTO> payables = data.getDuePayables() != null ? data.getDuePayables() : com.ncpl.sales.cashflow.util.Java8Collections.list();

            byte[] excel = excelExportService.exportDueInvoices(receivables, payables, type);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Overview-Due-Invoices.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export Excel: " + e.getMessage()));
        }
    }

    @GetMapping("/top-performers/export")
    public ResponseEntity<?> exportTopPerformers(
            @RequestParam(value = "type") String type,
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "week", required = false) Integer weekParam,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            // Apply filters
            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);
            List<Invoice> scoped = filterInvoices(invoices, parseYearMonth(monthParam), normalizeWeek(weekParam),
                    fromDate, toDate);

            TopPerformanceDTO dto = insightsService.buildTopPerformance(scoped, null, null);

            byte[] excel = excelExportService.exportTopPerformers(dto, type);
            HttpHeaders headers = new HttpHeaders();
            String filename = "customers".equals(type) ? "Top-Customers-Vendors-Customers.xlsx"
                    : "Top-Customers-Vendors-Vendors.xlsx";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid month format. Use yyyy-MM."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export Excel: " + e.getMessage()));
        }
    }

    @GetMapping("/debtors-creditors/download")
    public ResponseEntity<?> downloadDebtorsCreditors(
            @RequestParam(value = "type") String type,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestParam(value = "toDate", required = false) String toDateParam) {
        try {
            List<Invoice> invoices = resolveInvoices();
            if (invoices == null || invoices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "No data in session. Please upload a file."));
            }

            LocalDate fromDate = parseDate(fromDateParam);
            LocalDate toDate = parseDate(toDateParam);

            // Calculate data similar to the getDebtorsCreditors method
            List<Map<String, Object>> debtors = new ArrayList<>();
            List<Map<String, Object>> creditors = new ArrayList<>();

            if ("debtors".equals(type) || "all".equals(type)) {
                List<Invoice> overdueReceivables = invoices.stream()
                        .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                        .filter(Invoice::isOverdue)
                        .collect(Collectors.toList());

                Map<String, Double> debtorAmounts = overdueReceivables.stream()
                        .collect(Collectors.groupingBy(
                                Invoice::getCustomerName,
                                Collectors.summingDouble(Invoice::getInvoiceValue)));

                debtors = debtorAmounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .map(entry -> {
                            Map<String, Object> debtor = new HashMap<>();
                            debtor.put("name", entry.getKey());
                            debtor.put("amount", entry.getValue());
                            double avgDays = overdueReceivables.stream()
                                    .filter(inv -> entry.getKey().equals(inv.getCustomerName()))
                                    .mapToLong(Invoice::getDaysOverdue)
                                    .average()
                                    .orElse(0.0);
                            debtor.put("daysOverdue", (int) Math.round(avgDays));
                            return debtor;
                        })
                        .collect(Collectors.toList());
            }

            if ("creditors".equals(type) || "all".equals(type)) {
                List<Invoice> payables = invoices.stream()
                        .filter(inv -> inv.getType() == Invoice.InvoiceType.OUTFLOW)
                        .collect(Collectors.toList());

                Map<String, Double> creditorAmounts = payables.stream()
                        .collect(Collectors.groupingBy(
                                Invoice::getCustomerName,
                                Collectors.summingDouble(Invoice::getInvoiceValue)));

                creditors = creditorAmounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .map(entry -> {
                            Map<String, Object> creditor = new HashMap<>();
                            creditor.put("name", entry.getKey());
                            creditor.put("amount", entry.getValue());
                            double avgDays = payables.stream()
                                    .filter(inv -> entry.getKey().equals(inv.getCustomerName()))
                                    .mapToLong(inv -> inv.getDaysUntilDue())
                                    .average()
                                    .orElse(0.0);
                            creditor.put("daysUntilDue", (int) Math.round(avgDays));
                            return creditor;
                        })
                        .collect(Collectors.toList());
            }

            byte[] excel = excelExportService.exportDebtorsCreditors(debtors, creditors, type);
            HttpHeaders headers = new HttpHeaders();
            String filename = "debtors".equals(type) ? "Highest-Debtors.xlsx" : "Highest-Creditors.xlsx";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invalid date format. Use yyyy-MM-dd."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export Excel: " + e.getMessage()));
        }
    }

    @DeleteMapping("/session")
    public ResponseEntity<?> clearSession() {
        sessionDataService.clearAll();
        return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                "success", true,
                "message", "Session data cleared successfully"));
    }

    private List<Invoice> resolveInvoices() throws IOException {
        // Check mode and load data accordingly
        if (sessionDataService.isTallyMode()) {
            // Read the snapshot maintained by the 60-second live Tally poller.
            logger.info("💾 Resolve: Reading the current live Tally snapshot...");
            List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities = invoiceRepository.findBySource("TALLY");
            List<Invoice> invoices = convertEntitiesToInvoices(entities);
            logger.info("💾 Resolve: Retrieved {} TALLY invoices", invoices.size());
            sessionDataService.setInvoices(invoices);
            return invoices;
        } else {
            // EXCEL MODE - Prioritize Database for multi-user synchronization
            logger.info("💾 Resolve: Fetching EXCEL records from MySQL...");
            List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities = invoiceRepository.findBySource("EXCEL");
            
            if (!entities.isEmpty()) {
                List<Invoice> invoices = convertEntitiesToInvoices(entities);
                logger.info("💾 Resolve: Retrieved {} EXCEL invoices from MySQL", invoices.size());
                
                // Sync session state so UI components (like filename display) remain active
                sessionDataService.setInvoices(invoices);
                if (!sessionDataService.hasUploadedFile()) {
                    sessionDataService.setUploadedFileName("Shared Database Records");
                }
                return invoices;
            }

            logger.info("💾 Resolve: No EXCEL records in DB, checking session fallback...");
            // Fallback: Check if session has data (e.g. during a fresh upload process)
            List<Invoice> sessionInvoices = sessionDataService.getInvoices();
            if (sessionInvoices != null && !sessionInvoices.isEmpty()) {
                logger.info("💾 Resolve: Found {} invoices in session memory", sessionInvoices.size());
                return sessionInvoices;
            }

            // Ultimate Fallback: Reload from physical file if session is lost but path exists
            String filePath = sessionDataService.getSavedFilePath();
            if (StringUtils.hasText(filePath) && Files.exists(Paths.get(filePath))) {
                logger.info("💾 Resolve: Session lost, reloading from file: {}", filePath);
                List<Invoice> reloaded = excelReaderService.readExcelFile(filePath);
                sessionDataService.setInvoices(reloaded);
                return reloaded;
            }
            
            logger.info("💾 Resolve: No data found anywhere.");
            return sessionInvoices;
        }
    }

    private Invoice convertEntityToInvoice(com.ncpl.sales.cashflow.entity.InvoiceEntity entity) {
        Invoice invoice = new Invoice(
                entity.getCustomerName(),
                entity.getInvoiceNumber(),
                entity.getInvoiceDate(),
                entity.getDueDate(),
                entity.getInvoiceValue().doubleValue(), // Convert BigDecimal to double
                Invoice.InvoiceType.valueOf(entity.getInvoiceType().name()));
        invoice.setId(entity.getId());
        invoice.setReconciled(entity.isReconciled());
        return invoice;
    }

    private void applyBankBalance(OverviewAnalysisDTO dto) {
        if (dto != null) {
            dto.setBankBalance(sessionDataService.getBankBalance());
        }
    }

    private YearMonth parseYearMonth(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return YearMonth.parse(value);
    }

    private Integer normalizeWeek(Integer week) {
        if (week == null) {
            return null;
        }
        if (week < 1 || week > 6) {
            throw new IllegalArgumentException("Week value must be between 1 and 6.");
        }
        return week;
    }

    private List<Invoice> filterInvoices(List<Invoice> invoices, YearMonth month, Integer week, LocalDate fromDate,
            LocalDate toDate) {
        if (invoices == null) {
            return com.ncpl.sales.cashflow.util.Java8Collections.list();
        }

        // Priority: Date range > Month/Week
        if (fromDate != null || toDate != null) {
            return InvoiceFilterUtil.filterByDateRange(invoices, fromDate, toDate);
        }

        if (month == null && week == null) {
            return invoices;
        }
        return InvoiceFilterUtil.filterByMonthAndWeek(invoices, month, week);
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Trigger manual Tally 6.2 synchronization.
     * Reads from SME network Tally edit log and imports new invoices.
     * 
     * @return Sync status information
     */
    @PostMapping("/tally/sync")
    public ResponseEntity<Map<String, Object>> triggerTallySync(
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!Boolean.TRUE.equals(tallySyncService.getConfigStatus().get("enabled"))) {
                response.put("success", false);
                response.put("message", "Tally sync is disabled. Start ERP with the cashflow-test profile enabled.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            tallySyncService.syncTallyInvoices(force);
            invoicePipelineService.invalidateCache();
            response.put("success", true);
            response.put("message", "Tally synchronization triggered successfully");
            response.put("status", tallySyncService.getSyncStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to trigger sync: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get current Tally sync status and configuration.
     * 
     * @return Sync metadata and config status
     */
    @GetMapping("/tally/status")
    public ResponseEntity<Map<String, Object>> getTallySyncStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("syncStatus", tallySyncService.getSyncStatus());
            response.put("config", tallySyncService.getConfigStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get sync status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * NEW: Load all Tally-synced invoices from database and analyze as Overview
     * Allows users to view Tally data without uploading Excel files
     */
    @GetMapping("/tally/overview")
    public ResponseEntity<?> getTallyOverview() {
        try {
            List<com.ncpl.sales.cashflow.entity.InvoiceEntity> tallyInvoices = invoiceRepository.findBySource("TALLY");

            if (tallyInvoices == null || tallyInvoices.isEmpty()) {
                return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                        "hasData", false,
                        "message", "No Tally data synced yet. No invoices available."));
            }

            // Convert to Invoice model and analyze
            List<Invoice> invoices = convertEntitiesToInvoices(tallyInvoices);
            OverviewAnalysisDTO analysis = analysisService.analyzeForOverview(invoices);
            applyBankBalance(analysis);

            // Cache it
            sessionDataService.setInvoices(invoices);
            sessionDataService.setOverviewData(analysis);
            sessionDataService.setUploadedFileName("Tally 6.2 Sync");

            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "hasData", true,
                    "invoiceCount", tallyInvoices.size(),
                    "data", analysis));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to load Tally overview: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/outstanding")
    public ResponseEntity<?> getTallyOutstanding(
            @RequestParam(value = "type") String type) {
        try {
            Invoice.InvoiceType invoiceType = parseOutstandingType(type);
            List<Invoice> invoices = convertEntitiesToInvoices(invoiceRepository.findBySource("TALLY")).stream()
                    .filter(invoice -> invoice.getType() == invoiceType)
                    .sorted((left, right) -> Double.compare(right.getInvoiceValue(), left.getInvoiceValue()))
                    .collect(java.util.stream.Collectors.toList());

            List<Map<String, Object>> rows = invoices.stream().map(this::toOutstandingRow).collect(java.util.stream.Collectors.toList());
            double total = invoices.stream().mapToDouble(Invoice::getInvoiceValue).sum();
            long overdueCount = invoices.stream().filter(Invoice::isOverdue).count();
            double overdueAmount = invoices.stream().filter(Invoice::isOverdue)
                    .mapToDouble(Invoice::getInvoiceValue).sum();

            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "type", type.toLowerCase(Locale.ROOT),
                    "count", invoices.size(),
                    "total", total,
                    "overdueCount", overdueCount,
                    "overdueAmount", overdueAmount,
                    "rows", rows));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to load outstanding Tally bills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to load outstanding Tally bills: " + e.getMessage()));
        }
    }

    /**
     * Weekly forecast of currently outstanding Tally bills. Amounts are grouped only by due date;
     * no bank balance, payment assumption, or advance balance is included in this forecast.
     */
    @GetMapping("/tally/weekly-cashflow-forecast")
    public ResponseEntity<?> getWeeklyCashflowForecast(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "weekly") String granularity) {
        boolean daily;
        if ("weekly".equalsIgnoreCase(granularity)) {
            daily = false;
        } else if ("daily".equalsIgnoreCase(granularity)) {
            daily = true;
        } else {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Granularity must be weekly or daily."));
        }
        Map<String, com.ncpl.sales.cashflow.entity.InvoiceEntity> uniqueTallyBills = new LinkedHashMap<>();
        for (com.ncpl.sales.cashflow.entity.InvoiceEntity bill : invoiceRepository.findBySource("TALLY")) {
            uniqueTallyBills.putIfAbsent(forecastBillKey(bill), bill);
        }
        List<com.ncpl.sales.cashflow.entity.InvoiceEntity> allTallyBills = new ArrayList<>(uniqueTallyBills.values());
        List<com.ncpl.sales.cashflow.entity.InvoiceEntity> datedBills = allTallyBills.stream()
                .filter(bill -> bill.getDueDate() != null && bill.getInvoiceValue() != null)
                .collect(java.util.stream.Collectors.toList());

        if (datedBills.isEmpty()) {
            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "periods", com.ncpl.sales.cashflow.util.Java8Collections.list(),
                    "includedBillCount", 0,
                    "missingDueDateCount", allTallyBills.size(),
                    "totalReceivables", 0d,
                    "totalPayables", 0d,
                    "netPosition", 0d,
                    "finalCumulative", 0d));
        }

        LocalDate availableFrom = datedBills.stream().map(com.ncpl.sales.cashflow.entity.InvoiceEntity::getDueDate)
                .min(Comparator.naturalOrder()).orElseThrow(java.util.NoSuchElementException::new);
        LocalDate availableTo = datedBills.stream().map(com.ncpl.sales.cashflow.entity.InvoiceEntity::getDueDate)
                .max(Comparator.naturalOrder()).orElseThrow(java.util.NoSuchElementException::new);
        // Open invoices remain part of the cash-flow backlog even when their due date is in the past.
        // Default to the oldest outstanding due date so Finance sees the complete unpaid history.
        LocalDate selectedFrom = from == null ? availableFrom : from;
        LocalDate selectedTo = to == null ? availableTo : to;
        if (selectedFrom.isAfter(selectedTo)) {
            if (from == null && to == null) {
                selectedTo = selectedFrom;
            } else {
                return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "From date must be on or before To date."));
            }
        }
        final LocalDate rangeFrom = selectedFrom;
        final LocalDate rangeTo = selectedTo;

        Map<LocalDate, WeeklyForecastAccumulator> byWeek = new LinkedHashMap<>();
        List<com.ncpl.sales.cashflow.entity.InvoiceEntity> includedBills = datedBills.stream()
                .filter(bill -> !bill.getDueDate().isBefore(rangeFrom) && !bill.getDueDate().isAfter(rangeTo))
                .sorted(Comparator.comparing(com.ncpl.sales.cashflow.entity.InvoiceEntity::getDueDate))
                .collect(java.util.stream.Collectors.toList());
        for (com.ncpl.sales.cashflow.entity.InvoiceEntity bill : includedBills) {
            LocalDate bucketStart = daily ? bill.getDueDate() : bill.getDueDate().with(DayOfWeek.MONDAY);
            WeeklyForecastAccumulator bucket = byWeek.computeIfAbsent(bucketStart, ignored -> new WeeklyForecastAccumulator());
            double amount = bill.getInvoiceValue().doubleValue();
            if (bill.getInvoiceType() == com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.INFLOW) {
                bucket.receivables += amount;
            } else if (bill.getInvoiceType() == com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.OUTFLOW) {
                bucket.payables += amount;
            }
            bucket.bills.add(bill);
            bucket.billCount++;
        }

        List<Map<String, Object>> periods = new ArrayList<>();
        double cumulative = 0d;
        double totalReceivables = 0d;
        double totalPayables = 0d;
        for (Map.Entry<LocalDate, WeeklyForecastAccumulator> entry : byWeek.entrySet()) {
            WeeklyForecastAccumulator bucket = entry.getValue();
            cumulative += bucket.receivables - bucket.payables;
            totalReceivables += bucket.receivables;
            totalPayables += bucket.payables;
            Map<String, Object> period = new LinkedHashMap<>();
            period.put("weekStart", entry.getKey().toString());
            period.put("weekEnd", (daily ? entry.getKey() : entry.getKey().plusDays(6)).toString());
            period.put("receivables", bucket.receivables);
            period.put("payables", bucket.payables);
            period.put("net", bucket.receivables - bucket.payables);
            period.put("cumulative", cumulative);
            period.put("billCount", bucket.billCount);
            period.put("receivableParties", summarizeForecastParties(bucket.bills,
                    com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.INFLOW));
            period.put("payableParties", summarizeForecastParties(bucket.bills,
                    com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.OUTFLOW));
            period.put("bills", bucket.bills.stream()
                    .sorted(Comparator.comparing(com.ncpl.sales.cashflow.entity.InvoiceEntity::getDueDate)
                            .thenComparing(bill -> bill.getCustomerName() == null ? "" : bill.getCustomerName(), String.CASE_INSENSITIVE_ORDER))
                    .map(this::toForecastBill)
                    .collect(java.util.stream.Collectors.toList()));
            periods.add(period);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("from", rangeFrom.toString());
        response.put("to", rangeTo.toString());
        response.put("granularity", daily ? "daily" : "weekly");
        response.put("availableFrom", availableFrom.toString());
        response.put("availableTo", availableTo.toString());
        response.put("periods", periods);
        response.put("includedBillCount", includedBills.size());
        response.put("missingDueDateCount", allTallyBills.size() - datedBills.size());
        response.put("totalReceivables", totalReceivables);
        response.put("totalPayables", totalPayables);
        response.put("netPosition", totalReceivables - totalPayables);
        response.put("finalCumulative", cumulative);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tally/weekly-cashflow-forecast/export")
    public ResponseEntity<?> exportWeeklyCashflowForecast(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "error", "From date must be on or before To date."));
        }
        try {
            Map<String, com.ncpl.sales.cashflow.entity.InvoiceEntity> uniqueEntities = new LinkedHashMap<>();
            for (com.ncpl.sales.cashflow.entity.InvoiceEntity bill : invoiceRepository.findBySource("TALLY")) {
                uniqueEntities.putIfAbsent(forecastBillKey(bill), bill);
            }
            List<Invoice> invoices = convertEntitiesToInvoices(new ArrayList<>(uniqueEntities.values())).stream()
                    .filter(invoice -> invoice.getDueDate() != null)
                    .filter(invoice -> !invoice.getDueDate().isBefore(from) && !invoice.getDueDate().isAfter(to))
                    .sorted(Comparator.comparing(Invoice::getDueDate)
                            .thenComparing(invoice -> invoice.getCustomerName() == null ? "" : invoice.getCustomerName(), String.CASE_INSENSITIVE_ORDER))
                    .collect(java.util.stream.Collectors.toList());
            List<Invoice> receivables = invoices.stream()
                    .filter(invoice -> invoice.getType() == Invoice.InvoiceType.INFLOW)
                    .collect(java.util.stream.Collectors.toList());
            List<Invoice> payables = invoices.stream()
                    .filter(invoice -> invoice.getType() == Invoice.InvoiceType.OUTFLOW)
                    .collect(java.util.stream.Collectors.toList());
            byte[] excel = excelExportService.exportForecastInvoices(receivables, payables,
                    "Cash Flow Invoice Detail " + from + " to " + to);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=Cash-Flow-Invoice-Detail-" + from + "-to-" + to + ".xlsx");
            return ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (Exception e) {
            logger.error("Failed to export cash-flow invoice detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Unable to export invoice detail: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/invoice-pipeline")
    public ResponseEntity<?> getInvoicePipeline(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "error", "From date must be on or before To date."));
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(from, to) > 370) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "error", "Select a period of one year or less. Use the date filter to inspect older history."));
        }
        try {
            return ResponseEntity.ok(invoicePipelineService.load(from, to));
        } catch (Exception e) {
            logger.error("Failed to build the GRN-to-invoice pipeline", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    com.ncpl.sales.cashflow.util.Java8Collections.map(
                            "error", "Invoice pipeline is unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/invoice-pipeline/export")
    public ResponseEntity<?> exportInvoicePipeline(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from.isAfter(to) || java.time.temporal.ChronoUnit.DAYS.between(from, to) > 370) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "error", "Select a valid period of one year or less."));
        }
        try {
            Map<String, Object> pipeline = invoicePipelineService.load(from, to);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) pipeline.get("records");
            byte[] excel = excelExportService.exportInvoicePipeline(records,
                    "PO / SO Invoice Pipeline " + from + " to " + to);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=PO-SO-Invoice-Pipeline-" + from + "-to-" + to + ".xlsx");
            return ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (Exception e) {
            logger.error("Failed to export invoice pipeline", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    com.ncpl.sales.cashflow.util.Java8Collections.map(
                            "error", "Unable to export invoice pipeline: " + e.getMessage()));
        }
    }

    private List<Map<String, Object>> summarizeForecastParties(
            List<com.ncpl.sales.cashflow.entity.InvoiceEntity> bills,
            com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType type) {
        Map<String, PartyForecastAccumulator> totals = new LinkedHashMap<>();
        for (com.ncpl.sales.cashflow.entity.InvoiceEntity bill : bills) {
            if (bill.getInvoiceType() != type) continue;
            String party = bill.getCustomerName() == null || bill.getCustomerName().trim().isEmpty()
                    ? "Unspecified party" : bill.getCustomerName().trim();
            PartyForecastAccumulator total = totals.computeIfAbsent(party, ignored -> new PartyForecastAccumulator());
            total.amount += bill.getInvoiceValue() == null ? 0d : bill.getInvoiceValue().doubleValue();
            total.billCount++;
        }
        return totals.entrySet().stream()
                .sorted((left, right) -> Double.compare(right.getValue().amount, left.getValue().amount))
                .map(entry -> com.ncpl.sales.cashflow.util.Java8Collections.<String, Object>map(
                        "name", entry.getKey(), "amount", entry.getValue().amount,
                        "billCount", entry.getValue().billCount))
                .collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> toForecastBill(com.ncpl.sales.cashflow.entity.InvoiceEntity bill) {
        LocalDate today = LocalDate.now();
        boolean overdue = bill.getDueDate() != null && bill.getDueDate().isBefore(today);
        long ageingDays = bill.getInvoiceDate() == null ? 0L
                : Math.max(0L, java.time.temporal.ChronoUnit.DAYS.between(bill.getInvoiceDate(), today));
        long daysOverdue = overdue
                ? java.time.temporal.ChronoUnit.DAYS.between(bill.getDueDate(), today) : 0L;
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", bill.getId());
        row.put("direction", bill.getInvoiceType() == com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.INFLOW
                ? "RECEIVABLE" : "PAYABLE");
        row.put("partyName", bill.getCustomerName());
        row.put("invoiceNumber", bill.getInvoiceNumber());
        row.put("invoiceDate", bill.getInvoiceDate());
        row.put("dueDate", bill.getDueDate());
        row.put("amount", bill.getInvoiceValue());
        row.put("status", overdue ? "Overdue" : "Due");
        row.put("dueAmount", overdue ? 0d : bill.getInvoiceValue());
        row.put("overdueAmount", overdue ? bill.getInvoiceValue() : 0d);
        row.put("ageingDays", ageingDays);
        row.put("daysOverdue", daysOverdue);
        return row;
    }

    private String forecastBillKey(com.ncpl.sales.cashflow.entity.InvoiceEntity bill) {
        return String.join("|",
                bill.getInvoiceType() == null ? "" : bill.getInvoiceType().name(),
                bill.getCustomerName() == null ? "" : bill.getCustomerName().trim().toLowerCase(Locale.ROOT),
                bill.getInvoiceNumber() == null ? "" : bill.getInvoiceNumber().trim().toLowerCase(Locale.ROOT),
                bill.getInvoiceDate() == null ? "" : bill.getInvoiceDate().toString(),
                bill.getDueDate() == null ? "" : bill.getDueDate().toString(),
                bill.getInvoiceValue() == null ? "" : bill.getInvoiceValue().stripTrailingZeros().toPlainString());
    }

    private static final class WeeklyForecastAccumulator {
        private double receivables;
        private double payables;
        private int billCount;
        private final List<com.ncpl.sales.cashflow.entity.InvoiceEntity> bills = new ArrayList<>();
    }

    private static final class PartyForecastAccumulator {
        private double amount;
        private int billCount;
    }

    @GetMapping("/tally/advances")
    public ResponseEntity<?> getTallyAdvances() {
        try {
            com.ncpl.sales.cashflow.service.TallyLiveService.AdvanceSnapshot snapshot = tallySyncService.fetchLiveAdvanceBalances();
            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "customerAdvances", snapshot.customerAdvances(),
                    "supplierAdvances", snapshot.supplierAdvances(),
                    "customerAdvanceCount", snapshot.customerAdvances().size(),
                    "supplierAdvanceCount", snapshot.supplierAdvances().size(),
                    "customerAdvanceTotal", snapshot.customerAdvanceTotal(),
                    "supplierAdvanceTotal", snapshot.supplierAdvanceTotal(),
                    "classificationRule", "Sundry Debtors with credit/payable balances are customer advances; Sundry Creditors with debit/receivable balances are supplier advances."));
        } catch (Exception e) {
            logger.error("Failed to load live Tally advance balances", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Tally advance balances are unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/cost-centres")
    public ResponseEntity<?> getTallyCostCentres() {
        try {
            return ResponseEntity.ok(tallySyncService.fetchLiveCostCentres());
        } catch (Exception e) {
            logger.error("Failed to load live Tally Cost Centres", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Tally Cost Centres are unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/cost-centres/transactions")
    public ResponseEntity<?> getTallyCostCentreTransactions(
            @RequestParam String costCentre,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity.ok(tallySyncService.fetchLiveCostCentreTransactions(costCentre, from, to));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to load live Tally Cost Centre transactions", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Cost Centre transactions are unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/cost-centres/site-profitability")
    public ResponseEntity<?> getTallySiteProfitability(
            @RequestParam String site,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity.ok(tallySyncService.fetchLiveSiteProfitability(site, from, to));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to load live Tally site profitability", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Site profitability is unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/cost-centres/voucher-details")
    public ResponseEntity<?> getTallyCostCentreVoucherDetails(
            @RequestParam String voucherNumber,
            @RequestParam(required = false, defaultValue = "") String partyName,
            @RequestParam String voucherDate) {
        try {
            if (voucherNumber == null || voucherNumber.codePoints().allMatch(Character::isWhitespace)) {
                return ResponseEntity.badRequest()
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "A voucher number is required for a safe Tally lookup."));
            }
            LocalDate requestedDate = LocalDate.parse(voucherDate);
            com.ncpl.sales.cashflow.service.TallyLiveService.VoucherDetails voucher = tallySyncService.fetchLiveVoucherDetails(
                    voucherNumber.trim(), partyName == null ? "" : partyName.trim(), requestedDate);
            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map("voucher", voucher));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Voucher date must use YYYY-MM-DD format."));
        } catch (Exception e) {
            logger.error("Failed to load live Tally Cost Centre voucher details", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Tally voucher details are unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/cost-centres/site-report/export")
    public ResponseEntity<?> exportTallySiteReport(
            @RequestParam String site,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            com.ncpl.sales.cashflow.service.TallyLiveService.SiteProfitability report = tallySyncService.fetchLiveSiteProfitability(site, from, to);
            byte[] excel = excelExportService.exportSiteReport(report);
            String safeSite = site.replaceAll("[^A-Za-z0-9._-]+", "-").replaceAll("^-+|-+$", "");
            if (safeSite.codePoints().allMatch(Character::isWhitespace)) safeSite = "Site";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + safeSite + "-Tally-Site-Report.xlsx");
            return ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to export live Tally site report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export Tally site report: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/advances/details")
    public ResponseEntity<?> getTallyAdvanceDetails(
            @RequestParam String partyName,
            @RequestParam String type) {
        try {
            com.ncpl.sales.cashflow.service.TallyLiveService.AdvanceType advanceType = com.ncpl.sales.cashflow.service.TallyLiveService.AdvanceType.valueOf(type);
            return ResponseEntity.ok(tallySyncService.fetchLiveAdvanceDetails(partyName, advanceType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to load live Tally advance details", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Tally advance details are unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/advances/export")
    public ResponseEntity<?> exportTallyAdvances(
            @RequestParam(value = "type", required = false, defaultValue = "all") String type,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        try {
            com.ncpl.sales.cashflow.service.TallyLiveService.AdvanceSnapshot snapshot = tallySyncService.fetchLiveAdvanceBalances();
            List<com.ncpl.sales.cashflow.service.TallyLiveService.AdvanceBalance> rows = new ArrayList<>();
            rows.addAll(snapshot.customerAdvances());
            rows.addAll(snapshot.supplierAdvances());
            String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
            rows = rows.stream()
                    .filter(row -> "all".equalsIgnoreCase(type) || row.type().name().equalsIgnoreCase(type))
                    .filter(row -> normalizedSearch.isEmpty()
                            || safeLower(row.partyName()).contains(normalizedSearch)
                            || safeLower(row.ledgerGroup()).contains(normalizedSearch))
                    .sorted((left, right) -> Double.compare(right.amount(), left.amount()))
                    .collect(java.util.stream.Collectors.toList());

            String label = "all".equalsIgnoreCase(type) ? "All Advances"
                    : "CUSTOMER_ADVANCE_RECEIVED".equalsIgnoreCase(type)
                    ? "Customer Advances Received" : "Supplier Advances Paid";
            byte[] excel = excelExportService.exportAdvances(rows, label);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + label.replace(' ', '-') + ".xlsx");
            return ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (Exception e) {
            logger.error("Failed to export Tally advances", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export Tally advances: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/outstanding/export")
    public ResponseEntity<?> exportTallyOutstanding(
            @RequestParam(value = "type") String type,
            @RequestParam(value = "aging", required = false, defaultValue = "all") String aging,
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @RequestParam(value = "party", required = false, defaultValue = "all") String party,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "false") boolean retentionOnly) {
        try {
            Invoice.InvoiceType invoiceType = parseOutstandingType(type);
            String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
            List<Invoice> invoices = convertEntitiesToInvoices(invoiceRepository.findBySource("TALLY")).stream()
                    .filter(invoice -> invoice.getType() == invoiceType)
                    .filter(invoice -> !retentionOnly || com.ncpl.sales.cashflow.service.RetentionLedgerClassifier.isRetention(invoice.getCustomerName()))
                    .filter(invoice -> matchesOutstandingAging(invoice, aging))
                    .filter(invoice -> "all".equalsIgnoreCase(status)
                            || ("overdue".equalsIgnoreCase(status) && invoice.isOverdue())
                            || ("current".equalsIgnoreCase(status) && !invoice.isOverdue()))
                    .filter(invoice -> "all".equalsIgnoreCase(party)
                            || party.equalsIgnoreCase(invoice.getCustomerName()))
                    .filter(invoice -> normalizedSearch.isEmpty()
                            || safeLower(invoice.getCustomerName()).contains(normalizedSearch)
                            || safeLower(invoice.getInvoiceNumber()).contains(normalizedSearch))
                    .sorted((left, right) -> Double.compare(right.getInvoiceValue(), left.getInvoiceValue()))
                    .collect(java.util.stream.Collectors.toList());

            String label = invoiceType == Invoice.InvoiceType.INFLOW
                    ? "Outstanding Receivables" : "Outstanding Payables";
            byte[] excel = excelExportService.exportOutstandingInvoices(invoices, label);
            String filename = label.replace(' ', '-') + ".xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            return ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to export outstanding Tally bills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to export outstanding Tally bills: " + e.getMessage()));
        }
    }

    @GetMapping("/tally/outstanding/details")
    public ResponseEntity<?> getTallyOutstandingDetails(
            @RequestParam String billNumber,
            @RequestParam String partyName,
            @RequestParam String invoiceDate) {
        try {
            LocalDate requestedDate = LocalDate.parse(invoiceDate);
            com.ncpl.sales.cashflow.entity.InvoiceEntity invoice = invoiceRepository.findBySource("TALLY").stream()
                    .filter(item -> billNumber.equalsIgnoreCase(item.getInvoiceNumber()))
                    .filter(item -> partyName.equalsIgnoreCase(item.getCustomerName()))
                    .filter(item -> requestedDate.equals(item.getInvoiceDate()))
                    .findFirst()
                    .orElse(null);
            if (invoice == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "This bill is not present in the current Tally outstanding snapshot."));
            }

            com.ncpl.sales.cashflow.service.TallyLiveService.VoucherDetails voucher = tallySyncService.fetchLiveVoucherDetails(
                    invoice.getInvoiceNumber(), invoice.getCustomerName(), invoice.getInvoiceDate());
            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "bill", com.ncpl.sales.cashflow.util.Java8Collections.map(
                            "id", invoice.getId(),
                            "partyName", invoice.getCustomerName(),
                            "billNumber", invoice.getInvoiceNumber(),
                            "invoiceDate", invoice.getInvoiceDate(),
                            "dueDate", invoice.getDueDate(),
                            "outstandingBalance", invoice.getInvoiceValue(),
                            "type", invoice.getInvoiceType()),
                    "voucher", voucher));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Invoice date must use YYYY-MM-DD format."));
        } catch (Exception e) {
            logger.error("Failed to load live Tally voucher details", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Tally voucher details are unavailable: " + e.getMessage()));
        }
    }

    private Invoice.InvoiceType parseOutstandingType(String type) {
        if ("receivables".equalsIgnoreCase(type)) return Invoice.InvoiceType.INFLOW;
        if ("payables".equalsIgnoreCase(type)) return Invoice.InvoiceType.OUTFLOW;
        throw new IllegalArgumentException("Type must be receivables or payables");
    }

    private Map<String, Object> toOutstandingRow(Invoice invoice) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", invoice.getId());
        row.put("partyName", invoice.getCustomerName());
        row.put("invoiceNumber", invoice.getInvoiceNumber());
        row.put("invoiceDate", invoice.getInvoiceDate());
        row.put("dueDate", invoice.getDueDate());
        row.put("billAgeDays", invoice.getBillAgeDays());
        row.put("amount", invoice.getInvoiceValue());
        boolean retention = com.ncpl.sales.cashflow.service.RetentionLedgerClassifier.isRetention(invoice.getCustomerName());
        row.put("retention", retention);
        row.put("retentionAmount", retention ? invoice.getInvoiceValue() : 0d);
        row.put("status", invoice.isOverdue() ? "Overdue" : "Current");
        row.put("daysOverdue", invoice.getDaysOverdue());
        row.put("daysUntilDue", invoice.getDaysUntilDue() == Long.MAX_VALUE ? null : invoice.getDaysUntilDue());
        row.put("agingBucket", outstandingAgingKey(invoice));
        return row;
    }

    private boolean matchesOutstandingAging(Invoice invoice, String aging) {
        return aging == null || aging.codePoints().allMatch(Character::isWhitespace) || "all".equalsIgnoreCase(aging)
                || outstandingAgingKey(invoice).equalsIgnoreCase(aging);
    }

    private String outstandingAgingKey(Invoice invoice) {
        if (!invoice.isOverdue()) return "current";
        long days = invoice.getDaysOverdue();
        if (days <= 30) return "0-30";
        if (days <= 60) return "31-60";
        if (days <= 90) return "61-90";
        return "90+";
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    /**
     * Load Tally invoices for Analytics view
     */
    @GetMapping("/tally/analytics")
    public ResponseEntity<?> getTallyAnalytics() {
        try {
            List<com.ncpl.sales.cashflow.entity.InvoiceEntity> tallyInvoices = invoiceRepository.findBySource("TALLY");

            if (tallyInvoices == null || tallyInvoices.isEmpty()) {
                return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map("hasData", false, "message", "No Tally data available"));
            }

            List<Invoice> invoices = convertEntitiesToInvoices(tallyInvoices);
            AnalyticsDTO analytics = analyticsService.analyzeMonthlyAndWeekly(invoices);

            sessionDataService.setInvoices(invoices);
            sessionDataService.setAnalyticsData(analytics);

            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map("hasData", true, "invoiceCount", tallyInvoices.size(), "data", analytics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to load Tally analytics: " + e.getMessage()));
        }
    }

    /**
     * Load Tally invoice count and data availability status
     */
    @GetMapping("/tally/data-status")
    public ResponseEntity<?> getTallyDataStatus() {
        try {
            Long tallyCount = invoiceRepository.countBySource("TALLY");
            return ResponseEntity.ok(com.ncpl.sales.cashflow.util.Java8Collections.map(
                    "hasTallyData", tallyCount > 0,
                    "invoiceCount", tallyCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.ncpl.sales.cashflow.util.Java8Collections.map("error", "Failed to check Tally data status: " + e.getMessage()));
        }
    }

    /**
     * Helper: Convert InvoiceEntity (JPA) to Invoice (model)
     */
    private List<Invoice> convertEntitiesToInvoices(List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::convertEntityToInvoice)
                .collect(Collectors.toList());
    }

    /**
     * Get all recurring transactions
     */
    @GetMapping("/recurring-transactions")
    public ResponseEntity<?> getRecurringTransactions() {
        try {
            List<com.ncpl.sales.cashflow.entity.RecurringTransactionEntity> transactions = recurringTransactionService
                    .getAllActiveTransactions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            response.put("totalMonthly", recurringTransactionService.getTotalMonthlyAmount());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("transactions", new ArrayList<>());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create a new recurring transaction
     */
    @PostMapping("/recurring-transactions")
    public ResponseEntity<?> createRecurringTransaction(
            @RequestBody com.ncpl.sales.cashflow.entity.RecurringTransactionEntity transaction) {
        try {
            com.ncpl.sales.cashflow.entity.RecurringTransactionEntity saved = recurringTransactionService
                    .createRecurringTransaction(transaction);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recurring transaction created successfully");
            response.put("id", saved.getId());
            response.put("transaction", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update a recurring transaction
     */
    @PutMapping("/recurring-transactions/{id}")
    public ResponseEntity<?> updateRecurringTransaction(@PathVariable Long id,
            @RequestBody com.ncpl.sales.cashflow.entity.RecurringTransactionEntity transaction) {
        try {
            com.ncpl.sales.cashflow.entity.RecurringTransactionEntity updated = recurringTransactionService
                    .updateRecurringTransaction(id, transaction);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recurring transaction updated successfully");
            response.put("transaction", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete a recurring transaction (soft delete)
     */
    @DeleteMapping("/recurring-transactions/{id}")
    public ResponseEntity<?> deleteRecurringTransaction(@PathVariable Long id) {
        try {
            recurringTransactionService.deleteRecurringTransaction(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recurring transaction deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
