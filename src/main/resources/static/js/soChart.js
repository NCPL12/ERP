/**
 * NCPL Sales Order Monthly Chart - WITH BAR, PIE & DONUT CHARTS
 * Updated: Vibrant colors for Pie & Donut charts
 */

var chart; // Global bar chart variable
var pieChart; // Global pie chart variable
var donutChart; // Global donut chart variable

$(document).ready(function () {
    var year = $("#soYear").val();
    getSoChart(year);
    
    $("#soYear").on("change", function(){
        var year = $(this).val();
        getSoChart(year);
    });
});

function getSoChart(year){
 
    $.ajax({
        type: 'GET',
        url: api.SO_MOTNHLY_REPORT + "?year=" + year,
        dataType: 'json',
        async: false,
        success: function(response){
            drawSoChart(response);
            drawSoPieChart(response);
            drawSoDonutChart(response);
            loadSoTable(response);
        },  
        complete: function(resp){
            if(resp.status == 500){
                $.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : " + resp.responseJSON["errorMessage"]);
            }
        },
        error: function(e) {
            console.log(e);
        }  
    }); 
}

function loadSoTable(result){
    $("#soMonthlyTotalTable tbody").empty();
    $("#total").empty();
    
    var year = $("#soYear").val();
    var nextYear = $("#soYear option:selected").text().split("-")[1];
    
    var dcItems = 
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>APRIL -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.APRIL.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>MAY -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.MAY.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>JUNE -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.JUNE.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>JULY -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.JULY.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>AUGUST -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.AUGUST.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>SEPTEMBER -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.SEPTEMBER.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>OCTOBER -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.OCTOBER.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>NOVEMBER -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.NOVEMBER.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>DECEMBER -" + year + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.DECEMBER.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>JANUARY -" + nextYear + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.JANUARY.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>FEBRUARY -" + nextYear + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.FEBRUARY.toFixed()) + "</td></tr>" +
        "<tr><td style='padding:10px;font-weight:500;border:1px solid #ddd;'>MARCH -" + nextYear + "</td><td align='right' style='padding:10px;border:1px solid #ddd;'>" + commaSeparateNumber(result.value.MARCH.toFixed()) + "</td></tr>";
    
    $("#soMonthlyTotalTable tbody").append(dcItems);
    
    var totalValue = parseFloat(result.value.APRIL.toFixed()) + parseFloat(result.value.MAY.toFixed()) + parseFloat(result.value.JUNE.toFixed()) + 
                     parseFloat(result.value.JULY.toFixed()) + parseFloat(result.value.AUGUST.toFixed()) + parseFloat(result.value.SEPTEMBER.toFixed()) + 
                     parseFloat(result.value.OCTOBER.toFixed()) + parseFloat(result.value.NOVEMBER.toFixed()) + parseFloat(result.value.DECEMBER.toFixed()) + 
                     parseFloat(result.value.JANUARY.toFixed()) + parseFloat(result.value.FEBRUARY.toFixed()) + parseFloat(result.value.MARCH.toFixed());
    
    totalValue = Math.round(totalValue * 100) / 100;
    
    var total = "<tr><td style='font-weight:700;font-size:16px;padding:12px;background:#f8f9fa;border-top:2px solid #17a2b8;'>Total</td><td align='right' style='font-weight:700;font-size:16px;padding:12px;background:#f8f9fa;border-top:2px solid #17a2b8;'>" + commaSeparateNumber(totalValue) + "</td></tr>";
    $("#total").append(total);
}

function drawSoChart(result){
    
    am4core.ready(function() {
        
        // Dispose previous chart if exists
        if (chart) {
            chart.dispose();
        }
        
        $("#chartdiv").css("width", "100%");
        
        // Themes begin
        am4core.useTheme(am4themes_animated);
        // Themes end

        // Create chart instance
        chart = am4core.create("chartdiv", am4charts.XYChart);
        
        // REMOVE AMCHARTS LOGO
        chart.logo.disabled = true;
        
        // Add title
        var title = chart.titles.create();
        title.text = "SALES ORDER MONTHLY CHART - FY " + $("#soYear").val() + "-" + $("#soYear option:selected").text().split("-")[1];
        title.fontSize = 20;
        title.fontWeight = "600";
        title.marginBottom = 20;
															 
	  
				   
															 
	  
				   
															   
	  
				   
																  
	  
				   
																
	  
				   
																 
	  
				   
																 
	  
				   
																
	  
				   
																 

        // Add data - USE RAW NUMBERS, NOT FORMATTED STRINGS
        chart.data = [
            { "Month": "Apr", "Total": parseFloat(result.value.APRIL.toFixed()), "formatted": commaSeparateNumber(result.value.APRIL.toFixed()) },
            { "Month": "May", "Total": parseFloat(result.value.MAY.toFixed()), "formatted": commaSeparateNumber(result.value.MAY.toFixed()) },
            { "Month": "Jun", "Total": parseFloat(result.value.JUNE.toFixed()), "formatted": commaSeparateNumber(result.value.JUNE.toFixed()) },
            { "Month": "Jul", "Total": parseFloat(result.value.JULY.toFixed()), "formatted": commaSeparateNumber(result.value.JULY.toFixed()) },
            { "Month": "Aug", "Total": parseFloat(result.value.AUGUST.toFixed()), "formatted": commaSeparateNumber(result.value.AUGUST.toFixed()) },
            { "Month": "Sep", "Total": parseFloat(result.value.SEPTEMBER.toFixed()), "formatted": commaSeparateNumber(result.value.SEPTEMBER.toFixed()) },
            { "Month": "Oct", "Total": parseFloat(result.value.OCTOBER.toFixed()), "formatted": commaSeparateNumber(result.value.OCTOBER.toFixed()) },
            { "Month": "Nov", "Total": parseFloat(result.value.NOVEMBER.toFixed()), "formatted": commaSeparateNumber(result.value.NOVEMBER.toFixed()) },
            { "Month": "Dec", "Total": parseFloat(result.value.DECEMBER.toFixed()), "formatted": commaSeparateNumber(result.value.DECEMBER.toFixed()) },
            { "Month": "Jan", "Total": parseFloat(result.value.JANUARY.toFixed()), "formatted": commaSeparateNumber(result.value.JANUARY.toFixed()) },
            { "Month": "Feb", "Total": parseFloat(result.value.FEBRUARY.toFixed()), "formatted": commaSeparateNumber(result.value.FEBRUARY.toFixed()) },
            { "Month": "Mar", "Total": parseFloat(result.value.MARCH.toFixed()), "formatted": commaSeparateNumber(result.value.MARCH.toFixed()) }
        ];

        // Create category axis
        let categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
        categoryAxis.dataFields.category = "Month";
        categoryAxis.title.text = "Month";
        categoryAxis.title.fontWeight = "600";
        categoryAxis.renderer.grid.template.location = 0;
        categoryAxis.renderer.minGridDistance = 30;
        categoryAxis.renderer.labels.template.rotation = -45;
        categoryAxis.renderer.labels.template.horizontalCenter = "right";
        categoryAxis.renderer.labels.template.verticalCenter = "middle";
        categoryAxis.renderer.labels.template.fontSize = 12;

        // Create value axis
        let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
        valueAxis.title.text = "Total";
        valueAxis.title.fontWeight = "600";
        valueAxis.renderer.minWidth = 50;

        // Create series
        let series = chart.series.push(new am4charts.ColumnSeries());
        series.name = "Monthly Sales";
        series.dataFields.categoryX = "Month";
        series.dataFields.valueY = "Total";
        
        // IMPROVED TOOLTIP - Show formatted value
        series.columns.template.tooltipText = "{Month}\n[bold fontSize:14]₹{formatted}[/]";
        series.tooltip.pointerOrientation = "vertical";
        series.tooltip.dy = -10;
        
        // Column styling
        series.columns.template.fillOpacity = 0.9;
        series.columns.template.fill = am4core.color("#17a2b8");
        series.columns.template.stroke = am4core.color("#17a2b8");
        series.columns.template.strokeWidth = 2;
        series.columns.template.strokeOpacity = 1;
        
        // ROUNDED CORNERS
        series.columns.template.column.cornerRadiusTopLeft = 5;
        series.columns.template.column.cornerRadiusTopRight = 5;
        
        // Column width
        series.columns.template.width = am4core.percent(60);
        
        // HOVER STATE
        let hoverState = series.columns.template.column.states.create("hover");
        hoverState.properties.fillOpacity = 1;
        hoverState.properties.fill = am4core.color("#0e7c8f");
        hoverState.properties.shadowBlur = 10;
        hoverState.properties.shadowOffsetX = 0;
        hoverState.properties.shadowOffsetY = 5;
        hoverState.properties.shadowColor = am4core.color("#000000");
        hoverState.properties.shadowOpacity = 0.3;
        
        // Add cursor
        chart.cursor = new am4charts.XYCursor();
        chart.cursor.lineX.disabled = true;
        chart.cursor.lineY.disabled = true;
        
        // Enable responsive
        chart.responsive.enabled = true;
        
        // Animation on load
        series.columns.template.adapter.add("fill", function(fill, target) {
            return chart.colors.getIndex(target.dataItem.index);
        });
        
    }); 
}

// ==================== PIE CHART FUNCTION WITH COLORS ====================
function drawSoPieChart(result) {
    
    am4core.ready(function() {
        
        // Dispose previous pie chart if exists
        if (pieChart) {
            pieChart.dispose();
        }
        
        // Themes
        am4core.useTheme(am4themes_animated);
        
        // Create pie chart instance
        pieChart = am4core.create("pieChartdiv", am4charts.PieChart);
        
        // REMOVE AMCHARTS LOGO
        pieChart.logo.disabled = true;
        
        // Add title
        var title = pieChart.titles.create();
        title.text = "SALES DISTRIBUTION - FY " + $("#soYear").val() + "-" + $("#soYear option:selected").text().split("-")[1];
        title.fontSize = 18;
        title.fontWeight = "600";
        title.marginBottom = 15;
        
        // Prepare data WITH VIBRANT COLORS - 12 months
        var pieData = [];
        var monthColors = [
            { name: "April", value: result.value.APRIL, color: "#FF6B6B" },
            { name: "May", value: result.value.MAY, color: "#4ECDC4" },
            { name: "June", value: result.value.JUNE, color: "#FFD93D" },
            { name: "July", value: result.value.JULY, color: "#6C5CE7" },
            { name: "August", value: result.value.AUGUST, color: "#F38181" },
            { name: "September", value: result.value.SEPTEMBER, color: "#AA96DA" },
            { name: "October", value: result.value.OCTOBER, color: "#FCBAD3" },
            { name: "November", value: result.value.NOVEMBER, color: "#A8D8EA" },
            { name: "December", value: result.value.DECEMBER, color: "#FFE66D" },
            { name: "January", value: result.value.JANUARY, color: "#95E1D3" },
            { name: "February", value: result.value.FEBRUARY, color: "#F7DC6F" },
            { name: "March", value: result.value.MARCH, color: "#BB8FCE" }
        ];
        
        // Filter out months with zero values
        monthColors.forEach(function(month) {
            if (month.value > 0) {
                pieData.push({
                    "Month": month.name,
                    "Total": parseFloat(month.value.toFixed()),
                    "formatted": commaSeparateNumber(month.value.toFixed()),
                    "color": month.color
                });
            }
        });
        
        pieChart.data = pieData;
        
        // Create pie series
        var pieSeries = pieChart.series.push(new am4charts.PieSeries());
        pieSeries.dataFields.value = "Total";
        pieSeries.dataFields.category = "Month";
        
        // Slice styling
        pieSeries.slices.template.stroke = am4core.color("#fff");
        pieSeries.slices.template.strokeWidth = 2;
        pieSeries.slices.template.strokeOpacity = 1;
        
        // ==================== CUSTOM COLORS FOR PIE CHART ====================
        pieSeries.slices.template.adapter.add("fill", function(fill, target) {
            if (target.dataItem && target.dataItem.dataContext && target.dataItem.dataContext.color) {
                return am4core.color(target.dataItem.dataContext.color);
            }
            return fill;
        });
        // ==================== END CUSTOM COLORS ====================
        
        // Hover state
        var hoverState = pieSeries.slices.template.states.getKey("hover");
        hoverState.properties.scale = 1.05;
        hoverState.properties.shadowBlur = 10;
        hoverState.properties.shadowOffsetX = 0;
        hoverState.properties.shadowOffsetY = 5;
        hoverState.properties.shadowColor = am4core.color("#000000");
        hoverState.properties.shadowOpacity = 0.3;
        
        // Labels
        pieSeries.labels.template.text = "{category}";
        pieSeries.labels.template.fontSize = 11;
        pieSeries.labels.template.fill = am4core.color("#000");
        
        // Ticks (lines connecting labels to slices)
        pieSeries.ticks.template.disabled = false;
        pieSeries.ticks.template.stroke = am4core.color("#999");
        
        // Tooltip with formatted value and percentage
        pieSeries.slices.template.tooltipText = "{category}\n[bold fontSize:14]₹{formatted}[/]\n[fontSize:12]{value.percent.formatNumber('#.0')}%[/]";
        pieSeries.tooltip.pointerOrientation = "vertical";
        
        // Legend
        pieChart.legend = new am4charts.Legend();
        pieChart.legend.position = "right";
        pieChart.legend.maxHeight = 300;
        pieChart.legend.scrollable = true;
        pieChart.legend.valueLabels.template.text = "₹{formatted}";
        pieChart.legend.valueLabels.template.fontSize = 11;
        
        // Make chart responsive
        pieChart.responsive.enabled = true;
        
    }); 
}

// ==================== DONUT CHART FUNCTION WITH COLORS ====================
function drawSoDonutChart(result) {
    
    am4core.ready(function() {
        
        // Dispose previous donut chart if exists
        if (donutChart) {
            donutChart.dispose();
        }
        
        // Themes
        am4core.useTheme(am4themes_animated);
        
        // Create donut chart instance
        donutChart = am4core.create("donutChartdiv", am4charts.PieChart);
        
        // REMOVE AMCHARTS LOGO
        donutChart.logo.disabled = true;
        
        // Add title
        var title = donutChart.titles.create();
        title.text = "QUARTERLY SALES BREAKDOWN - FY " + $("#soYear").val() + "-" + $("#soYear option:selected").text().split("-")[1];
        title.fontSize = 18;
        title.fontWeight = "600";
        title.marginBottom = 15;
        
        // Calculate quarterly totals
        var q1 = parseFloat(result.value.APRIL) + parseFloat(result.value.MAY) + parseFloat(result.value.JUNE);
        var q2 = parseFloat(result.value.JULY) + parseFloat(result.value.AUGUST) + parseFloat(result.value.SEPTEMBER);
        var q3 = parseFloat(result.value.OCTOBER) + parseFloat(result.value.NOVEMBER) + parseFloat(result.value.DECEMBER);
        var q4 = parseFloat(result.value.JANUARY) + parseFloat(result.value.FEBRUARY) + parseFloat(result.value.MARCH);
        
        // Prepare quarterly data WITH COLORS
        var donutData = [
            {
                "Quarter": "Q1 (Apr-Jun)",
                "Total": parseFloat(q1.toFixed()),
                "formatted": commaSeparateNumber(q1.toFixed()),
                "color": "#FF6B6B"
            },
            {
                "Quarter": "Q2 (Jul-Sep)",
                "Total": parseFloat(q2.toFixed()),
                "formatted": commaSeparateNumber(q2.toFixed()),
                "color": "#4ECDC4"
            },
            {
                "Quarter": "Q3 (Oct-Dec)",
                "Total": parseFloat(q3.toFixed()),
                "formatted": commaSeparateNumber(q3.toFixed()),
                "color": "#FFD93D"
            },
            {
                "Quarter": "Q4 (Jan-Mar)",
                "Total": parseFloat(q4.toFixed()),
                "formatted": commaSeparateNumber(q4.toFixed()),
                "color": "#6C5CE7"
            }
        ];
        
        donutChart.data = donutData;
        
        // Create donut series
        var donutSeries = donutChart.series.push(new am4charts.PieSeries());
        donutSeries.dataFields.value = "Total";
        donutSeries.dataFields.category = "Quarter";
        
        // Make it a donut by setting innerRadius
        donutSeries.slices.template.innerRadius = am4core.percent(40);
        
        // Slice styling
        donutSeries.slices.template.stroke = am4core.color("#fff");
        donutSeries.slices.template.strokeWidth = 3;
        donutSeries.slices.template.strokeOpacity = 1;
        
        // ==================== CUSTOM COLORS FOR DONUT CHART ====================
        donutSeries.slices.template.adapter.add("fill", function(fill, target) {
            if (target.dataItem && target.dataItem.dataContext && target.dataItem.dataContext.color) {
                return am4core.color(target.dataItem.dataContext.color);
            }
            return fill;
        });
        // ==================== END CUSTOM COLORS ====================
        
        // Hover state
        var hoverState = donutSeries.slices.template.states.getKey("hover");
        hoverState.properties.scale = 1.05;
        hoverState.properties.shadowBlur = 10;
        hoverState.properties.shadowOffsetX = 0;
        hoverState.properties.shadowOffsetY = 5;
        hoverState.properties.shadowColor = am4core.color("#000000");
        hoverState.properties.shadowOpacity = 0.3;
        
        // Labels
        donutSeries.labels.template.text = "{category}";
        donutSeries.labels.template.fontSize = 12;
        donutSeries.labels.template.fontWeight = "600";
        donutSeries.labels.template.fill = am4core.color("#000");
        
        // Ticks
        donutSeries.ticks.template.disabled = false;
        donutSeries.ticks.template.stroke = am4core.color("#999");
        
        // Tooltip
        donutSeries.slices.template.tooltipText = "{category}\n[bold fontSize:16]₹{formatted}[/]\n[fontSize:12]{value.percent.formatNumber('#.0')}%[/]";
        donutSeries.tooltip.pointerOrientation = "vertical";
        
        // Add center label showing total
        var label = donutChart.seriesContainer.createChild(am4core.Label);
        label.text = "Total\n₹" + commaSeparateNumber((q1 + q2 + q3 + q4).toFixed());
        label.horizontalCenter = "middle";
        label.verticalCenter = "middle";
        label.fontSize = 16;
        label.fontWeight = "bold";
        label.fill = am4core.color("#17a2b8");
        label.textAlign = "middle";
        
        // Legend
        donutChart.legend = new am4charts.Legend();
        donutChart.legend.position = "bottom";
        donutChart.legend.valueLabels.template.text = "₹{formatted}";
        donutChart.legend.valueLabels.template.fontSize = 11;
        
        // Make chart responsive
        donutChart.responsive.enabled = true;
        
    }); 
}

function commaSeparateNumber(val) {
    var x = val;
						 
    x = x.toString();
    x = x.replace(/,/g, "");
    
    var afterPoint = '';
    if (x.indexOf('.') > 0) {
        afterPoint = x.substring(x.indexOf('.'), x.length);
    }
    
    x = Math.floor(x);
    x = x.toString();
    var lastThree = x.substring(x.length - 3);
    var otherNumbers = x.substring(0, x.length - 3);
    
    if (otherNumbers != '') {
        lastThree = ',' + lastThree;
    }
    
    var res = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree + afterPoint;
    return res;


}
