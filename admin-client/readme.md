# Admin client

## Topic name limitation
All validations can be found [here](https://github.com/apache/kafka/blob/2.1/clients/src/main/java/org/apache/kafka/common/internals/Topic.java)
* length: 249
* legal chars: [a-zA-Z0-9._-]
* not empty
* do not mix (`-` with `_`) 