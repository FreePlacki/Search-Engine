import java.io.File


fun buildIndexMap(data: MutableList<String>): MutableMap<String, MutableList<Int>> {
    val map = mutableMapOf<String, MutableList<Int>>()
    for (line in 0..data.lastIndex) {
        for (w in data[line].split(" ")) {
            val word = w.lowercase()
            if (!map.containsKey(word)) map[word] = mutableListOf(line)
            else map[word]?.add(line)
        }
    }
    return map
}

object SearchStrategies {
    // without index
    fun searchPeople(query: String, people: MutableList<String>): MutableList<String> {
        val found = mutableListOf<String>()
        for (person in people) {
            if (query.lowercase() in person.lowercase())
                found.add(person)
        }
        return found
    }

    // a chunk of code used by any, all and none functions
    private fun printFound(indexSet: Set<Int>, people: MutableList<String>) {
        if (indexSet.isNotEmpty()) {
            println("Found ${indexSet.size} " + if (indexSet.size == 1) "person:" else "people:")
            for (i in indexSet)
                println(people[i])
        } else
            println("No matching people found.")
    }

    fun any(query: String, indexMap: MutableMap<String, MutableList<Int>>, people: MutableList<String>, printOutput: Boolean = true): Set<Int> {
        val foundIndexes = mutableListOf<Int>()
        for (word in query.split(" ")) {
            if (indexMap[word] == null) continue
            foundIndexes += indexMap[word]!!
        }
        val indexes = foundIndexes.toSet()
        if (printOutput) printFound(indexes, people)

        return indexes
    }

    fun all(query: String, indexMap: MutableMap<String, MutableList<Int>>, people: MutableList<String>): Set<Int> {
        // first word in query
        val foundLines = indexMap[query.split(" ")[0]]
        if (foundLines == null) {
            println("No matching people found.")
            return emptySet()
        }
        // check if all other words have the same index
        for (word in query.split(" ").drop(1)) {
            for (i in foundLines) {
                if (indexMap[word] == null || i !in indexMap[word]!!) {
                    println("No matching people found.")
                    return emptySet()
                }
            }
        }
        val indexes = foundLines.toSet()
        printFound(indexes, people)

        return indexes
    }

    fun none(query: String, indexMap: MutableMap<String, MutableList<Int>>, people: MutableList<String>): Set<Int> {
        val linesWithQuery = any(query, indexMap, people, printOutput=false)
        val indexes = (people.indices - linesWithQuery).toSet()
        printFound(indexes, people)

        return indexes
    }
}

fun main(args: Array<String>) {
    val file = File(args[1])
    val people = file.readLines().toMutableList()

    val indexMap = buildIndexMap(people)

    while (true) {
        println("\n=== Menu ===\n1. Find a person\n2. Print all people\n0. Exit")
        var strategy: String

        when (readLine()!!) {
            "1" -> {
                println("Select a matching strategy: ALL, ANY, NONE")
                strategy = readLine()!!
                if (strategy.uppercase() !in listOf("ALL", "ANY", "NONE")) {
                    println("Incorrect strategy! Try again.")
                    continue
                }

                println("Enter a name or email to search all suitable people.")
                val inp = readLine()!!.lowercase()
                when (strategy.uppercase()) {
                    "ANY" -> SearchStrategies.any(inp, indexMap, people)
                    "ALL" -> SearchStrategies.all(inp, indexMap, people)
                    "NONE" -> SearchStrategies.none(inp, indexMap, people)
                    else -> println("Incorrect strategy! Try again.")
                }
            }

            "2" -> {
                println("=== List of all people (${people.size}) ===")
                println(people.joinToString("\n"))
            }

            "0" -> break

            else -> println("Incorrect option! Try again.")
        }
    }
}