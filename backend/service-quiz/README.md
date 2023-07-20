# smart-words / backend / service-quiz

[ BACKEND DOCS ](../README.md) |
[ FRONTEND DOCS ](../../frontend/README.md) |
[ MAIN DOCS ](../../README.md)

Service for handling word quiz type of game.

It's responsible for creating and starting a quiz based on the user input parameters like: number of questions and language. 
Upon creation it provides a specified question number with four possible answers, receives and processes responsens and sends a response with correct or incorrect answer status. 
Finally after finishing a quiz it calculates the end result.

### endpoints

* `GET /health` -> `RET: OK 200+JSON` | `ERR 500+JSON`<br>Check quiz service health status
* `GET /modes` -> `RET: OK 200+JSON`<br>Get all supported quiz modes stored in current instance of quiz service
* `GET /modes/settings` -> `RET: OK 200+JSON`<br>Get all supported modes settings (fields that can be added in modes)
* `POST /modes` -> `RET: OK 200+ID` | `ERR 500`<br>Create new quiz mode 
* `PUT /modes/{id}+JSON` -> `RET: OK 200` | `ERR 500`<br>Update selected quiz mode
* `DELETE /modes/{id}` -> `RET: OK 200` | `ERR 500`<br>Delete selected quiz mode
* `POST /quiz/{mode}/start?size=10〈=pl` -> `RET: OK 200+UUID` | `ERR 500`<br>Start a new quiz
* `GET /quiz/{uuid}/question/{no}` -> `RET: OK 200 + Round JSON` | `ERR 404`<br>Receive a specific question for a specified quiz
* `POST /quiz/{uuid}/question/{no}/{answerNo}` -> `RET: OK 200` | `ERR 404`<br>Send question answer for a specific question in a specified quiz
* `GET /quiz/{uuid}/stop` -> `RET: OK 200` | `ERR 404`<br>End quiz and get result

### technology

<img src="../../resources/logo/scala.png" alt="scala logo" width="300"/>

This service is written entirely in **Scala** language.<br>
No database framework is used. All quiz information is stored in memory.

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>