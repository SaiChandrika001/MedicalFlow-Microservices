# Kafka Integration - Interview Q&A

## Q1: What is the purpose of Kafka topics in this system?
**A:** Kafka topics serve as event channels for asynchronous communication between microservices. Instead of direct service-to-service calls, services publish events to topics (e.g., "user-registered", "appointment-booked") and subscribers consume them. This decouples services—if the notification service is down, appointment-service can still publish events without failing. When notification-service comes back online, it consumes all pending messages.

## Q2: How does Kafka ensure message ordering?
**A:** Kafka uses partition keys:
- All events for the same user (email key) go to the same partition
- Within a partition, messages are ordered sequentially
- A consumer processes one partition at a time, maintaining order
Example: Both "user-registered" and "user-updated" events from the same user arrive in order.

## Q3: What happens if the notification service crashes?
**A:** 
1. Producer services continue publishing to Kafka topics
2. Messages accumulate in broker storage (retention: 1-30 days)
3. When notification-service restarts, it reads from its last committed offset
4. All messages between the crash and restart are processed

## Q4: How does the retry mechanism work?
**A:** Spring Kafka configures retry via KafkaTemplate with:
- **Retries**: 3 attempts
- **Backoff**: Exponential (delay increases with each retry)
- **DLT**: After 3 retries, messages go to Dead Letter Topic (e.g., "user-registered.DLT")
- Failed messages are logged and moved to DLT for manual inspection

## Q5: Can we have multiple consumers for the same topic?
**A:** Yes! Spring Kafka's consumer groups allow this:
- Each consumer group reads all partition replicas
- Different group IDs = different consumers get all messages
- Same group ID = consumers share partitions (load balancing)
Example: Both email notifications and SMS notifications can consume "user-registered"

## Q6: How is the consumer offset managed?
**A:** Spring Kafka automatically manages offsets:
- `spring.kafka.consumer.enable-auto-commit=true` (default) auto-commits every ~5 seconds
- Offset stored in Kafka's `__consumer_offsets` topic
- On consumer restart, it reads from last committed offset
- Manual commit available with `@KafkaListener(groupId="..." )`

## Q7: What is JSON serialization for?
**A:** 
- Kafka stores messages as byte arrays
- JsonSerializer converts event objects (UserRegisteredEvent, etc.) to JSON strings
- JsonDeserializer converts JSON back to objects on consumer side
- Enables language interoperability: Java produces, Python/Node.js can consume

## Q8: Why partition messages by email/appointmentId instead of random key?
**A:** 
- **Idempotency**: Same user's events always go to same partition
- **Ordering**: User's events processed in sequence
- **Fault handling**: If consumer fails on "userA", "userB" events still process
- **Better load distribution**: If one user has many events, distributed across multiple partitions

## Q9: How do we ensure messages don't get duplicated?
**A:** 
- **Producer side**: `acks=all` waits for all in-sync replicas to confirm
- **Consumer side**: Spring Kafka stores offset after processing
- **Idempotent consumers**: Notification service can safely re-send emails (idempotent operation)
- Database unique constraints prevent duplicate rows from multiple processing

## Q10: What's the difference between acks and retries?
**A:** 
- **Retries**: Producer automatically retries on failure (network timeout, broker unavailable)
- **Acks**: Ensures broker confirms message persistence (acks=1: leader; acks=all: all replicas)
- Example: acks=all + retries=3 means: send → wait for all replicas → if fails → retry up to 3 times

## Q11: How does this differ from message queues like RabbitMQ?
**A:** 
| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| Message Retention | Persistent (1-30 days) | Lost after delivery (queue-specific) |
| Replay | Messages available after delivery | Message deleted after consumer ack |
| Consumer Groups | Built-in, auto load-balancing | No native concept |
| Throughput | ~1M msgs/sec | ~50K msgs/sec |
| Use Case | Event streaming, replay | Task queues, RPC |

Kafka is better for: event sourcing, real-time analytics, replay scenarios.

## Q12: What is a Dead Letter Topic (DLT)?
**A:** 
- Kafka topic that stores messages that failed after all retries
- Example: "user-registered.DLT" receives messages that failed 3 times
- Operations team monitors DLT and determines: fix code → reprocess or investigate
- Prevents infinite retry loops that could block consumer threads

## Q13: How do we handle schema evolution?
**A:**
Currently: **No schema validation** (free-form JSON)
- Add new field = backward compatible (consumers ignore unknown fields)
- Remove field = potential errors (consumers expect it)
- **Future**: Implement Confluent Schema Registry for strict versioning

## Q14: What if a consumer takes too long to process?
**A:** 
- Kafka has `max.poll.interval.ms` (default 5 min)
- If consumer doesn't poll within interval → marked dead → partition rebalanced to other consumer
- Solution: Set `spring.kafka.listener.poll-timeout=3000` (3 seconds)
- Long operations: offload to async thread pools, commit offset quickly

## Q15: How do we monitor Kafka health?
**A:**
1. **Broker metrics**: `/actuator/metrics/kafka.producer.*`, `kafka.consumer.*`
2. **Topic lag**: `kafka-consumer-groups --describe` shows lag (messages behind)
3. **Broker logs**: Check broker status, replication, partition leaders
4. **Application logs**: Monitor for `KafkaException`, failed sends, retries
5. **Grafana dashboards**: Visualize lag, throughput, error rates

## Q16: Can we use Kafka for request-response (RPC)?
**A:**
No. Kafka is **one-way publish-subscribe**, not request-response.
- For RPC: Use REST, gRPC, or implement correlation ID pattern with 2 topics
- Correlation ID pattern: 
  - Service A publishes to "requests" topic with correlationId=123
  - Service B consumes, processes, publishes result to "responses" topic with same correlationId
  - Service A has background thread listening to "responses" matching correlationId=123

## Q17: How is the event payload structured?
**A:**
```java
public class UserRegisteredEvent {
    private String email;           // partition key (unique)
    private String fullName;
    private String role;            // PATIENT, DOCTOR, ADMIN
    private Instant timestamp;      // when event occurred
}
```
- **Minimal payload**: Only essential data; consumers can look up more via service calls
- **Event ID**: Optional correlation ID for tracing across services
- **Timestamp**: Machine-readable (Instant/ISO 8601) for auditing

## Q18: What is the trade-off between throughput and latency?
**A:**
- **High throughput**: Batch messages (1000 msgs), send every 100ms → high latency
- **Low latency**: Send immediately (1 msg) → lower throughput
- **Spring Kafka defaults**: 16KB or 100ms batching
- **Configuration**: `spring.kafka.producer.batch-size` and `linger.ms`

## Q19: How would we implement an event replay from a specific date?
**A:**
1. Store events with timestamp
2. Use Kafka's `--from-beginning` to read from topic start
3. Filter consumer to skip messages before date
4. Or: Use Kafka Stream topology to re-publish past events to new topic
5. Consumer reset: `kafka-consumer-groups --reset-offsets --to-datetime '2026-05-31T12:00:00'`

## Q20: How to ensure Exactly-Once semantics?
**A:**
- **At-most-once**: default, may lose messages
- **At-least-once**: enable offset commits after processing (current, may duplicate)
- **Exactly-once**: requires:
  1. `enable.idempotence=true` (producer)
  2. `isolation.level=read_committed` (consumer)
  3. Idempotent downstream (database unique keys, or check before insert)
  4. Transactional processing: process + commit atomically

For notification emails: at-least-once is acceptable (idempotent operation, user OK with duplicate emails).
