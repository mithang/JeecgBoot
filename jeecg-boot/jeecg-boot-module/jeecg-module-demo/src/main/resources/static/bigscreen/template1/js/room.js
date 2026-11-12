//Lựa chọn hiện tại của bảng hoàn thành kế hoạch
var indexnum = 0;
var color=['#F35331','#2499F8','#3DF098','#33B734'];
var fontColor='#FFF';

//Định nghĩa component thanh tiến độ và thuộc tính
var y_gauge1 =null;
var y_gauge2 =null;
var y_gauge3 =null;
var y_gauge4 =null;
var m_gauge1 =null;
var option_Progress =null;

//Biểu đồ xoắn ốc tình hình đơn hàng
var orderStatus=null;
var orderStatus_option =null;

//Định nghĩa component dashboard và thuộc tính
var gauge1 =null;
var gauge2 =null;
var gauge3 =null;
var gauge4 =null;
var gauge5 =null;
var option_gauge =null;

//Thành phần và thuộc tính biểu đồ tròn sản phẩm
var productPie=null;
var productPie_option=null;

//Thành phần và thuộc tính biểu đồ tiến triển kinh doanh
var businessProgress=null;
var businessProgress_placeHoledStyle = null;
var businessProgress_dataStyle =null;
var businessProgress_option=null;

//Định nghĩa component biểu đồ chồng chất chất lượng sản xuất và thuộc tính
var quality_chart = null;
var quality_option=null;

//Thành phần và thuộc tính đám mây từ
var wordCloud= null;
var wordCloud_option=null;

//Định nghĩa component biểu đồ đường kế hoạch sản xuất và thuộc tính
var plan_chart = null;
var plan_option=null;

//Định nghĩa phong cách biểu đồ hình tròn
var dataStyle = {
	normal: {
		label: {show:false},
		labelLine: {show:false}
	}
};
var placeHolderStyle = {
	normal : {
		color: 'rgba(0,0,0,0.1)',
		label: {show:false},
		labelLine: {show:false}
	},
	emphasis : {
		color: 'rgba(0,0,0,0)'
	}
};

//Mã đơn hàng lớn nhất
var lastOrderNumber=1;

$(document).ready(function ()
{	
	//Đối tượng cài đặt thanh tiến độ hình tròn	
	option_Progress={
		title : {
			text: 'Tiến độ hiện tại',
			subtext: '50%',
			x: 'center',
			y: 90,
			itemGap: 10,
			textStyle : {
				color : '#B7E1FF',
				fontWeight: 'normal',
				fontFamily : '微软雅黑',
				fontSize : 24
			},
			subtextStyle:{
				color: '#B7E1FF',
				fontWeight: 'bolder',
				fontSize:24,
				fontFamily : '微软雅黑'
			}
		},
		series : [{
			type : 'pie',
			center : ['50%', '50%'],
			radius : [75,90],
			x: '0%',
			tooltip:{show:false},		
			data : [{
				name:'Tỷ lệ đạt được', 
				value:79,
				itemStyle:{color :'rgba(0,153,255,0.8)'},
				hoverAnimation: false, 
				label : {
					show : false,
					position : 'center',
					textStyle: {						
						fontFamily:'微软雅黑',
						fontWeight: 'bolder',
						color:'#B7E1FF',
						fontSize:24
					}
				},
				labelLine : {
					show : false
				}
			},
			{
				name:'79%',
				value:21,
				itemStyle:{color: 'rgba(0,153,255,0.1)'},
				hoverAnimation: false, 
				label : {
					show : false,
					position : 'center',
					padding:20,		
					textStyle: {
						fontFamily:'微软雅黑',
						fontSize: 24,
						color:'#B7E1FF'
					}
				},
				labelLine : {
					show : false
				}
			}]
		},
		{
			type : 'pie',
			center : ['50%', '50%'],
			radius : [95,100],
			x: '0%',
			hoverAnimation: false, 
			data : [{
				value:100,
				itemStyle:{color :'rgba(0,153,255,0.3)'},
				label : {show : false},
				labelLine : {show : false}
			}]	
		},
		{
			type : 'pie',
			center : ['50%', '50%'],
			radius : [69,70],
			x: '0%',
			hoverAnimation: false, 
			data : [{
				value:100,
				itemStyle:{color :'rgba(0,153,255,0.3)'},
				label : {show : false},
				labelLine : {show : false}
			}]	
		}]
	};	
	
	//Dashboard năm
	y_gauge1 = echarts.init(document.getElementById('y_gauge1'));
	y_gauge2 = echarts.init(document.getElementById('y_gauge2'));
	y_gauge3 = echarts.init(document.getElementById('y_gauge3'));
	y_gauge4 = echarts.init(document.getElementById('y_gauge4'));
	
	//Biểu đồ xoắn ốc tình hình hoàn thành đơn hàng
	var yearPlanData=[];
	var yearOrderData=[];
	var differenceData=[];
	var visibityData=[];
	var xAxisData=[];
	
	for(var i=0;i<12;i++)
	{
		yearPlanData.push(Math.round(Math.random()*900)+100);
		yearOrderData.push(Math.round(Math.random()*yearPlanData[i]));
		differenceData.push(yearPlanData[i]-yearOrderData[i]);
		visibityData.push(yearOrderData[i]);
		xAxisData.push((i+1).toString()+"tháng");
	}
	orderStatus= echarts.init(document.getElementById('orderStatus'));
	orderStatus_option={
		title :{show:false},
		tooltip : {
			trigger: 'axis',
			formatter: function (params){
				return params[0].name + '<br/>'
					   + params[0].seriesName + ' : ' + params[0].value + '<br/>'
					   + params[1].seriesName + ' : ' + params[1].value + '<br/>'
					   +'Tỷ lệ hoàn thành: '
					   + (params[0].value > 0 ? (params[1].value/params[0].value*100).toFixed(2)+'%' : '-') 
					   + '<br/>'
			},
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		toolbox: {show:false},
		legend:{
			top: 'top',
			textStyle: {
				color: '#B7E2FF',
				fontSize:24,
				fontFamily:'微软雅黑'
			},
			data:['Sản xuất theo kế hoạch','Đơn hàng đã nhận']
		},
		xAxis: {
			data: xAxisData,
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24
				}
			},
			axisLine:{
				lineStyle:{
					color:'#09F'	
				}
			},
			axisTick:{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		yAxis: {
			inverse: false,
			splitArea: {show: false},
			axisLine:  {show: false},
			axisTick:  {show: false},
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24,
					fontFamily:'Arial',
				}
			},
			splitLine :{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		grid: {
			left: 100
		},
		series : [{
				name:'Sản xuất theo kế hoạch',
				type:'line',
				smooth :true,
				symbol: 'circle',
				symbolSize: 10,
				showAllSymbol : true,
				color:color[1],
				data:yearPlanData
			},
			{
				name:'Đơn hàng đã nhận',
				type:'line',
				smooth :true,
				symbol: 'circle',
				symbolSize: 10,
				showAllSymbol : true,
				color:'#F90',
				itemStyle:{					
					normal:{
					  lineStyle: {
						width:2
					  }
					}
				},
				data:yearOrderData
			},
			{
				name:'Không hiển thị',
				type:'bar',
				stack: '1',
				barWidth: 1,
				itemStyle:{
					normal:{
						color:'rgba(0,0,0,0)'
					},
					emphasis:{
						color:'rgba(0,0,0,0)'
					}
				},
				data:visibityData
			},
			{
				name:'Thay đổi',
				type:'bar',
				stack: '1',
				barWidth: 1,
				color:'#B7E1FF',
				data:differenceData
			}
		]
	}
	orderStatus.setOption(orderStatus_option);
	
	//Biểu đồ vành khăn doanh số sản phẩm
	var productLegend=[['Xe con 1','Xe con 2','Xe con 3','Xe con 4','Xe con 5','Xe con 6','Xe con 7','Xe con 8','Xe con 9'],['SUV1','SUV2','SUV3','SUV4'],['Xe tải 1','Xe tải 2','Xe tải 3','Xe tải 4','Xe tải 5','Xe tải 6']];
	var productClassLegend=['Xe con','SUV','Xe tải'];
	var productClassColor=['rgba(255,153,0,','rgba(153,204,102,','rgba(0,102,255,'];
	var productClassData=[];
	var productData=[];
	var productColor=[];
	for(var i=0;i<productClassLegend.length;i++)
	{	
		var total=0;
		for(var j=0;j<productLegend[i].length;j++)
		{
			var n=Math.round(Math.random()*100)+1;
			productData.push({name:productLegend[i][j],value:n});
			total+=n;
		}
		for(var j=0;j<productLegend[i].length;j++)
		{		
			productColor.push(productClassColor[i]+(1.0-productData[j].value/total).toFixed(2)+")");
		}
		productClassData.push({name:productClassLegend[i],value:total});
	}
	
	productPie=echarts.init(document.getElementById('productPie'));
	productPie_option={
		title : {
			text: 'Doanh số bán hàng',			
			x: 'center',
			y: 'center',
			itemGap: 10,
			textStyle : {
				color : '#09F',
				fontWeight: 'normal',
				fontFamily : '微软雅黑',
				fontSize : 32
			}
		},
		calculable : false,
		tooltip : {
			trigger: 'item',
			textStyle: {
				color:'#FFF',
				fontSize:24
			},
			formatter: "{a} <br/>{b} : {c} ({d}%)"
		},
		series : [
			{
				name:'Danh mục',
				type:'pie',
				selectedMode: 'single',
				radius : ['20%','40%'],				
				width: '40%',
				funnelAlign: 'right',
				itemStyle : {
					normal : {
						color:function(d)
						{
							return productClassColor[d.dataIndex]+'1)';
						},
						borderColor:'#032749',
						label : {
							position : 'inner',							
							fontSize:28,
						},
						labelLine : {
							show : false
						}
					}
				},
				data:productClassData
			},
			{
				name:'Mẫu xe',
				type:'pie',
				radius : ['40%','70%'],				
				width: '35%',
				funnelAlign: 'left',
				itemStyle : {					
					normal : {
						color:function(d)
						{
							return productColor[d.dataIndex];
						},
						borderColor:'#032749',
						label : {
							color:'#B7E1FF',						
							fontSize:24
						}
					}
				},			
				data:productData
			}
		]
	};
	productPie.setOption(productPie_option);
	
	//Biểu đồ tiến triển nghiệp vụ
	businessProgress=echarts.init(document.getElementById('businessProgress'));	
	businessProgress_placeHoledStyle = {
		normal:{
			barBorderColor:'rgba(0,0,0,0)',
			color:'rgba(0,0,0,0)'
		},
		emphasis:{
			barBorderColor:'rgba(0,0,0,0)',
			color:'rgba(0,0,0,0)'
		}
	};
	businessProgress_dataStyle = { 
		normal: {
			barBorderColor:'rgba(0,102,255,1)',
			color:function(d){
				return 'rgba(0,102,255,0.3)';
			},
			label : {
				show: true,
				position: 'insideLeft',
				formatter: '{c}%',
				textStyle: {						
					fontFamily:'Arial',
					fontWeight: 'bolder',
					color:'#B7E1FF',
					fontSize:24
				}
			}
		}
	};
	
	businessProgress_option = {
		title: {show:false},
		tooltip : {
			trigger: 'axis',
			axisPointer : {
				type : 'shadow'
			},
			textStyle: {
				color:'#FFF',
				fontSize:24
			},
			formatter : '{b}<br/>{a0}:{c0}%<br/>{a2}:{c2}%<br/>{a4}:{c4}%<br/>{a6}:{c6}%'
		},
		legend: {			
			itemGap : 60,
			top:'top',
			textStyle: {						
				fontFamily:'微软雅黑',
				fontWeight: 'bolder',
				color:'#B7E1FF',
				fontSize:24
			},
			data:['Đấu thầu dự án', 'Tiến độ đấu thầu','Dự án đang thực hiện', 'Bàn giao dự án']
		},
		toolbox: {show : false},
		grid: {
			left: 150
		},
		xAxis : [
			{
				type : 'value',
				position: 'top',
				axisLine:{
					lineStyle:{color:'#09F'}
				},
				splitLine :{
					lineStyle:{color:'#09F'	}
				},
				axisLabel: {show: false},
			}
		],
		yAxis : [
			{
				type : 'category',
				data : ['Nghiệp vụ trọng điểm 1', 'Nghiệp vụ trọng điểm 2', 'Nghiệp vụ trọng điểm 3', 'Nghiệp vụ trọng điểm 4'],
				axisLabel: {
					textStyle: {
						color: '#B7E1FF',
						fontSize:24
					}
				},
				axisLine:{
					lineStyle:{
						color:'#09F'	
					}
				},
				splitLine :{
					lineStyle:{color:'#09F'	}
				}
			}
		],
		series : [
			{
				name:'Đấu thầu dự án',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle : businessProgress_dataStyle,
				data:[100, 100, 100, 70]
			},
			{
				name:'Đấu thầu dự án',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle: businessProgress_placeHoledStyle,
				data:[0, 0, 0, 30]
			},
			{
				name:'Tiến độ đấu thầu',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle : businessProgress_dataStyle,
				data:[100, 100, 42, 0]
			},
			{
				name:'Tiến độ đấu thầu',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle: businessProgress_placeHoledStyle,
				data:[0, 0, 58, 100]
			},
			{
				name:'Dự án đang thực hiện',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle : businessProgress_dataStyle,
				data:[100, 100, 0, 0]
			},
			{
				name:'Dự án đang thực hiện',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle: businessProgress_placeHoledStyle,
				data:[0, 0, 100, 100]
			},
			{
				name:'Bàn giao dự án',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle : businessProgress_dataStyle,
				data:[71, 50, 0, 0]
			},
			{
				name:'Bàn giao dự án',
				type:'bar',
				stack: 'Tiến độ',
				itemStyle: businessProgress_placeHoledStyle,
				data:[29, 50, 100, 100]
			}
		]
	};
	businessProgress.setOption(businessProgress_option);
	
	
	//Dashboard giám sát
	/*option_gauge = {		
		title: {
			text: '', //Nội dung văn bản tiêu đề
		},
		toolbox: { //Hộp công cụ trực quan hóa
			show: false,                
		},
		tooltip: { //Component popup
			formatter: "{a} <br/>{b} : {c}%"
		},			
		series: [{          
			type: 'gauge',
			axisLine: {// Đường trục tọa độ
				lineStyle: { // Thuộc tính lineStyle điều khiển kiểu đường kẻ
					color:[[0.2, color[0]],[0.8, color[1]],[1, color[0]]],
					width: 18
				 }
			},				 
			splitLine: { // Đường phân cách
					show:true,
					length: 18,
					lineStyle: {                            
						color: '#28292D',
						width: 1
					}
				},
			axisTick : { //Kiểu đường vạch chia (và kiểu đường ngắn)
				show:false,
				lineStyle: {                    
						color: 'auto',
						width: 1
					},
				length : 20
			},
			axisLabel : {
				color:'#FFF',
				fontSize:14,
				fontFamily:'Verdana, Geneva, sans-serif'
			},
			title: {					
					textStyle: { // Các thuộc tính còn lại mặc định dùng kiểu văn bản toàn cục, xem chi tiết TEXTSTYLE
						fontWeight: 'bolder',
						fontSize: 20,                          
						color: '#FFF'
					},
					offsetCenter: [0, '30%']
				},
			pointer: {
					width: 5,                     
					color: '#F00',
					shadowColor: '#FF0',
					shadowBlur: 10
				},
			detail: {
				show:false,
				formatter:'{value}%',
				textStyle: 
				{
					fontFamily:'Arial',
					color: '#000',
					fontSize:'32px'						
				},
				offsetCenter: [0, '90%']
			},
			data: [{value: 45, name: 'Nước'}]
		}]
     };
		
	gauge1 = echarts.init(document.getElementById('gauge1'));
	gauge2 = echarts.init(document.getElementById('gauge2'));
	gauge3 = echarts.init(document.getElementById('gauge3'));
	gauge4 = echarts.init(document.getElementById('gauge4'));	
	gauge5 = echarts.init(document.getElementById('gauge5'));
	option_gauge.series[0].axisLine.lineStyle.color=[[0.2, color[0]],[0.8, color[1]],[1, color[2]]];
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Nước";
	$('#vg1').html(option_gauge.series[0].data[0].value);
	gauge1.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Điện";
	$('#vg2').html(option_gauge.series[0].data[0].value);
	gauge2.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Khí tự nhiên";
	$('#vg3').html(option_gauge.series[0].data[0].value);
	gauge3.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Khí nén";
	$('#vg4').html(option_gauge.series[0].data[0].value);
	gauge4.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Hơi nước";
	$('#vg5').html(option_gauge.series[0].data[0].value);
	gauge5.setOption(option_gauge);*/
	
	//Biểu đồ chồng chất chất lượng sản xuất
	quality_chart = echarts.init(document.getElementById('quality'));
	quality_option={
		title: {			
			show:false,
			text: 'AUDIT',
			left: 'center',
			textStyle: {
				color: '#F00',
				fontSize:32
			}
		},
		xAxis: {
			data: ['1 tháng','2 tháng','3 tháng','4 tháng','5 tháng','6 tháng','7 tháng','8 tháng','9 tháng','10 tháng','11 tháng','12 tháng'],
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24
				}
			},
			axisLine:{
				lineStyle:{
					color:'#09F'	
				}
			},
			axisTick:{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		yAxis: {
			inverse: false,
			splitArea: {show: false},
			axisLine:  {show: false},
			axisTick:  {show: false},
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24,
					fontFamily:'Arial',
				}
			},
			splitLine :{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		grid: {
			left: 100
		},
		tooltip: {
			trigger: 'item',
			textStyle: {
				color: '#B7E1FF',
				fontSize:24
			}
		},
		legend:{
			show:false,
			top: 'bottom',
			textStyle: {
				color: '#F00',
				fontSize:24,
				fontFamily:'微软雅黑'
			},
			data:['AUDIT Điểm số 1','AUDIT Điểm số']
		},
		series: [
			{
				name: 'AUDIT Điểm số 1',
				type: 'bar',
				stack: 'one',
				itemStyle: 
				{
					normal: {color: color[1]}
				},
				barWidth : 60,
				data:[2200,2900,3680,2200,2900,3680,2200,2900,3680,2200,2900,3680]
			},
			{
				name: 'AUDIT Điểm số',
				type: 'bar',
				stack: 'one',
				itemStyle: {
					normal: {
						color: '#F90',
						label: {
							 show: true,
							 position: 'insideTop',
							 textStyle: {
								 color: '#000',
								 fontSize:24
							 }
						 }
					}
				},
				barWidth : 50,
				data: [1800,1100,320,1800,1100,320,1800,1100,320,1800,1100,320]
			}
		]
	};
	quality_chart.setOption(quality_option);
	
	//Biểu đồ đường kế hoạch sản xuất
	var plan_data1=[];
	var plan_data2=[];
	var plan_xAxis=[];
	for (var i = 1; i <= 12; i++) {
		plan_xAxis.push(i+"tháng");
		plan_data1.push(Math.round(Math.random() * 100));
		plan_data2.push(Math.round(Math.random() * 100));
	}
	plan_chart = echarts.init(document.getElementById('plan'));
	plan_option={		
		xAxis: {
			data:plan_xAxis,
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24
				}
			},
			axisLine:{
				lineStyle:{
					color:'#09F'	
				}
			},
			axisTick:{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		yAxis: {			
			inverse: false,
			splitArea: {show: false},
			axisLine:  {show: false},
			axisTick:  {show: false},
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24,
					fontFamily:'Arial',
				}
			},
			splitLine :{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		tooltip: {
			trigger: 'axis',
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		grid: {
			left: 100
		},
		legend:{
			show:false,
			top: 'bottom',
			textStyle: {
				color: '#F00',
				fontSize:24
			},			
			data:['Số lượng hoàn thành theo kế hoạch','Số lượng hoàn thành thực tế']
		},
		series: [
			{
				name: 'Số lượng hoàn thành theo kế hoạch',
				type: 'bar',
				itemStyle: 
				{
					normal: {color: color[1]},
					emphasis: {color: color[2]}
				},
				barWidth : 40,
				data:plan_data1
			},
			{
				name: 'Số lượng hoàn thành thực tế',
				type: 'line',
				itemStyle: {
					normal: {
						color: '#F90',
						label: {
							 show: true,
							 position: 'top',
							 textStyle: {
								 color: '#CCC',
								 fontSize:24
							 }
						},
						lineStyle:{
							color:'#F90',
							width:4
						}				 
					},
					emphasis: {
						color: '#FF0'
					}	
				},			
				symbolSize: 24,
				data: plan_data2
			}
		]
	};
	plan_chart.setOption(plan_option);
	
	//Hiển thị luân phiên tips
	/*function clock(){
	  showToolTip_highlight(plan_chart);	  
	}
	setInterval(clock, 5000);*/
	
	//Đám mây từ
	var cloudData=[];
	for(var i=0;i<30;i++)
	{
		cloudData.push({name:'Ký tự đám mây từ'+i.toString(),value:Math.random()*1000});
	}
	wordCloud=echarts.init(document.getElementById('wordCloud'));
	wordCloud_option={
		left: 'center',
        top: 'center',        
		tooltip: {
			textStyle: {
				color: '#FFF',
				fontSize:24
			}},
		series : [{  
            type : 'wordCloud',  
            shape:'smooth',  
			drawOutOfBound: true,
            gridSize : 10,  
            sizeRange : [ 10, 48 ],
			rotationRange: [0, 0],
            textStyle : {  
                normal : {  
                    color :function (d) {
						// Random color
						return 'rgba(0,153,255,'+(d.value/1000)+ ')';
					}
                },  
                emphasis : {  
                    shadowBlur : 10,  
                    shadowColor : '#333'  
                }  
            },  
            data : cloudData
        }]
	};
	
	wordCloud.setOption(wordCloud_option);
	
	//Bắt đầu bản đồ
	var map_chart = echarts.init(document.getElementById('map'));
	/*map_option = {			
		title : {show:false},
		tooltip: {
			show:function(d)
			{
				return (d.value!=null && d.value>=0);
			},
			trigger: 'item',
			formatter:function(d){
				return (d.value>=0)?d.name+'</br>Cường độ chiến lược：'+(d.value).toFixed(2):'';
			},
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		legend: {
			show:false			
		},
		dataRange: {  
			show:false,
			min: 0,  
			max: 100,  
			text:['High','Low'],  
			realtime: false,  
			calculable : false,  
			color: ['rgba(0,51,204,0.8)','rgba(0,102,255,0.8)','rgba(0,153,255,0.8)'],
			splitList: [
				{start: 0,end: 30},
				{start: 31, end: 70},
				{start: 71, end: 100},
			]
		},		
		series: [{
			name: 'Bố cục',  
			type: 'map',  
			mapType: 'china',  
			roam: false, 
			showLegendSymbol : false,
			label: {
				show: true,
				textStyle: {
					 color: '#FFF',
					 fontSize:18
				 }
			},
			itemStyle :{
				areaColor :'rgba(0,0,0,0.2)',
				borderColor : '#09F'
			},
			emphasis:{				
				areaColor :'rgba(255,0,0,0.8)',
				borderColor : 'rgba(255,0,0,0.8)'
			},
			data:[
				{name: 'Bắc Kinh', value:Math.random()*100},
				{name: 'Thiên Tân', value:Math.random()*100},
				{name: 'Thượng Hải', value:Math.random()*100},
				{name: 'Trùng Khánh', value:Math.random()*100},
				{name: 'Hà Bắc', value:Math.random()*100},
				{name: 'Hà Nam', value:Math.random()*100},				
				{name: 'Liêu Ninh', value:Math.random()*100},
				{name: 'Hắc Long Giang', value:Math.random()*100},
				{name: 'Hồ Nam', value:Math.random()*100},
				{name: 'An Huy', value:Math.random()*100},
				{name: 'Sơn Đông', value:Math.random()*100},				
				{name: 'Giang Tô', value:Math.random()*100},
				{name: 'Chiết Giang', value:Math.random()*100},
				{name: 'Giang Tây', value:Math.random()*100},
				{name: 'Hồ Bắc', value:Math.random()*100},
				{name: 'Quảng Tây', value:Math.random()*100},
				{name: 'Cam Túc', value:Math.random()*100},
				{name: 'Sơn Tây', value:Math.random()*100},
				{name: 'Nội Mông', value:Math.random()*100},
				{name: 'Thiểm Tây', value:Math.random()*100},
				{name: 'Cát Lâm', value:Math.random()*100},
				{name: 'Phúc Kiến', value:Math.random()*100},
				{name: 'Quý Châu', value:Math.random()*100},
				{name: 'Quảng Đông', value:Math.random()*100},
				{name: 'Thanh Hải', value:Math.random()*100},				
				{name: 'Tứ Xuyên', value:Math.random()*100},
				{name: 'Ninh Hạ', value:Math.random()*100},
				{name: 'Hải Nam', value:Math.random()*100}
			]	
		}]
	};*/
	var mapData=[];
	for(key in geoCoordMap)
	{
		var geoCoord = geoCoordMap[key];
		mapData.push({name:key,value:geoCoord.concat((Math.random()*1000).toFixed(2))});
	}	

	map_option = {
		title : {show:false},
		tooltip : {
			trigger: 'item',
			formatter: function(params) {
                if (typeof(params.value)[2] == "undefined") {
                    return params.name + ' : ' + params.value;
                } else {
                    return params.name + ' : ' + params.value[2];
                }
            },
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		legend: {
			show:false			
		},
		geo: {
			map: 'china',
			label: {
				normal: {show: false},
				emphasis: {show: false}
			},
			roam: false,
			itemStyle: {
				normal: {
					areaColor: 'rgba(0,153,255,0.6)',
					borderColor: '#09F'
				},
				emphasis: {
					areaColor: 'rgba(0,153,255,0.6)',
					borderColor: '#09F'
				}
			}
		},
		series: [{
			name: 'Điểm bố trí chiến lược',
			type: 'scatter',
			coordinateSystem: 'geo',
			zlevel: 1,
			rippleEffect: {
				brushType: 'stroke'
			},			
			symbolSize: function (val) {
				return val[2] / 30;
			},
			label: {				
				normal: {show: false},
				emphasis: {show: false}
			},
			itemStyle: {
				normal: {color: 'rgba(255,255,0,0.8)'},
				emphasis: {color: 'rgba(246,33,87,1)'}
			},
			data: mapData
		},	
		{
			name: 'Bố trí chiến lược TOP5',
			type: 'effectScatter',
			coordinateSystem: 'geo',
			zlevel: 2,
			hoverAnimation: true,
			showEffectOn: 'render',
			rippleEffect: {
				brushType: 'stroke'
			},
			label: {				
				normal: {
					show: true,
					fontFamily:'微软雅黑',
					fontSize: 24,
					color:'#FFF',
					formatter: '{b}',
					position: 'right',
					shadowBlur: 5,
                    shadowColor: '#000'					
				},
				emphasis: {
					show: true
				}
			},			
			symbolSize: function (val) {
				return val[2] / 30;
			},
			itemStyle: {
				normal: {
					color: 'rgba(255,255,255,1)'
				}
			},
			data: mapData.sort(function(a, b) {
                    return b.value[2] - a.value[2];
                }).slice(0, 5)
		},	
		{
			name: 'Điểm',
			type: 'scatter',
			coordinateSystem: 'geo',
			symbol: 'pin',
			symbolSize: function(val) {                    
				return val[2]/10;
			},
			label: {				
				normal: {
					show: true,
					fontFamily:'Arial, Helvetica, sans-serif',
					formatter:'{@[2]}',					
					textStyle: {
						color: '#FFF',
						fontSize: 18,
					}
				}
			},
			itemStyle: {
				normal: {
					color: 'rgba(246,33,87,1)', //Màu dấu hiệu
				}
			},
			zlevel: 3,                
			data: mapData.sort(function(a, b) {
                    return b.value[2] - a.value[2];
                }).slice(0, 5)
		}]
	};
	
	map_chart.setOption(map_option, true);
	
	resresh();
	
	//Bắt đầu làm mới định kỳ
	setInterval(resresh, 5*1000);
});

var convertData = function (data) {
    var res = [];
    for (var i = 0; i < data.length; i++) {
        var dataItem = data[i];
        var fromCoord = geoCoordMap[dataItem[0].name];
        var toCoord = geoCoordMap[dataItem[1].name];
        if (fromCoord && toCoord) {
            res.push({
                fromName: dataItem[0].name,
                toName: dataItem[1].name,
                coords: [fromCoord, toCoord]
            });
        }
    }
    return res;
};

function showToolTip_highlight(mychart)
{  
  var echartObj = mychart;
  	  
  // Làm nổi bật hình hiện tại
  var highlight =setInterval(function() 
  {
	  echartObj.dispatchAction({
		  type: 'highlight',
		  seriesIndex: 0,
		  dataIndex: indexnum
	  });
	  
	  echartObj.dispatchAction({
		  type: 'showTip',
		  seriesIndex: 0,
		  dataIndex: indexnum
	  });
	  clearInterval(highlight);
	  indexnum = indexnum + 1;
  	  if(indexnum>=7) indexnum=0;	  	  
  },1000);
}

//Làm mới dữ liệu định kỳ
function resresh()
{
	var myDate = new Date();
	
	// $('#refresh').html("<img src=\"images/wait.gif\" align=\"absmiddle\"><span>Đang làm mới dữ liệu...</span>");
	$('#currentDate').html(myDate.getFullYear()+"/"+insertZero(myDate.getMonth()+1)+"/"+insertZero(myDate.getDate()));	
	
	var maxg=Math.round(Math.random()*500)+400;
	var n1=Math.round(Math.random()*(maxg-100))+100;
	var n2=Math.round(Math.random()*(n1-50))+50;	
	var n3=(n2/maxg*100).toFixed(2);	
	
	//Thanh tiến độ năm
	option_Progress.title.text ="Sản xuất theo kế hoạch";
	option_Progress.series[0].data[0].value = maxg;
	option_Progress.title.subtext =maxg+"máy";
	option_Progress.series[0].data[1].value =0;
	y_gauge1.setOption(option_Progress);
	
	option_Progress.title.text ="Đơn hàng đã nhận";
	option_Progress.series[0].data[0].value = n1;
	option_Progress.title.subtext =n1+"máy";
	option_Progress.series[0].data[1].value =(maxg-n1);
	y_gauge2.setOption(option_Progress);
	
	option_Progress.title.text ="Đã hoàn thành";
	option_Progress.series[0].data[0].value = n2;
	option_Progress.title.subtext =n2+"máy";
	option_Progress.series[0].data[1].value =(maxg-n2);
	y_gauge3.setOption(option_Progress);
	
	option_Progress.title.text ="Tỷ lệ hoàn thành theo kế hoạch";
	option_Progress.series[0].data[0].value = n3;
	option_Progress.title.subtext =n3+"%";
	option_Progress.series[0].data[1].value =(100-n3);
	y_gauge4.setOption(option_Progress);
	
	//Làm mới dashboard
	/*option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Nước";
	$('#vg1').html(option_gauge.series[0].data[0].value);
	gauge1.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Điện";
	$('#vg2').html(option_gauge.series[0].data[0].value);
	gauge2.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Khí tự nhiên";
	$('#vg3').html(option_gauge.series[0].data[0].value);
	gauge3.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Khí nén";
	$('#vg4').html(option_gauge.series[0].data[0].value);
	gauge4.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="Hơi nước";
	$('#vg5').html(option_gauge.series[0].data[0].value);
	gauge5.setOption(option_gauge);	*/
			
	//Hiển thị thời gian cập nhật cuối cùng
	$('#refresh').html("<span id=\"refreshTime\">Thời gian làm mới lần cuối:"+myDate.toLocaleDateString()+" "+myDate.toLocaleTimeString()+"</span>");
}

//Tạo mã đơn hàng
function getOrderNumber(n)
{
	var no="000000"+n.toString();
	return no.substring(no.length-6);
}

//Bổ sung ở phía trước 0
function insertZero(n)
{
	var no="000000"+n.toString();
	return no.substring(no.length-2);
}

//Mở cửa sổ modal
function openDialog(DlgName)
{		
	$('#'+DlgName).dialog('open');	
}