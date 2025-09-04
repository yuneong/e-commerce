//package com.loopers.testcontainers;
//
//
//import org.springframework.context.annotation.Configuration;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.utility.DockerImageName;
//
//@Configuration
//public class KafkaTestContainersConfig {
//
//    private static final GenericContainer<?> kafka =
//            new GenericContainer<>(DockerImageName.parse("apache/kafka:3.7.0"))
//                    .withExposedPorts(9092)
//                    .withEnv("KAFKA_CFG_NODE_ID", "1")
//                    .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
//                    // 브로커 리스너 (0.0.0.0 은 bind 용)
//                    .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://0.0.0.0:9092,CONTROLLER://:9093")
//                    // advertise 주소는 컨테이너 내부에서는 localhost, 외부는 Testcontainers 매핑으로 접근
//                    .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://127.0.0.1:9092")
//                    .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
//                    .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "1@127.0.0.1:9093")
//                    .withEnv("KAFKA_CFG_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
//                    // Kafka 기동 완료 로그 기준으로 대기
//                    // 포트 기반 대기 (로그 대신)
//                    .waitingFor(Wait.forListeningPort());
//
//    static {
//        kafka.start();
//    }
//
//    public KafkaTestContainersConfig() {
//        String bootstrapServers = String.format("%s:%d",
//                kafka.getHost(), kafka.getMappedPort(9092));
//        System.setProperty("BOOTSTRAP_SERVERS", bootstrapServers);
//    }
//}
