// reverse the string
fun reverseUsingInbuildFunction( Str : String ) : String = Str.reversed()
fun reverseWithoutInbuildFunction( Str : String ) : String {
    var reversedStr = ""
    for (i in Str.length - 1 downTo 0) {
        reversedStr += Str[i]
    }
    return reversedStr
}


fun main() {
    val Str = "Hello World"

    println(reverseUsingInbuildFunction(Str))
    println(reverseWithoutInbuildFunction(Str))
    println("Hello World")
}