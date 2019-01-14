package ru.zhenik.kafkaapis.producer;

import ru.zhenik.kafkaapis.schema.avro.User;
import ru.zhenik.kafkaapis.schema.avro.Users;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Application {
    public static void main(String[] args) {
        ProducerListUsersExample producer = new ProducerListUsersExample();

        User user1 = User.newBuilder().setId("user-id-1").build();
        User user2 = User.newBuilder().setId("user-id-2").build();
        Users users = Users.newBuilder().setList(Arrays.asList(user1, user2)).build();

        // async ack
        producer.produceWithAsyncAck(users);

        // sync ack
        try {
            producer.produceWithSyncAck(users);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
