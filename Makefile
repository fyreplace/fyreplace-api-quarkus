.PHONY: emails keygen-rsa

emails:
	for email_path in src/main/resources/templates/*/html.html.mjml; \
	do \
		npx mjml -c.minify=true $$email_path -o $${email_path%.mjml}; \
	done

keygen-rsa:
	openssl genrsa > src/main/resources/keys/jwt.rsa 2048
	openssl pkcs8 -topk8 -nocrypt -inform pem -in src/main/resources/keys/jwt.rsa -outform pem > src/main/resources/keys/jwt.rsa.pem
	openssl rsa -in src/main/resources/keys/jwt.rsa -pubout > src/main/resources/keys/jwt.rsa.pub
