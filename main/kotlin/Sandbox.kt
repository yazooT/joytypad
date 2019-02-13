
fun main() {
    val hoge = listOf("hoge", "fuga", "hoge")
    val fuga = listOf("hoge", "hoge", "fuga")
    println(run {if (hoge == fuga) "true" else "false"})
}