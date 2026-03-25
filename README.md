Spring S3 import examples
====================================================================================================

Examples of Spring Boot applications that import GZip-compressed CSV files stored on AWS S3
into a database and make them available via an API.


How to run
----------------------------------------------------------------------------------------------------

1. Run containers

```bash
./mvnw clean package && \
docker compose down && docker compose up --build
 ```

2. Check the log and wait until the message `Ending table updates.` is displayed.

```
... Node=8cc37cee-2658-4ca9-8a3b-9d40e2429976 Time=1774427797687 | Starting table updates.
... Node=8cc37cee-2658-4ca9-8a3b-9d40e2429976 Time=1774427797687 | Took 25792-millis to update users.
... Node=8cc37cee-2658-4ca9-8a3b-9d40e2429976 Time=1774427797687 | Ending table updates.
```

3. Access to API

http://localhost/users?username=taro


Actuator endpoints
----------------------------------------------------------------------------------------------------

- http://localhost:8090/actuator
- http://localhost:8090/actuator/health
- http://localhost:8090/actuator/metrics/jvm.memory.used
