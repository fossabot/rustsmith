package com.rustsmith

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.rustsmith.ast.generateProgram
import com.rustsmith.generation.IdentGenerator
import com.rustsmith.generation.selection.OptimalSelectionManager
import com.rustsmith.generation.selection.SelectionManager
import com.rustsmith.recondition.Reconditioner
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import java.io.File
import kotlin.io.path.Path
import kotlin.random.Random as Random1

lateinit var Random: Random1
lateinit var selectionManager: SelectionManager

class RustSmith : CliktCommand() {
    private val count: Int by option(help = "No. of files to generate", names = arrayOf("-n", "-count")).int()
        .default(100)
    private val print: Boolean by option("-p", "-print", help = "Print out program only").flag(default = false)
    private val seed: Long? by option(help = "Optional Seed", names = arrayOf("-s", "-seed")).long()
    private val directory: String by option(help = "Directory to save files").default("outRust")

    override fun run() {
        selectionManager = OptimalSelectionManager()

        if (!print) {
            File(directory).deleteRecursively()
            File(directory).mkdirs()
        }
        val mapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        val progressBar =
            ProgressBarBuilder().setTaskName("Generating").setInitialMax(count.toLong())
                .setStyle(ProgressBarStyle.ASCII).setUpdateIntervalMillis(10).build()
        repeat(count) {
            val randomSeed = seed ?: Random1.nextLong()
            Random = Random1(randomSeed)
            val program = Reconditioner.recondition(generateProgram(randomSeed))
            if (print) {
                println(program.toRust())
                return
            }
            val path = Path(directory, "file$it")
            path.toFile().mkdir()
            path.resolve("file$it.rs").toFile().writeText(program.toRust())
            path.resolve("file$it.json").toFile().writeText(mapper.writeValueAsString(program))
            IdentGenerator.reset()
            progressBar.step()
        }
        progressBar.close()
    }
}

fun main(args: Array<String>) = RustSmith().main(args)
