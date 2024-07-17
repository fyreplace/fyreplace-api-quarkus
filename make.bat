@ECHO off
SETLOCAL EnableDelayedExpansion

GOTO %1

:emails
    FOR /R src\main\resources\templates %%G IN (*html.html.mjml) DO (
        npx mjml -c.minify=true "%%G" -o "%%~dpnG"
    )
    GOTO :eof

:keygen-rsa
    openssl genrsa > src\main\resources\keys\jwt.rsa 2048
    openssl pkcs8 -topk8 -nocrypt -inform PEM -in src\main\resources\keys\jwt.rsa -outform PEM > src\main\resources\keys\jwt.rsa.pem
    openssl rsa -in src\main\resources\keys\jwt.rsa -pubout > src\main\resources\keys\jwt.rsa.pub
    GOTO :eof
