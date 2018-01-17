import kotlin.math.ceil

external fun require(module: String): dynamic
external val exports: dynamic

class Task(val link: String, val name: String, val points: Int, val num_person: Int, val member: String, val date: String, val board: String)


fun main(args: Array<String>) {
    val fireFunctions = require("firebase-functions")
    val config = fireFunctions.config()
    val fireAdmin = also(require("firebase-admin")) { it.initializeApp(config.firebase) }
    val dbBot = fireAdmin.database().ref("/bot")
    val dbQuestion = dbBot.child("polls/questions/2017-10-16-serverless")
    val dbResp = dbBot.child("polls/responses/2017-10-16-serverless")
    val dbTasks = dbBot.child("tasks")

    fun pollQuestion(id: Int, result: (PollQuestion?) -> dynamic) = fireReadOnce(dbQuestion.child(id)) { result(it.unsafeCast<PollQuestion?>()) }

    // OLD: https://us-central1-gdgmilano-1eb88.cloudfunctions.net/dialogflowFirebaseFulfillment

    // Action on Google
    // val DialogflowApp = require("actions-on-google").DialogflowApp

    exports.saveString = fireFunctions.https.onRequest { req, res ->
        console.log("Request headers: " + toJson(req.headers))
        console.log("Request body: " + toJson(req.body))
        // val app = eval("""new DialogflowApp({req, res})""")

        // Simple 'authorization' with bearer

        val bearer = req.headers.authorization?.unsafeCast<String>()?.removePrefix("Bearer ")
        if (bearer != config.auth.key?.unsafeCast<String>()) {
            console.error("Unauthorized - Access denied for ${req.headers.authorization}")
            // FIXME remove before production!
            // return@onRequest res.status(403).send("Unauthorized")
        }


        // Responses generic

        fun sendPlainText(text: String): dynamic {
            console.log("Response body: $text")
            return res.status(200).send(text)
        }


        fun sendJsonObj(obj: dynamic) = sendPlainText(toJson(obj))
        fun sendDialogResponse(obj: DialogResponse<dynamic>) = sendJsonObj(obj)
        fun sendDialogResponse(text: String, context: Array<DialogContext<dynamic>>? = null, data: dynamic = null) = sendJsonObj(DialogResponse(text, text, context, data))

        val requestSource = req.body.originalRequest?.source
        if (requestSource !in listOf("telegram", "google")) return@onRequest sendDialogResponse("Servizio non supportato su $requestSource")


        // Telegram params

        val tgData = req.body.originalRequest.data
        val tgUserInfo = tgData.message?.from ?: tgData.callback_query?.from ?: tgData.user
        val userId = tgUserInfo.id ?: tgUserInfo.userId


        // DialogFlow params

        // Parameters: are any entites that Dialogflow has extracted from the request.
        // Action: is a string used to identify what needs to be done in fulfillment
        // Contexts: are objects used to track and store conversation state
        // https://dialogflow.com/docs/actions-and-parameters
        // https://dialogflow.com/docs/contexts

        val dfResult = req.body.result
        val dfAction = dfResult?.action?.unsafeCast<String>()
        val dfParameters = dfResult?.parameters
        val dfInputContexts = dfResult?.contexts?.unsafeCast<Array<DialogContext<dynamic>>>()
        fun <T> getContext(name: String) = dfInputContexts?.find { it.name == name }?.unsafeCast<DialogContext<T>>()
        // val app = DialogflowApp(request = req, response = res)
        // val text = req.query.text


        // Responses for polls

        fun getIdFromContext(contextName: String) = getContext<PollParameters>(contextName)?.parameters?.id

        fun sendQuestionOrComplete(questionId: Int) {
            pollQuestion(questionId) {
                console.log("Next question: " + toJson(it))
                if (it != null) {
                    sendDialogResponse(it.text, PollQuestion.pollContext(it, questionId), PollQuestion.pollSuggestion(it))
                } else {
                    val text = "Hai completato il questionario!\nGrazie per il tempo che ci hai dedicato!"
                    sendDialogResponse(text, PollQuestion.pollContextClean(), PollQuestion.pollSuggestionHideKeyboard(text))
                }
            }
        }

        fun pollError(reason: String) = sendDialogResponse("Impossibile continuare il sondaggio: $reason", PollQuestion.pollContextClean())


        when (dfAction) {

        // Test

            "test.number" -> sendDialogResponse("""Ciao ${tgUserInfo.first_name} ${tgUserInfo.last_name}, sò che il tuo nick è ${tgUserInfo.username} (id: ${tgUserInfo.id}) 😊""")


        // Polls

            "event.poll" -> {
                dbResp.child(userId).update(jsMap {
                    it.name = "${tgUserInfo.first_name} ${tgUserInfo.last_name} (${tgUserInfo.username})"
                })

                val questionId = 0

                pollQuestion(questionId) {
                    console.log("Next question: ${toJson(it)}")
                    if (it != null) {
                        val text = """Questo è il secondo sondaggio tramite Telegram! 😀
                            |Ecco la prima domanda:
                            |
                            |${it.text}""".trimMargin()
                        sendDialogResponse(text, PollQuestion.pollContext(it, questionId), PollQuestion.pollSuggestion(it, text))
                    } else {
                        val text = "Non ci sono sondaggi disponibili al momento!"
                        sendDialogResponse(text, PollQuestion.pollContextClean(), PollQuestion.pollSuggestionHideKeyboard(text))
                    }
                }
            }
            "event.poll.resp.open" -> {
                val questionId = getIdFromContext("poll-question-open") ?: return@onRequest pollError("id")
                dbResp.child(userId).update(jsMap { it[questionId] = dfResult.resolvedQuery })
                sendQuestionOrComplete(questionId + 1)
            }
            "event.poll.resp.yes" -> {
                val questionId = getIdFromContext("poll-question-yes_no") ?: return@onRequest pollError("id")
                dbResp.child(userId).update(jsMap { it[questionId] = true })
                sendQuestionOrComplete(questionId + 1)
            }
            "event.poll.resp.no" -> {
                val questionId = getIdFromContext("poll-question-yes_no") ?: return@onRequest pollError("id")
                dbResp.child(userId).update(jsMap { it[questionId] = false })
                sendQuestionOrComplete(questionId + 1)
            }


        // Staff only

            "estrai" -> {
                if (tgUserInfo.id == 100729672) {
                    fireReadOnce(dbResp) { snap ->
                        // console.log("snap: ${toJson(snap)}")
                        val names = ArrayList<String>()
                        keys(snap).forEach {
                            val value = snap[it]
                            if (value[4]) {
                                names.add(value["name"])
                            }
                        }

                        // ALTERNATIVE:
                        // val names = snap.unsafeCast<Array<dynamic>>().filter { it["4"] == "yes" }.map { it["name"] }

                        sendDialogResponse("Nomi: ${toJson(names.shuffle())}")
                    }

                    // rp({
                    //     method: 'GET',
                    //     uri: 'https://api.telegram.org/bot274893065:AAFIZQWIuVJVnL179yQmnDj3pXYJHp3cCkM/sendMessage?chat_id=100729672&text=Hai%20vinto!',
                    // })

                    // Invia bottone
                } else {
                    sendDialogResponse("Non sei autorizzato")
                }
            }


        // Unknown action
            else -> sendDialogResponse("Il servizio non è ancora disponibile")
        }
    }

    exports.trelloToFirebase = fireFunctions.https.onRequest { req, res ->
        console.log("Request headers: " + toJson(req.headers))
        console.log("Request body: " + toJson(req.body))

        // Responses generic

        fun sendPlainText(text: String): dynamic {
            console.log("Response body: $text")
            return res.status(200).send(text)
        }

        val users = jsMap { }
        val dbCards = dbTasks.child("cards")


        // merge tasks from boards

        val listTasks = ArrayList<Pair<Task, dynamic>>()
        keys(req.body).forEach { board ->
            req.body[board].unsafeCast<Array<dynamic>>().forEach {
                val points = it.labels.unsafeCast<Array<dynamic>>().map { it.name.unsafeCast<String>().take(2).trim().toIntOrNull() ?: 0 }.max() ?: 0
                val date = eval("new Date('${it.dateLastActivity}')")
                val members = it.members.unsafeCast<Array<dynamic>>()
                if (members.isEmpty()) listTasks.add(Task(it.shortUrl, it.name, points, 0, "unknown", it.dateLastActivity, board) to date)
                members.forEach { m ->
                    users[m.username] = m.fullName
                    listTasks.add(Task(it.shortUrl, it.name, points, members.size, m.username, it.dateLastActivity, board) to date)
                }
            }
        }

        // Sort task by year, week # of the year

        listTasks.groupBy { it.second.getFullYear().unsafeCast<Int>() }
                .map {
                    it.key to it.value.groupBy { getWeekNumber(it.second).unsafeCast<Int>() }.map {
                        it.key to jsMap { map ->
                            it.value.groupBy { it.first.member }.map {
                                map[it.key] = it.value.map { it.first }.toTypedArray()
                            }
                        }
                    }
                }
                .forEach {
                    val year = it.first
                    val dbYear = dbCards.child(year)
                    it.second.forEach {
                        dbYear.child(it.first).update(it.second)
                        Unit
                    }
                }

        dbTasks.child("users").update(users)
        sendPlainText("OK")
    }

    exports.firebaseTaskAnalyticsJson = fireFunctions.https.onRequest { req, res ->
        console.log("Request headers: " + toJson(req.headers))
        console.log("Request body: " + toJson(req.body))

        fireReadOnce(dbTasks.child("cards")) { cards ->
            fun sendPlainText(text: String): dynamic {
                console.log("Response body: $text")
                return res.status(200).send(text)
            }
            sendPlainText(toJson(cards))
        }
    }

    exports.firebaseTaskAnalytics = fireFunctions.https.onRequest { req, res ->
        console.log("Request headers: " + toJson(req.headers))
        console.log("Request body: " + toJson(req.body))


        fireReadOnce(dbTasks.child("cards")) { allCards ->
            fun sendPlainText(text: String): dynamic {
                console.log("Response body: $text")
                return res.status(200).send(text)
            }

            val years = keys(allCards).map {
                val cardsByYear = allCards[it]
                val weeks = keys(cardsByYear).map {
                    val peopleByWeek = cardsByYear[it]
                    Week("Settimana " + it, keys(peopleByWeek).map {
                        Person(it, emptyList(), peopleByWeek[it].unsafeCast<Array<Task>>().map {
                            PersonalTask(it.link, it.name, it.points / it.num_person.coerceAtLeast(1).toDouble(), it.date, it.board)
                        })
                    })
                }
                Year(it,
                        weeks.flatMap { it.persons }.groupBy { it.name }.toList().map {
                            Person(
                                    name = it.first,
                                    badges = it.second.flatMap { it.badges },
                                    tasks = it.second.flatMap { it.tasks }
                            )
                        }.sortedByDescending { it.taskPoints }
                        , weeks)
            }
            val globalStats = GlobalStats(years,
                    years.flatMap { it.persons }.groupBy { it.name }.toList().map {
                        Person(
                                name = it.first,
                                badges = it.second.flatMap { it.badges },
                                tasks = it.second.flatMap { it.tasks }
                        )
                    }.sortedByDescending { it.taskPoints }
            )

            sendPlainText(TaskAnalyticsHtml(globalStats).build())
        }
    }
}

class PersonalTask(val link: String, val name: String, val points: Double, val date: String, val board: String)
class Person(val name: String, val badges: List<String>, val tasks: List<PersonalTask>) {
    val taskPoints = ceil(tasks.sumByDouble { it.points })
}

class Week(val title: String, val persons: List<Person>)
class Year(val title: String, val persons: List<Person>, val weeks: List<Week>)
class GlobalStats(val years: List<Year>, val persons: List<Person>)