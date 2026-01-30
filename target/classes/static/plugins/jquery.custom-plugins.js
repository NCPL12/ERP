(function($) {
		$.fileSizeConverter = function(size){
			var temp = 0, unit = "KB";
			var temp = size / 1024;
			
			if(temp >= 1024){
				var temp = temp / 1024;
				unit = "MB";
			}
			
			return temp.toFixed(2) + unit;
		};
		$.getFileExtension = function(ext){			
			return ext.split("/")[1];
		};
/************************************************* Start of URL Parameter *******************************************/
		$.getParamFromURL = function(key){
			var queryParam = window.location.search.substring(1);
			var params = queryParam.split("&");
			for (var index = 0; index < params.length; index++){
				var keyValue = params[index].split("=");
				if (keyValue[0] === key){
					return decodeURI(keyValue[1]);
				}
			}
		};
/************************************************* End of URL Parameter *********************************************/
/*********************************************** Start of Date Formatter ********************************************/
		$.format = function(date, formater){			

			if(date==undefined){
				return "" ;
			}

			var year = /yyyy|yy/g;
			if((result = year.exec(formater)) != null){
				result.forEach(function(item, index){
					if(item === undefined) return;
					if (item.length == 2){			
						formater = formater.replace(item, date.getYear().toString().substr(1, 2));
					}else if(item.length == 4){
						formater = formater.replace(item, date.getFullYear());
					}
				});
			}
			
			var month = /M{1,4}/g;
			if((result = month.exec(formater)) != null){
				var monthsName = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
				result.forEach(function(item, index){
					if(item === undefined) return;
					var monthIndex = date.getMonth();
					if (item.length == 1){			
						formater = formater.replace(item, monthIndex);
					}else if(item.length == 2){				
						formater = formater.replace(item, (monthIndex < 10)? "0" + (monthIndex+1): (monthIndex+1));
					}else if(item.length == 3){			
						formater = formater.replace(item, monthsName[monthIndex].substr(0, 3));
					}else if(item.length == 4){
						formater = formater.replace(item, monthsName[monthIndex]);
					}
				});
			}

			var day = /d{1,2}/g;
			if((result = day.exec(formater)) != null){
				result.forEach(function(item, index){
					if(item === undefined) return;
					if (item.length == 1){			
						formater = formater.replace(item, date.getDate());
					}else if(item.length == 2){			
						formater = formater.replace(item, (date.getDate() < 10)? "0" + date.getDate(): date.getDate());
					}
				});
			}

			var hour = /h{1,2}/gi
			if((result = hour.exec(formater)) != null){
				result.forEach(function(item, index){
					if(item === undefined) return;
					var hourIndex = date.getHours();
					if (item.length == 1 && item == item.toUpperCase()){
						formater = formater.replace(item, hourIndex);
					}else if(item.length == 2 && item == item.toUpperCase()){
						formater = formater.replace(item, (hourIndex < 10)? "0" + hourIndex: hourIndex);
					}else if (item.length == 1 && item == item.toLowerCase()){
						if(hourIndex > 12) hourIndex -= 12;
						formater = formater.replace(item, hourIndex);
					}else if(item.length == 2 && item == item.toLowerCase()){
						if(hourIndex > 12) hourIndex -= 12;
						formater = formater.replace(item, (hourIndex < 10)? "0" + hourIndex: hourIndex);
					}
				});
			}

			var min = /m{1,2}/g;
			if((result = min.exec(formater)) != null){
				result.forEach(function(item, index){
					if(item === undefined) return;
					if (item.length == 1){			
						formater = formater.replace(item, date.getMinutes());
					}else if(item.length == 2){			
						formater = formater.replace(item, (date.getMinutes() < 10)? "0" + date.getMinutes(): date.getMinutes());
					}
				});
			}

			var sec = /s{1,2}/g;
			if((result = sec.exec(formater)) != null){
				result.forEach(function(item, index){
					if(item === undefined) return;
					if (item.length == 1){			
						formater = formater.replace(item, date.getSeconds());
					}else if(item.length == 2){			
						formater = formater.replace(item, (date.getSeconds() < 10)? "0" + date.getSeconds(): date.getSeconds());
					}
				});
			}

			var ampm = /a$/gi
			if((result = ampm.exec(formater)) != null){
				result.forEach(function(item, index){
					if(item === undefined) return;			
					if(date.getHours() > 12){
						formater = formater.replace(ampm, "PM");
					}else{
						formater = formater.replace(ampm, "AM");
					}
				});
			}

			return formater;
		}
/*********************************************** End of Date Formatter **********************************************/
/******************************************* Start of form to JSON converter ****************************************/
		var getObject = function(keys, value, index){
			var index = index || 0;		
			var temp = {};
			if(index == keys.length - 1){
				temp[keys[index]] = value;
				return temp;
			}else{
				temp[keys[index]] = getObject(keys, value, index + 1);
				return temp;
			}
		};

		var getTypeCastedValue = function(value, type){
			switch(type){
				case 'Integer':				
				case 'Float':
				case 'Double':
				case 'Number':
				value = Number(value);
				break;
				case 'Boolean':
				value = (value == 'true') ? true : false;
				break;				
				break;
				case 'String':
				default:
				value = (value != '') ? value : null;		
			}
			return value;
		};		
		$.fn.form2json = function(){
			var _this = this;
			var json = {};
			var obj = this.serializeArray();

			var index = 0;
			while(index < obj.length){
				var type = $(_this).find('[name="' + obj[index].name + '"]').data('type');
				var array = false;
				if(type != undefined && type.indexOf('[') != -1){
					array = true;
					type = type.substr(0, type.indexOf('['));
				}
				obj[index].value = getTypeCastedValue(obj[index].value.trim(), type);

				var temp = [obj[index].value];
				for(var i = index + 1; i < obj.length; i++){
					if(obj[index].name == obj[i].name){				
						temp.push(getTypeCastedValue(obj[i].value.trim(), type));
						obj.splice(i, 1);
						i--;
					}
				}
				if(temp.length >= 2 || array){
					obj[index].value = temp;
				}
				index++;
			};

			$.each(obj, function(index, object) {			
				var keys = object.name.split('.');
				$.extend(true, json, getObject(keys, object.value));
			});
			return json;
		};
/******************************************* End of form to JSON converter ******************************************/
/*********************************************** Start of image preview *********************************************/		
		/* image preview */
		function imageFileReader(src, type, callBackFunction) {	
			var file = document.getElementById(src).files[0];
			var reader = new FileReader();
			
			reader.readAsDataURL(file);			
			reader.addEventListener("load", function() {
				callBackFunction(reader.result, file);				
			}, false);
		}
		
		$.fn.preview = function(options) {			
			var settings = $.extend({				
				target	:  'preview'
			}, options);

			var result = {};

			this.change(function(e){
				imageFileReader($(this).attr("id"), 'image', function(data, file){					
					var tempImgInfo = data.split(",");					
					var start = tempImgInfo[0].indexOf(":")+1;
					var end = tempImgInfo[0].lastIndexOf(";");

					result.name = file.name;					
					result.base64 = tempImgInfo[1];
					result.mimeType = tempImgInfo[0].substr(start, end - start);
					result.lastModified = file.lastModified;

					$(settings.target).attr("src", data);
				});
			});
			return result;
		};
		$.fn.readAnyFile = function(callback){
			var result = {};
			imageFileReader($(this).attr("id"), 'file', function(data, file){
				var tempImgInfo = data.split(",");
				result.base64 = tempImgInfo[1];
				result.fileName = file.name;
				result.mimeType = file.type;
				callback(result);
			});				
			//return this;
		}
/************************************************* End of image preview **********************************************/
/***************************************** Start of datatable configuration ******************************************/
		$.datatableSettings = function(options){
			if(options.url == undefined || options.url.split("/")[0] == "undefined" || options.url.split("?")[0] == "undefined"){
				return;
			}
			
			
			ajaxComplete = options.ajaxComplete || function(response){return response};
			options.ajax = {
					"url": getUrl(options.url),
                    "contentType": "application/json",
                    "dataType": 'json',
                    "cmd" : "refresh",
                    "type": (options.data == null || options.data == "undefined") ? "GET" : "POST",
                    "async":true,
                    "data"  : function(data){
                    	console.log(data);
                    	return JSON.stringify(options.data || null);
                    },
                    "dataSrc": function (response) {                    	
                        return ajaxComplete(response);
                    },
                    "error" : function(jqXHR, textStatus, errorThrown){
                    	checkSessionTimeout(jqXHR, textStatus, errorThrown);
                    }
			}
			
			delete options.url;
			delete options.ajaxComplete;

			drawingComplete = options.drawingComplete || function(settings){};
			options.drawCallback = function(settings) {
				$(".dataTables_length").addClass("pull-left");
				$(".dataTables_paginate").addClass("pull-right");
				drawingComplete(settings);
            	hideLoader();
            }
			delete options.drawingComplete;
			
			initComplete = options.initComplete || function(settings, json){};
			options.initComplete = function(settings, json) {
				initComplete(settings, json);
            }
			delete options.initComplete;
						
			var settings = {					
					"destroy": true,		
					"retrieve": true,
					"serverSide": false,
					"processing": true,
					"deferRender": true,
					"paging":   true,
					"ordering": false,
					"sort": true,
					"info": false,
					"autoWidth": false,					
					"scrollX" : false,
					"searching": true,
					"deferRender": true,
					"dom" : '<"top"f>t<"bottom"lp>',
					"lengthMenu": [
						[10, 20, 50, -1],
						[10, 20, 50, "All"]
					]
			}
			$.extend(true, settings, options);
			return settings;
		}
		
		$.searchDatatable = function(dataTable, searchParam) {
			dataTable.search(searchParam).draw();	
		}
		
		$.reloadDatatable = function(datatable, url){
			if(url == undefined || url.split("/")[0] == "undefined"){
				return;
			}			
			url = url || null;
			datatable.clear();
			$(".tableSection").slideUp("fast");
            $(".tableSection").slideDown("fast");
            if(url === null){
            	datatable.ajax.reload();
            }else{
            	datatable.ajax.url(getUrl(url)).load(); 
            }
		}
		
		$.datatableAddRow = function (datatable, data, index){
			datatable.row.add(data).draw();
			
		    count = datatable.data().length-1,
		    insertedRow = datatable.row(count).data();
		    
		    for (var i = count; i > index; i--) {
		        tempRow = datatable.row(i-1).data();
		        datatable.row(i).data(tempRow);
		        datatable.row(i-1).data(insertedRow);
		    }     
		    insertedRow.addClass="sub"
		    datatable.page(currentPage).draw(false);
		    
		}
/****************************************** End of datatable configuration ******************************************/
/***************************************** Start of ajaxcall configuration ******************************************/
		$.serverApiCall = function(options){
			if(options.url == undefined || options.url.split("/")[0] == "undefined" || options.url.split("?")[0] == "undefined"){
				return;
			}
			
			
			//options.xhr=options.xhr||"";
			
			options.async = options.async || "true";
			var settings = {
					type: options.method,	                
	                url: getUrl(options.url),
	                async : JSON.parse(options.async),
	                cache: false,
	                contentType: false,
	                processData: false,
			}
			options.ajaxComplete = options.ajaxComplete || function(response){};
			options.ajaxFailure = options.ajaxFailure || function(status){};
			
			if(options.xhr){
				settings["xhr"] = options.xhr;
			}

			if(options.formData == true){
				settings.data = options.data;
			}else{
				settings.contentType = 'application/json; charset=utf-8';
				settings.dataType = 'json';
				settings.data = JSON.stringify(options.data) || "";
			}
			
			$.ajax(settings)
            .done(function(response) {
            	setTimeout(function(){hideLoader();},2000);	
            	options.ajaxComplete(response);
                //hideLoader();
            	//$.hideLoading();
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
            	options.ajaxFailure(textStatus);
                checkSessionTimeout(jqXHR, textStatus, errorThrown);
            })
            .always(function(jqXHROrData, textStatus, jqXHROrErrorThrown) {});
		};
		
		$.parsePathParam = function(url, param){
			param = param || null;
			var params = url.match(/{.[\w\d.:+]+}/g);
			if(params == null) return url;
			if(!(param instanceof Object)) throw "parameter required.";
				
			$.each(params, function(index, token){
				var key = token.substr(1, token.length -2);
				if(!param.hasOwnProperty(key)) throw "parameter " + key + " not found.";
				url = url.replace(token, param[key]);
			});
			return url;
		};
/****************************************** End of ajaxcall configuration *******************************************/
/***************************************** Start of toaster configuration *******************************************/		
		var tosterSettings = {
				position: {top:40,right:10},	
				hideAfter: 7000,
				loader:false
			};

			$.success = function(text){
				var tosterOption = {
					'heading': 'Success',
					'text': text,
					'icon': 'success'
				}
				$.extend(true, tosterOption, tosterSettings);
				
				$.toast(tosterOption);
			};

			$.warning = function(text){
				var tosterOption = {
					'heading': 'Warning',
					'text': text,
					'icon': 'warning'
				}	
				$.extend(true, tosterOption, tosterSettings);

				$.toast(tosterOption);
			};

			$.error = function(text){
				var tosterOption = {
					'heading': 'Error',
					'text': text,
					'icon': 'error'
				}	
				$.extend(true, tosterOption, tosterSettings);
				
				$.toast(tosterOption);
			};
/***************************************** End of toaster configuration *******************************************/
/**************************************** Start of bootbox configuration *******************************************/
			$.alert = function(message, callback){
				bootbox.alert(message, callback);
			};
			
			$.conform = function(message, callback){				
				bootbox.confirm({ 					 
					  message: message, 
					  callback: function(flag){callback(flag);}
				});
			};
			
			$.prompt = function(options, callback){
				var setting = {
						type : "text",
						class : "",
						placeholder : "",
						value : ""
				}
				$.extend(true, setting, options);
				var prompt = bootbox.prompt({
				    title: setting.message,
				    inputType: setting.type,
				    callback: function (result) {callback(result);}
				});
				prompt.init(function(){
				    $(".bootbox-input").addClass(setting.class).attr({"placeholder": setting.placeholder,"required":true}).val(setting.value);
				}); 
			}
/**************************************** End of bootbox configuration *******************************************/
			
}(jQuery));