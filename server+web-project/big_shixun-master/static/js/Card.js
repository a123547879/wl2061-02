const Card=Vue.component('Card',{
    props:["boxes"],
    template:`
    <div class="outbox">
        <div v-for='(box,index) in boxes' v-on:click='onItemClick(box)' class='boxes'>
            <header :class='{different:box.selected}'>{{box.name}}{{index+1}}<span v-if='box.selected'>(选中)</span></header>
            <article>{{box.value}}</article>
            <footer>{{box.content}}</footer>
        </div>  
    </div> `,
    data:function(){
        return{
           count:0 
        }
    },
    methods:{
        onItemClick:function(b){
            b.selected=!b.selected
            if(b.selected){
                this.count++
            }else{
                this.count--
            }
            this.$emit('acount',this.count)
        }    
    },
    
})

export default Card