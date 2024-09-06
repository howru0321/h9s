import com.google.protobuf.gradle.*

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    id("com.google.protobuf") version "0.9.4"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

val grpcKotlinVersion by extra("1.4.1")
val grpcVersion by extra("1.65.1")
val grpcProtobufVersion by extra("4.27.3")
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    runtimeOnly("io.grpc:grpc-netty-shaded:$grpcVersion")
    compileOnly ("org.apache.tomcat:annotations-api:6.0.53")
    implementation("com.google.protobuf:protobuf-kotlin:$grpcProtobufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$grpcProtobufVersion")

    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("redis.clients:jedis:5.1.4")
}

sourceSets {
    getByName("main") {
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/kotlin"
            )
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$grpcProtobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}
