echo | openssl s_client -connect localhost:8443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > localhost_cert.pem

