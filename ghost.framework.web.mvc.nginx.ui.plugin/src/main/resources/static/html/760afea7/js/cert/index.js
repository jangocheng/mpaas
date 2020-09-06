$(function(){
	form.on('switch(autoRenew)', function(data){
		  $.ajax({
				type : 'POST',
				url : ctx + '/adminPage/cert/setAutoRenew',
				data : {
					id : data.value,
					autoRenew : data.elem.checked?1:0
				},
				dataType : 'json',
				success : function(data) {
				
					if (data.success) {
						//location.reload();
					} else {
						layer.msg(data.msg);
					}
				},
				error : function() {
					layer.alert(commonStr.errorInfo);
				}
		});
	});   
	
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem : '#pemBtn',
			url : '/upload/',
			accept : 'file',
			done : function(res) {
				// 上传完毕回调
				if (res.success) {
					$("#pem").val(res.obj);
					$("#pemPath").html(res.obj);
				}

			},
			error : function() {
				// 请求异常回调
			}
		});

		upload.render({
			elem : '#keyBtn',
			url : '/upload/',
			accept : 'file',
			done : function(res) {
				// 上传完毕回调
				if (res.success) {
					$("#key").val(res.obj);
					$("#keyPath").html(res.obj);
				}
			},
			error : function() {
				// 请求异常回调
			}
		});
	});
	
	form.on('select(dnsType)', function(data) {
		checkDnsType(data.value);
	});
	
	
	form.on('select(type)', function(data) {
		checkType(data.value);
	});
})


function checkDnsType(value){
	if (value == 'ali') {
		$("#ali").show();
		$("#dp").hide();
	} 
	if (value == 'dp') {
		$("#ali").hide();
		$("#dp").show();
	} 
}

function checkType(value){
	if (value == 0) {
		$("#type0").show();
		$("#type1").hide();
	} 
	if (value == 1) {
		$("#type0").hide();
		$("#type1").show();
	} 
}

function add() {
	$("#id").val(""); 
	$("#domain").val(""); 
	$("#dnsType option:first").prop("selected", true);
	$("#aliKey").val(""); 
	$("#aliSecret").val(""); 
	$("#dpId").val(""); 
	$("#dpKey").val(""); 
	checkType(0);
	checkDnsType('ali');
	
	
	showWindow(certStr.add);
}


function edit(id) {
	$("#id").val(id);

	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/cert/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				
				var cert = data.obj;
				$("#domain").val(cert.domain); 
				
				$("#type").val(cert.type); 
				$("#dnsType").val(cert.dnsType!=null?cert.dnsType:'ali');
				$("#aliKey").val(cert.aliKey); 
				$("#aliSecret").val(cert.aliSecret); 
				$("#dpId").val(cert.dpId); 
				$("#dpKey").val(cert.dpKey);
				$("#pem").val(cert.pem);
				$("#key").val(cert.key);
				$("#pemPath").html(cert.pem);
				$("#keyPath").html(cert.key);
				
				checkType(cert.type);
				checkDnsType(cert.dnsType!=null?cert.dnsType:'ali');
				
				form.render();
				
				showWindow(certStr.edit);
				
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '700px', '450px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if ($("#domain").val() == "") {
		layer.msg(certStr.error1);
		return;
	}
	
	if($("#type").val() == 0){
		if($("#dnsType").val() == 'ali'){
			if($("#aliKey").val() == '' || $("#aliSecret").val() == ''){
				layer.msg(commonStr.IncompleteEntry);
				return;
			}
		}
		if($("#dnsType").val() == 'dp'){
			if($("#dpId").val() == '' || $("#dpKey").val() == ''){
				layer.msg(commonStr.IncompleteEntry);
				return;
			}
		}
	}
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/cert/addOver',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
		
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}


function del(id){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/cert/del',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					location.reload();
				}else{
					layer.msg(data.msg)
				}
			},
			error : function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}


function issue(id){
	
	if(confirm(certStr.confirm1)){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/cert/apply',
			data : {
				id : id,
				type : "issue"
			},
			dataType : 'json',
			success : function(data) {
				layer.closeAll();
				if (data.success) {
					layer.alert(certStr.applySuccess);
					location.reload();
				} else {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.msg
					});
				}
			},
			error : function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}


function renew(id){
	
	if(confirm(certStr.confirm2)){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/cert/apply',
			data : {
				id : id,
				type : "renew"
			},
			dataType : 'json',
			success : function(data) {
				layer.closeAll();
				if (data.success) {
					layer.alert(certStr.renewSuccess);
					location.reload();
				} else {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.msg
					});
				}
			},
			error : function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}


function selectPem(){
	rootSelect.selectOne(function(rs){
		$("#pem").val(rs);
		$("#pemPath").html(rs);
	})
}


function selectKey(){
	rootSelect.selectOne(function(rs){
		$("#key").val(rs);
		$("#keyPath").html(rs);
	})
}