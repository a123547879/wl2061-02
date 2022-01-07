const CardGrid=Vue.component('CardGrid',{
    props:["count","boxes"],
    template:`<h3>总计:{{boxes.length}}，选中:{{count}}</h3>`
});

export default CardGrid