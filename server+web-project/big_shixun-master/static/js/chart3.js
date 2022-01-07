var myChart3 = echarts.init(document.getElementById('echart3'));
const option3 = {
    series: [
        {
            type: 'gauge',
            min: 0,
            max: 300,
            progress: {
                show: true,
                width: 28
            },
            axisLine: {
                lineStyle: {
                    width: 28
                }
            },
            splitLine: {
                length: 25,
                distance: 0,
                lineStyle: {
                    width: 2,
                    color: '#999'
                }
            },
            axisLabel: {
                distance: 20,
                color: '#999',
                fontSize: 15,
                distance:-50
            },
            anchor: {
                show: true,
                showAbove: false,
                size: 10,
                itemStyle: {
                    borderWidth: 10
                }
            },
            title: {
                show: true,
            },
            detail: {
                valueAnimation: false,
                fontSize: 20,
                offsetCenter: [0, '70%'],
                formatter: '{value}dBm'
            },
            data: [
                {
                    value: 120
                }
            ]
        }
    ]
};

myChart3.setOption(option3);
// setInterval(function () { // 用于定时更新数据
//     myChart.setOption({
//         series: [{
//             data: [{
//                 value:
//             }]
//         }]
//     });
// }, 2000);