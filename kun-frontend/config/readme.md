### 如何实现本地https证书

1. 首先在homebrew下载mkcert ( mkcert 是生成本低证书的库 )

```bash
brew install mkcert
```

2. 执行 mkcert -install  (为了火狐浏览器, 还要执行一下 ``brew install nss``, 安装nss)
3. 新建一个文件夹, 进入文件夹, 执行 ``mkcert dev.localhost.com 127.0.0.1 ::1``
4. 然后修改host, 为了把127.0.0.1对应到dev.localhost.com上,  执行 ``sudo vim /private/etc/hosts``
5. 在umi中的 config中加上   (把key, cert都对应上)

``` javascript
{
    // ...
    devServer: {
        host: HTTPS ? 'dev.localhost.com' : undefined,
        port: PORT ? parseInt(PORT, 10) : 8000,
        https: HTTPS ? {
            key: '你的key文件存放地址',
            cert: '你的cert文件存放地址',
        } : undefined,
    },
}
```
