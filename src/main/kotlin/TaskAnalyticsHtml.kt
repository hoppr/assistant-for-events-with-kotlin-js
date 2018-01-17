import kotlin.js.Date
import kotlin.math.round

class TaskAnalyticsHtml(val global: GlobalStats) {

    fun <T, R> Iterable<T>.mapJoinString(transform: (T) -> R) = map(transform).joinToString("\n")

    fun build() = """<!doctype HTML>
<html lang="it">
<head>
	<title>GDG Milano | Tasks</title>
	<meta charset=utf-8>
	<meta name="author" content="Omar Miatello">
    <meta name="description" content="Analisi dei task segnati su Trello del GDG Milano">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>
	<link href="//cdn.jsdelivr.net/npm/bare-css@2.0.3/css/bare.min.css" rel="stylesheet">
</head>
<body>
<nav style="background: darkgrey">
	<label>
		<input type="checkbox">
		<header>
			<a href="#"><img src="http://www.gdgmilano.it/assets/GDGMilanoW.svg" style="width: 100px"> Tasks</a>
		</header>

		<ul>
			<li><a href="#y2017">2017</a></li>
			<li><a href="#y2018">2018</a></li>
		</ul>
	</label>
</nav>

<section id="people">
    <h1>Staff</h1>

    <h3>Membri attivi</h3>
    <card>
        <grid>
    ${global.personsActive.mapJoinString {
        """
            <div col="1/3">
                <p>
                    ${it.name}<br />
                    <tag>${it.taskPoints} punti | ${it.tasks.size} task</tag>${it.badges.mapJoinString { """<tag tt="${it.hint}">${it.name}</tag>""" }}
                </p>
            </div>
        """
    }}
        </grid>
    </card>

    <h3>Hanno contribuito (oltre 4 mesi fa)</h3>
    <p>${global.personsInactive.map { """${it.name} (${it.taskPoints} punti | ${it.tasks.size} task)""" }.joinToString(" - ")}</p>
</section>

    ${global.years.reversed().mapJoinString {
        """
<section id="y${it.title}">
    <h1>${it.title}</h1>

    <card>
        <grid>
        ${it.persons.mapJoinString {
            """
            <div col="1/3">
                <p>
                    ${it.name}<br />
                    <tag>${it.taskPoints} punti | ${it.tasks.size} task</tag><br />
                    ${it.badges.mapJoinString { """<tag tt="${it.hint}">${it.name}</tag>""" }}
                </p>
            </div>
        """
        }}
        </grid>
    </card>

        ${it.weeks.reversed().mapJoinString {
            """
    <h4>${it.title} <tag>${it.persons.size} persone | ${it.persons.sumByDouble { it.taskPoints }.oneDigit()} punti | ${it.persons.sumBy { it.tasks.size }} task</tag></h4>
    <grid>
            ${it.persons.mapJoinString {
                """
        <div col="1/3">${it.name}<br /><tag>${it.taskPoints.oneDigit()} punti | ${it.tasks.size} task</tag>${it.badges.mapJoinString { """<tag tt="${it.hint}">${it.name}</tag>""" }}</div>
        <div col="2/3">
                ${it.tasks.mapJoinString {
                    """
        <p><small><a href="${it.link}" tt="${it.board} | ${Date(it.date).toLocaleString("it")}">${it.name}</a></small> <tag>${it.points.oneDigit()} punti</tag></p>
                    """
                }}
                </div>
                """
            }}
    </grid>
            """
        }}
</section>
    """
    }}
</body>
</html>
"""
}