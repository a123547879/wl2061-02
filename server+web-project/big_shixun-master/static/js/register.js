const register=Vue.component('Register',{
    data:function(){
        return {
            userId:'',
            password:'',
            rePassword:'', 
        }
    },
    props:['userData'],
    template:`
        <div class="box">
            <div class="line">
                <span>账号</span>
                <input type="text" class="text" v-model='userId'>
            </div>
            <div class="line">
                <span>密码</span>
            <input type="password" class="text" v-model='password'>
            </div>
            <div class="line">
                <span>确认密码</span>
            <input type="password" class="text" v-model='rePassword'>
            </div>
            <div class='buttons2'>
                <button @click='print'>确认</button>
                <button @click='back'>返回</button>
            </div>
            
        </div>
        `,
    methods:{
        print:function(){
            if(this.password!=this.rePassword){//判断两次密码是否相同
                this.password=''
                this.rePassword=''
                alert('两次密码不相同！请重新输入')
                return
            }
            if(this.userId==''||this.password==''||this.rePassword==''){//判断是否有项未输入
                alert('有项目未输入')
            }else{
                for(var i=0;i<this.userData.length;i++){//判断账号是否存在
                    if(this.userId==this.userData[i].userId){
                        alert('账号已存在，请换一个账号!')
                        this.userId=''
                        this.password=''
                        this.rePassword=''
                        return
                    }
                }
                this.$emit('registerData',this.userId,this.password)
                console.log('账号:'+this.userId)
                console.log('密码:'+this.password)
                alert('注册成功，即将跳转至登录界面')
                this.userId='',
                this.password='',
                this.rePassword=''
            }
        },
        back:function(){
            this.$emit('is',0)
        }
    }
    
})

export default register