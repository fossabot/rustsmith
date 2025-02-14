package com.rustsmith

import com.andreapivetta.kolor.Color
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.rustsmith.ast.*
import com.rustsmith.exceptions.NoAvailableStatementException
import com.rustsmith.generation.IdentGenerator
import com.rustsmith.generation.selection.*
import com.rustsmith.logging.Logger
import com.rustsmith.recondition.Reconditioner
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import java.io.File
import kotlin.io.path.Path
import kotlin.random.Random

lateinit var CustomRandom: Random
lateinit var selectionManager: SelectionManager

class RustSmith : CliktCommand(name = "rustsmith") {
    private val count: Int by option(help = "No. of files", names = arrayOf("-n", "-count")).int().default(100)
    private val print: Boolean by option("-p", "-print", help = "Print out program only").flag(default = false)
    private val chosenSelectionManagers: List<SelectionManagerOptions> by argument(
        "selection-manager",
        help = "Choose selection manager(s) for generation"
    ).enum<SelectionManagerOptions>().multiple()
    private val failFast: Boolean by option("-f", "-fail-fast", help = "Use fail fast approach").flag(default = true)
    private val seed: Long? by option(help = "Optional Seed", names = arrayOf("-s", "-seed")).long()
    private val directory: String by option(help = "Directory to save files").default("outRust")

    enum class SelectionManagerOptions {
        BASE_SELECTION,
        SWARM_SELECTION,
        OPTIMAL_SELECTION,
        AGGRESSIVE_SELECTION
    }

    private fun getSelectionManager(): List<SelectionManager> {
        return chosenSelectionManagers.toSet().ifEmpty { setOf(SelectionManagerOptions.OPTIMAL_SELECTION) }.map {
            when (it) {
                SelectionManagerOptions.BASE_SELECTION -> BaseSelectionManager()
                SelectionManagerOptions.SWARM_SELECTION -> SwarmBasedSelectionManager(getRandomConfiguration())
                SelectionManagerOptions.OPTIMAL_SELECTION -> OptimalSelectionManager()
                SelectionManagerOptions.AGGRESSIVE_SELECTION -> AggressiveSelectionManager(AddExpression::class)
            }
        }
    }

    override fun run() {
        if (!print) {
            File(directory).deleteRecursively()
            File(directory).mkdirs()
        }
        // Don't make progress bar if printing out the program in console
        val progressBar = if (!print) ProgressBarBuilder().setTaskName("Generating").setInitialMax(count.toLong())
            .setStyle(ProgressBarStyle.ASCII).setUpdateIntervalMillis(10).build() else null
        var i = 0
        while (i < count) {
            val randomSeed = seed ?: Random.nextLong()
            CustomRandom = Random(randomSeed)
            selectionManager = getSelectionManager().random(CustomRandom)
            Logger.logText("Chosen selection manager ${selectionManager::class}", null, Color.YELLOW)
            val reconditioner = Reconditioner()
            try {
                val (generatedProgram, cliArguments) = generateProgram(randomSeed, failFast)
                if (generatedProgram.toRust().count { char -> char == '\n' } > 20000) continue
                val program = reconditioner.recondition(generatedProgram)
                if (print) {
                    println(program.toRust())
                    print(cliArguments.joinToString(" "))
                    return
                }
                val stats: MutableMap<String, Any> = reconditioner.nodeCounters.mapKeys { it.key.simpleName!! }.toMutableMap()
                stats["averageVarUse"] = reconditioner.variableUsageCounter.map { it.value }.sum().toDouble() / reconditioner.variableUsageCounter.size.toDouble()
                val path = Path(directory, "file$i")
                path.toFile().mkdir()
                path.resolve("file$i.rs").toFile().writeText(program.toRust())
                path.resolve("file$i.txt").toFile().writeText(cliArguments.joinToString(" "))
                path.resolve("file$i.json").toFile()
                    .writeText(
                        jacksonObjectMapper().writeValueAsString(
                            stats
                        )
                    )
                IdentGenerator.reset()
                progressBar?.step()
                i++
            } catch (e: NoAvailableStatementException) {
                continue
            }
        }
        progressBar?.close()
    }
}

fun main(args: Array<String>) = RustSmith().main(args)
