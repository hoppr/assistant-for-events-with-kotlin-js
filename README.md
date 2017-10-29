## Telegram bot: Assistant for events

Purpose: Build a Assistant bot for handle questions/polls/suggestions during an event.

Chat supported: [Telegram](https://telegram.org) (🚧 maybe other in future 🚧)

Developed by [Omar Miatello](https://google.com/+OmarMiatello) for [GDG Milano](http://www.gdgmilano.it).

---

#### Content of this repository
- This code is Fulfillment for [DialogFlow](https://dialogflow.com/) (ex [api.ai](https://www.api.ai))
- Use
[Cloud Functions for Firebase](https://firebase.google.com/products/functions) as "backend service"
- Use
[Kotlin](https://www.kotlinlang.org/) as language for Cloud Functions (compile in JavaScript)


### Project Setup

1. `npm install -g firebase-tools`
2. `git clone https://github.com/jacklt/Assistant-for-events.git`
3. Create a Firebase Project using the Firebase Developer Console
4. Initialize Firebase
   ```
   cd Assistant-for-events
   firebase init
   ```
5. Install the dependencies and deploy
   ```
   cd functions
   npm install
   firebase deploy
   ```
   
#### Note about Kotlin
Note: with Kotlin you need to build the JavaScript files before
deploying, so there's an npm script that does the steps.  You can see
that and a few other handy shortcuts in [package.json](functions/package.json)


## License

    Copyright 2017 Omar Miatello.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    All trademarks and registered trademarks are the property of their respective owners.