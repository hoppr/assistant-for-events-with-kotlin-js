class DialogResponse<T>(val displayText: String, val speech: String, val contextOut: Array<DialogContext<T>>? = null, val data: dynamic = null)

class DialogContext<out T>(val name: String, val lifespan: Int, val parameters: T)

class PollQuestion(val text: String, val text_hints: String?, val type: String) {
    companion object {
        const val TYPE_YES_NO = "yes_no"
        const val TYPE_OPEN = "open"

        val pollTypes = arrayOf(TYPE_YES_NO, TYPE_OPEN)

        fun pollContext(question: PollQuestion, questionId: Int) = pollTypes.map {
            DialogContext(
                    "poll-question-$it",
                    if (question.type == it) 3 else 0,
                    if (question.type == it) PollParameters(questionId) else null
                    //if (type == it) jsMap { it.id = questionId } else null
            )
        }.toTypedArray()

        fun pollSuggestion(question: PollQuestion, text: String? = null) = when (question.type) {
            TYPE_YES_NO -> pollSuggestionShowKeyboard(text ?: question.text, arrayOf("No", "Si"))
            TYPE_OPEN -> if (question.text_hints != null) {
                pollSuggestionShowKeyboard(text ?: question.text, question.text_hints.split("|").toTypedArray())
            } else {
                pollSuggestionHideKeyboard(text ?: question.text)
            }
            else -> pollSuggestionHideKeyboard(text ?: question.text)
        }

        private fun pollSuggestionShowKeyboard(text: String?, hints: Array<String>) = jsMap {
            it.telegram = jsMap {
                it.text = text
                it.reply_markup = jsMap {
                    it.keyboard = arrayOf(hints)
                    it.resize_keyboard = true
                    it.one_time_keyboard = true
                }
            }
            it.google = jsMap {
                it.richResponse = jsMap {
                    it.items = arrayOf(
                            jsMap {
                                it.simpleResponse = jsMap {
                                    it.textToSpeech = text
                                    it.displayText = text
                                }
                            }

                    )
                    it.suggestions = hints.map { hint -> jsMap { it.title = hint } }
                }

                it.systemIntent = arrayOf(
                        jsMap {
                            it.intent = "actions_intent_PERMISSION"
                            it.inputValueData = jsMap {
                                it["@type"] = "type.googleapis.com/google.actions.v2.PermissionValueSpec"
                                it.optContext = "To deliver your order"
                                it.permissions = arrayOf("NAME")
                            }
                        }
                )
            }
        }


        fun pollSuggestionHideKeyboard(text: String) = jsMap {
            it.telegram = jsMap {
                it.text = text
                it.reply_markup = jsMap { it.remove_keyboard = true }
            }
        }

        fun pollSuggestionShowInlineButtons(text: String?, question: PollQuestion) = jsMap {
            it.telegram = jsMap {
                it.text = text
                it.reply_markup = jsMap {
                    it.inline_keyboard = arrayOf(
                            arrayOf(
                                    jsMap {
                                        it.text = "No"
                                        it.callback_data = "No"
                                    }
                                    ,
                                    jsMap {
                                        it.text = "Si"
                                        it.callback_data = "Si"
                                    }
                            ),
                            arrayOf(
                                    jsMap {
                                        it.text = "Url"
                                        it.url = "http://www.google.it"
                                    }
                                    ,
                                    jsMap {
                                        it.text = "Inline"
                                        it.switch_inline_query = "Si"
                                    }
                                    ,
                                    jsMap {
                                        it.text = "Inline Current chat"
                                        it.switch_inline_query_current_chat = "Si"
                                    }
                            )
                    )
                }
            }
        }

        fun pollContextClean() = pollTypes.map { DialogContext("poll-question-$it", 0, null) }.toTypedArray()
    }
}

class PollParameters(val id: Int)