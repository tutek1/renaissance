package org.renaissance.jdk.streams

import org.renaissance.Benchmark
import org.renaissance.Benchmark._
import org.renaissance.BenchmarkContext
import org.renaissance.BenchmarkResult
import org.renaissance.BenchmarkResult.Assert
import org.renaissance.License

import scala.jdk.CollectionConverters._

@Name("scrabble")
@Group("jdk-streams")
@Group("functional") // With Scala 3, the primary group goes last.
@Summary("Solves the Scrabble puzzle using JDK Streams.")
@Licenses(Array(License.GPL2))
@Repetitions(50)
@Parameter(name = "input_path", defaultValue = "/shakespeare.txt")
@Parameter(
  name = "expected_result",
  defaultValue = "120--QUICKLY,118--ZEPHYRS,114--QUALIFY-QUICKEN-QUICKER"
)
@Parameter(name = "run_parallel", defaultValue = "true")
@Parameter(name = "repeat_input", defaultValue = "1")
@Parameter(name = "generated_words", defaultValue = "0")
@Configuration(
  name = "test",
  settings = Array(
    "input_path = /shakespeare-truncated.txt",
    "expected_result = 120--QUICKLY,114--QUICKEN-QUICKER,108--BLAZING-PRIZING"
  )
)
@Configuration(name = "jmh")
final class Scrabble extends Benchmark {

  // TODO: Consolidate benchmark parameters across the suite.
  //  See: https://github.com/renaissance-benchmarks/renaissance/issues/27

  private var inputPathParam: String = _

  private var expectedResultParam: Seq[String] = _

  private val scrabblePath = "/scrabble.txt"

  private var runParallel: Boolean = _

  private var repeatInput: Integer = _

  private var generatedWords: Integer = _

  private var scrabble: JavaScrabble = _

  override def setUpBeforeAll(c: BenchmarkContext): Unit = {
    inputPathParam = c.parameter("input_path").value
    expectedResultParam = c.parameter("expected_result").toList().asScala.toSeq
    runParallel = c.parameter("run_parallel").toBoolean()
    repeatInput = c.parameter("repeat_input").toInteger()
    generatedWords = c.parameter("generated_words").toInteger()
    scrabble = new JavaScrabble(inputPathParam, scrabblePath, repeatInput, generatedWords, runParallel)
  }

  override def run(c: BenchmarkContext): BenchmarkResult = {
    val result = scrabble.run()

    () => {
      val actualWords = JavaScrabble.prepareForValidation(result)
      Assert.assertEquals(
        expectedResultParam.size,
        actualWords.size,
        "best words count"
      )

      for ((expected, actual) <- expectedResultParam zip actualWords.asScala) {
        Assert.assertEquals(expected, actual, "best words")
      }
    }
  }
}
