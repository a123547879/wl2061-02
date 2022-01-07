import login from './login.js'
import register from './register.js'
const mainInterface=Vue.component('mainInterface',{
    components:{
        'login':login,
        'register':register
    },
    data:function(){
        return{
            userId:'',
            password:'',
            userData:[],
            curComponent:'login'
        }
    },
    methods:{
        selectData:function(userId,password){
            this.curComponent='login'
            this.index=0
            this.userData.push({
                'userId':userId,
                'password':password
            })  
        },
        setCurComponent:function(index){
            if(index==0){
                this.curComponent='login'
            }else{
                this.curComponent='register'
            }
            
        }
    },
    template:`
    <div>
        <keep-alive>
            <component v-bind:is='curComponent' @registerData='selectData' :userData='userData' @is='setCurComponent'></component>
        </keep-alive>
    </div>
    `,
})

export default mainInterface