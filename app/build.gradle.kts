plugins { alias(libs.plugins.kotlin.jvm); application }
repositories { mavenCentral() }
dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.guava)
    val v = "21.0.2"; val p = "linux"
    implementation("org.openjfx:javafx-controls:$v")
    implementation("org.openjfx:javafx-base:$v:$p")
    implementation("org.openjfx:javafx-graphics:$v:$p")
    implementation("org.openjfx:javafx-controls:$v:$p")
}
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
application { mainClass = "gravityquest.Launcher" }
tasks.named<Test>("test") { useJUnitPlatform() }
