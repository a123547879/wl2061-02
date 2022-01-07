import Card from './Card.js'
import CardGrid from './CardGrid.js'
import box_data from './Data.js'
const CardPage=Vue.component('cardPage',{
    components:{
        'card':Card,
        'CardGrid':CardGrid
    },
    data:function(){
        return{
            index:0,
            count:0,
            boxes:box_data,
            curComponent:'CardGrid'
        }
    },
    template:`
    <div class='outbox'>
        <ul>
            <li v-on:click='onMenuClicked(0)' :class='index==0? "bk":""'>概要</li>
            <li v-on:click='onMenuClicked(1)' :class='index==1? "bk":""'>详情</li>
        </ul>
        <keep-alive>
            <component v-bind:is='curComponent' :count="count" :boxes="boxes" @acount='handleCount'></component>
        </keep-alive>
    </div>    
    `,
    methods:{
        handleCount:function(count){
            this.count = count
        },
        onMenuClicked:function(index){
            this.index=index
            if( index === 0 ){
                this.curComponent='CardGrid'
            }else{
                this.curComponent='card'
            }
        }
    }
})
export default CardPage