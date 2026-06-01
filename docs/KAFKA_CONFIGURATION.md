# Kafka Configuration Templates

## Local Development (localhost:9092)

### For Producers (user-service, appointment-service, report-service)
```properties
# Broker Connection
spring.kafka.bootstrap-servers=localhost:9092

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Reliability
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.linger.ms=10
spring.kafka.producer.properties.batch.size=16384

# Timeouts
spring.kafka.producer.properties.request.timeout.ms=30000
spring.kafka.producer.properties.delivery.timeout.ms=120000

# Compression
spring.kafka.producer.compression-type=snappy
```

### For Consumers (notification-service)
```properties
# Broker Connection
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service

# Deserializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# JSON Trust
spring.kafka.consumer.properties.spring.json.trusted.packages=com.medicalflow.*

# Polling
spring.kafka.listener.poll-timeout=3000

# Offset Management
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-commit-interval.ms=5000
spring.kafka.consumer.max.poll.records=500

# Session Timeout
spring.kafka.consumer.properties.session.timeout.ms=30000
```

## Docker Compose Environment (kafka:9092)

### docker-compose.yml Additions
```yaml
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'false'
      KAFKA_LOG_RETENTION_HOURS: 168
      KAFKA_NUM_PARTITIONS: 3
      KAFKA_DEFAULT_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    networks:
      - medicalflow-network
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 10s
      retries: 5
```

### application.properties (Docker profile)
```properties
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3

spring.kafka.consumer.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=notification-service
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.listener.poll-timeout=3000
```

## Production Environment (AWS MSK or Confluent Cloud)

### AWS Managed Streaming for Kafka (MSK)
```properties
# MSK Broker Connection (3+ brokers)
spring.kafka.bootstrap-servers=broker-1.cluster.amazonaws.com:9092,broker-2.cluster.amazonaws.com:9092,broker-3.cluster.amazonaws.com:9092

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Reliability (Production High Availability)
spring.kafka.producer.acks=all
spring.kafka.producer.retries=5
spring.kafka.producer.max-in-flight-requests-per-connection=5
spring.kafka.producer.properties.linger.ms=20
spring.kafka.producer.properties.batch.size=32768

# Security (IAM Authentication)
spring.kafka.security.protocol=SASL_SSL
spring.kafka.sasl.mechanism=SCRAM-SHA-512
spring.kafka.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="${KAFKA_USERNAME}" password="${KAFKA_PASSWORD}";
spring.kafka.ssl.trust-store-location=${TRUSTSTORE_PATH}
spring.kafka.ssl.trust-store-password=${TRUSTSTORE_PASSWORD}

# Timeouts
spring.kafka.producer.properties.request.timeout.ms=60000
spring.kafka.producer.properties.delivery.timeout.ms=300000

# Compression
spring.kafka.producer.compression-type=lz4
```

### Confluent Cloud Configuration
```properties
# Confluent Cloud Broker
spring.kafka.bootstrap-servers=pkc-xxxx.region.provider.confluent.cloud:9092

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=5

# Security (API Key)
spring.kafka.security.protocol=SASL_SSL
spring.kafka.sasl.mechanism=PLAIN
spring.kafka.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="${CONFLUENT_API_KEY}" password="${CONFLUENT_API_SECRET}";

# SSL Configuration
spring.kafka.ssl.protocol=TLSv1.2

# Consumer Configuration
spring.kafka.consumer.bootstrap-servers=pkc-xxxx.region.provider.confluent.cloud:9092
spring.kafka.consumer.group-id=notification-service-prod
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.medicalflow.*
spring.kafka.consumer.max.poll.records=1000
spring.kafka.listener.poll-timeout=5000
```

## Environment-Specific Profiles

### application-kafka-local.properties
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service-local
kafka.topics.enabled=true
```

### application-kafka-docker.properties
```properties
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=notification-service-docker
kafka.topics.enabled=true
```

### application-kafka-prod.properties
```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}
spring.kafka.security.protocol=SASL_SSL
spring.kafka.sasl.mechanism=SCRAM-SHA-512
spring.kafka.sasl.jaas.config=${KAFKA_JAAS_CONFIG}
spring.kafka.consumer.group-id=notification-service-prod
kafka.topics.enabled=true
kafka.replicas=3
kafka.partitions=9
```

## Topic Configuration Parameters

### Standard Topic Settings (Development)
```json
{
  "name": "user-registered",
  "partitions": 3,
  "replication_factor": 1,
  "config": {
    "retention.ms": 86400000,
    "min.insync.replicas": 1,
    "compression.type": "snappy"
  }
}
```

### High-Availability Topic Settings (Production)
```json
{
  "name": "user-registered",
  "partitions": 9,
  "replication_factor": 3,
  "config": {
    "retention.ms": 604800000,
    "min.insync.replicas": 2,
    "compression.type": "lz4",
    "cleanup.policy": "delete",
    "unclean.leader.election.enable": false
  }
}
```

## Testing Kafka Connectivity

### Check broker status
```bash
# In Docker
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server kafka:9092

# Locally
kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Create test topic
```bash
docker-compose exec kafka kafka-topics --create \
  --bootstrap-server kafka:9092 \
  --topic test-topic \
  --partitions 3 \
  --replication-factor 1
```

### Produce test message
```bash
docker-compose exec -it kafka kafka-console-producer \
  --broker-list kafka:9092 \
  --topic test-topic
# Type message and press Enter
```

### Consume test message
```bash
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic test-topic \
  --from-beginning
```

## Performance Tuning

### High-Throughput Configuration
```properties
# Producer: Increase batching
spring.kafka.producer.properties.linger.ms=100
spring.kafka.producer.properties.batch.size=65536
spring.kafka.producer.compression-type=lz4

# Consumer: Increase fetch size
spring.kafka.consumer.max.poll.records=1000
spring.kafka.consumer.fetch-min-bytes=1048576
spring.kafka.consumer.fetch-max-wait-ms=500
```

### Low-Latency Configuration
```properties
# Producer: Minimize batching
spring.kafka.producer.properties.linger.ms=1
spring.kafka.producer.properties.batch.size=8192
spring.kafka.producer.compression-type=snappy

# Consumer: Frequent polling
spring.kafka.consumer.max.poll.records=100
spring.kafka.listener.poll-timeout=1000
```

## Monitoring Queries

### Check consumer lag
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group notification-service \
  --describe
```

### Monitor broker metrics
```bash
kafka-metrics --bootstrap-server localhost:9092 \
  --metrics "[KafkaServer]" \
  --interval 5000
```
