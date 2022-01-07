const login= Vue.component('login',{
    data:function(){
        return{
            userId:'',
            password:'',
            canLogin:false
        }
    },
    props:['userData'],
    template:`
    <div class="log">
        <div class="login">
            <span class='userLogin'>用户登录</span>
            <span>账号:</span>
            <input type="text" v-model="userId">
            <span>密码:</span>
            <input type="password" v-model="password">
            <div class='buttons'>
                <button @click='login'>登录</button>
                <button @click='register'>注册</button>
            </div>
        </div>
    </div>
    `,
    methods:{
        login:function(){
            console.log(this.userData)
            for(var i=0;i<this.userData.length;i++){
                if(this.userId==this.userData[i].userId&&this.password==this.userData[i].password){
                    this.canLogin=true
                    break
                }else{
                    this.canLogin=false
                }
            }
            if(this.userId==''||this.password==''){
                alert('用户名和密码都不能为空!')
                return
            }
            if(this.canLogin){
                alert('登录成功！欢迎用户'+this.userId)
                window.location.assign("../show.html")
            }else{
                alert('登录失败!用户名或密码错误')
                this.userId=''
                this.password=''
            }
        },
        register:function(){
            this.$emit('is',1)
        }
    }
})

export default login