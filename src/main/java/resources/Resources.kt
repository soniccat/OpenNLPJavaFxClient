package resources

object Resources {
    private val basePath = "/resources"
    private val layoutsPath = "$basePath/layouts"
    private val modelsPath = "$basePath/models"

    fun layout(name: String) = javaClass.getResource("$layoutsPath/$name")
    fun modelAsStream(name: String) = javaClass.getResourceAsStream("$modelsPath/$name")
}