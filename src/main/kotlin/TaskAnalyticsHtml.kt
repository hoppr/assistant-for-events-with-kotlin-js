class TaskAnalyticsHtml(val global: GlobalStats) {

    fun <T, R> Iterable<T>.mapJoinString(transform: (T) -> R) = map(transform).joinToString("\n")

    private fun Person.toHtml() = """
<p>
    ${name}<br />
    ${tasks.size} task ($taskPoints punti)<br />
    ${badges.mapJoinString { "<tag>$it</tag>" }}
</p>
"""

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
    <grid>
    ${global.persons.mapJoinString {
        """
        <div col="1/3">
            ${it.toHtml()}
        </div>
        """
    }}
    </grid>
</section>

    ${global.years.reversed().mapJoinString {
        """
<section id="y${it.title}">
    <h1>${it.title}</h1>
    <grid>
        ${it.persons.mapJoinString {
            """
        <div col="1/3">
            ${it.toHtml()}
        </div>
        """
        }}
    </grid>
        ${it.weeks.reversed().mapJoinString {
            """
    <h4>${it.title} - ${it.persons.sumBy { it.tasks.size }} task completati da ${it.persons.size} persone</h4>
    <grid>
            ${it.persons.mapJoinString {
                """
        <div col="1/6">${it.name}<br />${it.badges.mapJoinString { "<tag>$it</tag>" }}</div>
        <div col="5/6">
                ${it.tasks.mapJoinString {
                    """
        <p><tag>${it.points} p</tag> <small><a href="${it.link}" tt="${it.board} | ${it.date}">${it.name}</a></small></p>
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