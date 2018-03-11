# signature_generate

腾讯云通信   腾讯云储存生成signature方案   --java实现的封装
=========
* im文件夹为云通信的signature生成  生成代码为https://github.com/xdhuangsidi/signature_generate/blob/master/im/java/com/tls/tls_sigature/tls_sigature.java
按照注释修改相关的值，然后调用 getSig(String uid)即可生成  uid为用户的uid，用户不存在时腾讯云通信的服务器会自动创建。获得signature后，客户端凭该signature调用云通信的api
* object_storage为对象储存signature生成  生成代码https://github.com/xdhuangsidi/signature_generate/blob/master/object_storage/com/qcloud/cos/sign/Sign.java
根据注释修改相关的值，然后调用 getSign()即可生成字符串形式的signature，凭该signature上传或删除文件，具体参考腾讯云的文档
