# Netty TLS example

Example using Netty TLS with different signature algorithms.

## Building

Build the Server binary:
```shell script
bazel build //src/main/java/example/netty/server:Server
```

## Usage

Run the Server binary with the sha256WithRSAEncryption algorithm:
```shell script
bazel-bin/src/main/java/example/netty/server/Server --port 50440 --certificate src/main/certs/server-rsa.pem --private-key src/main/certs/server-rsa.key
```

Run the Server binary with the ED25519 algorithm:
```shell script
bazel-bin/src/main/java/example/netty/server/Server --port 50440 --certificate src/main/certs/server-ed25519.pem --private-key src/main/certs/server-ed25519.key
```

Test the server using OpenSSL:
```shell script
openssl s_client -connect localhost:50440 -CAfile src/main/certs/ca-rsa.pem
```