package com.interviewprep.brokencalc

// does all the math. tested it with 2+2 and it works
object Calculator {

    var tokens = ArrayList<String>()
    var pos = 0

    fun evaluate(expr: String): Double {
        tokens = tokenize(expr)
        pos = 0
        return parseExpression()
    }

    fun tokenize(expr: String): ArrayList<String> {
        val result = ArrayList<String>()
        var current = ""
        for (i in 0 until expr.length) {
            val c = expr[i]
            if (c.isDigit() || c == '.') {
                current += c
            } else {
                if (current != "") {
                    result.add(current)
                    current = ""
                }
                if (c == '−' || c == '-') {
                    val prev = if (result.size > 0) result[result.size - 1] else null
                    // minus at the start or after another operator means negative number
                    if (prev == null || prev == "+" || prev == "−" || prev == "×" || prev == "÷" || prev == "(") {
                        current = "-"
                    } else {
                        result.add("−")
                    }
                } else if (c == '+' || c == '×' || c == '÷' || c == '(' || c == ')') {
                    result.add(c.toString())
                }
                // anything else we just skip
            }
        }
        if (current != "") {
            result.add(current)
        }
        return result
    }

    fun parseExpression(): Double {
        val left = parseTerm()
        if (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "−")) {
            val op = tokens[pos]
            pos++
            val right = parseExpression()
            if (op == "+") return left + right else return left - right
        }
        return left
    }

    fun parseTerm(): Double {
        val left = parseFactor()
        if (pos < tokens.size && (tokens[pos] == "×" || tokens[pos] == "÷")) {
            val op = tokens[pos]
            pos++
            val right = parseTerm()
            if (op == "×") return left * right else return left / right
        }
        return left
    }

    fun parseFactor(): Double {
        val token = tokens[pos]
        pos++
        if (token == "(") {
            val value = parseExpression()
            if (pos < tokens.size && tokens[pos] == ")") pos++
            return value
        }
        if (token == "-") {
            return -parseFactor()
        }
        if (token == "+" || token == "−" || token == "×" || token == "÷" || token == ")") {
            throw IllegalStateException("Unexpected token " + token)
        }
        return token.toDouble()
    }

    fun format(value: Double): String {
        val precision = MainActivity.prefs.getInt("precission", -1)
        if (value == value.toLong().toDouble()) {
            return String.format("%,d", value.toLong())
        }
        if (precision < 0) {
            return value.toString()
        }
        return String.format("%." + precision + "f", value).trimEnd('0').trimEnd('.')
    }
}
