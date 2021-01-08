workspace(name = "netty_tls_example")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.0"

RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "io.netty:netty-all:4.1.56.Final",
        "info.picocli:picocli:4.6.1",
        "org.bouncycastle:bcprov-jdk15on:1.68",
        "io.netty:netty-all:4.1.56.Final",
        "io.netty:netty-tcnative-boringssl-static:2.0.35.Final",
    ],
    repositories = ["https://repo1.maven.org/maven2"],
)
