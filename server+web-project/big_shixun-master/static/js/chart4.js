var myChart4 = echarts.init(document.getElementById('echart4'));
const option4 = {
    title: {
        text: '光照',
        textStyle:{
            color:'#fff',
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        }
      },
      legend: {
        data: [],
      },
      toolbox: {
        feature: {
          saveAsImage: {}
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
          
        }
      ],
      yAxis: [
        {
          type: 'value',
          color:'#fff',
          textStyle:{
            color:'#fff',
        }
        }
      ],
      series: [
        {
          name: 'Email',
          type: 'line',
          stack: 'Total',
          areaStyle: {},
          emphasis: {
            focus: 'series'
          },
          data: [120, 132, 101, 134, 90, 230, 210]
        }
      ]
};

myChart4.setOption(option4);
// setInterval(function () { // 用于定时更新数据
//     myChart4.setOption({
//           xAxis:[{
//          data:
//          }],
//         series: [{
//             data: [{
//                 value:
//             }]
//         }]
//     });
// }, 2000);