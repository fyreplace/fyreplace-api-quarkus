.PHONY: emails keygen-rsa

emails: src/main/resources/templates/*/html.html

src/main/resources/templates/%/html.html: src/main/resources/templates/%/html.html.mjml src/main/resources/templates/emails/*.mjml
	npx mjml -c.minify=true $< -o $@

keygen-rsa:
	openssl genrsa > src/main/resources/keys/jwt.rsa 2048
	openssl pkcs8 -topk8 -nocrypt -inform pem -in src/main/resources/keys/jwt.rsa -outform pem > src/main/resources/keys/jwt.rsa.pem
	openssl rsa -in src/main/resources/keys/jwt.rsa -pubout > src/main/resources/keys/jwt.rsa.pub
