package com.rustsmith.ast

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.rustsmith.recondition.ReconditionedDivision
import com.rustsmith.recondition.ReconditionedMod
import java.math.BigInteger
import kotlin.reflect.KClass

annotation class ExpressionGenNode(val compatibleType: KClass<out Type>)

@JsonIgnoreProperties(value = ["symbolTable"])
sealed interface Expression : ASTNode {
    val symbolTable: SymbolTable
}

@ExpressionGenNode(I8Type::class)
data class Int8Literal(val value: Int, override val symbolTable: SymbolTable) : Expression {

    override fun toRust(): String {
        return "${value}i8"
    }
}

@ExpressionGenNode(I16Type::class)
data class Int16Literal(val value: Int, override val symbolTable: SymbolTable) : Expression {

    override fun toRust(): String {
        return "${value}i16"
    }
}

@ExpressionGenNode(I32Type::class)
data class Int32Literal(val value: Int, override val symbolTable: SymbolTable) :
    Expression {

    override fun toRust(): String {
        return "${value}i32"
    }
}

@ExpressionGenNode(I64Type::class)
data class Int64Literal(val value: Long, override val symbolTable: SymbolTable) :
    Expression {

    override fun toRust(): String {
        return "${value}i64"
    }
}

@ExpressionGenNode(I128Type::class)
data class Int128Literal(val value: BigInteger, override val symbolTable: SymbolTable) :
    Expression {

    override fun toRust(): String {
        return "${value}i128"
    }
}

@ExpressionGenNode(F32Type::class)
data class Float32Literal(val value: Float, override val symbolTable: SymbolTable) : Expression {
    override fun toRust(): String {
        return "${value}f32"
    }
}

@ExpressionGenNode(F64Type::class)
data class Float64Literal(val value: Double, override val symbolTable: SymbolTable) : Expression {
    override fun toRust(): String {
        return "${value}f64"
    }
}

@ExpressionGenNode(StringType::class)
data class StringLiteral(val value: String, override val symbolTable: SymbolTable) : Expression {

    override fun toRust(): String {
        return "String::from(\"$value\")"
    }
}

@ExpressionGenNode(BoolType::class)
data class BooleanLiteral(val value: Boolean, override val symbolTable: SymbolTable) : Expression {

    override fun toRust(): String {
        return value.toString()
    }
}

@ExpressionGenNode(TupleType::class)
data class TupleLiteral(val values: List<Expression>, override val symbolTable: SymbolTable) : Expression {

    override fun toRust(): String {
        return "(${values.joinToString(",") { it.toRust() }})"
    }
}

sealed interface PartialMoveExpression : ASTNode

@ExpressionGenNode(Type::class)
data class TupleElementAccessExpression(
    val expression: Expression,
    val index: Int,
    override val symbolTable: SymbolTable
) : RecursiveExpression, PartialMoveExpression {

    override fun toRust(): String {
        return "${expression.toRust()}.$index"
    }
}

// @ExpressionGenNode(Type::class)
// data class StructElementAccessExpression(
//    val expression: Expression,
//    val elementName: String,
//    override val symbolTable: SymbolTable
// ) : RecursiveExpression, PartialMoveExpression {
//
//    override fun toRust(): String {
//        return "${expression.toRust()}.$elementName"
//    }
// }

@ExpressionGenNode(Type::class)
data class Variable(val value: String, override val symbolTable: SymbolTable) : Expression {

    override fun toRust(): String {
        return value
    }
}

sealed interface RecursiveExpression : Expression

sealed interface BinOpExpression : RecursiveExpression {
    val expr1: Expression
    val expr2: Expression
}

@ExpressionGenNode(NumberType::class)
data class AddExpression(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} + ${expr2.toRust()})"
    }
}

@ExpressionGenNode(NumberType::class)
data class SubtractExpression(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} - ${expr2.toRust()})"
    }
}

@ExpressionGenNode(NumberType::class)
data class DivideExpression(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} / ${expr2.toRust()})"
    }
}

@ExpressionGenNode(NumberType::class)
data class MultiplyExpression(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} * ${expr2.toRust()})"
    }
}

@ExpressionGenNode(IntType::class)
data class ModExpression(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} % ${expr2.toRust()})"
    }
}

@ExpressionGenNode(BitWiseCompatibleType::class)
data class BitwiseAndLogicalAnd(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} & ${expr2.toRust()})"
    }
}

@ExpressionGenNode(BitWiseCompatibleType::class)
data class BitwiseAndLogicalOr(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} | ${expr2.toRust()})"
    }
}

@ExpressionGenNode(BitWiseCompatibleType::class)
data class BitwiseAndLogicalXor(
    override val expr1: Expression,
    override val expr2: Expression,
    override val symbolTable: SymbolTable
) : BinOpExpression {

    override fun toRust(): String {
        return "(${expr1.toRust()} ^ ${expr2.toRust()})"
    }
}

@ExpressionGenNode(Type::class)
data class GroupedExpression(
    val expression: Expression,
    override val symbolTable: SymbolTable
) : RecursiveExpression {
    override fun toRust(): String {
        return "(${expression.toRust()})"
    }
}

// @ExpressionGenNode
// data class ShiftLeftExpression(
//    override val expr1: Expression,
//    override val expr2: Expression,
//    override val symbolTable: SymbolTable
// ) : RecursiveExpression {
//
//    companion object : RandomizeableBinOp<ShiftLeftExpression> {
//        override fun createRandom(symbolTable: SymbolTable): ShiftLeftExpression {
//            val (expr1, expr2) = createExpressions(IntType::class, symbolTable)
//            return ShiftLeftExpression(expr1, expr1, symbolTable)
//        }
//    }
//
//    override fun toRust(): String {
//        return "${expr1.toRust()} << ${expr2.toRust()}"
//    }
// }

/* Nodes that affect the change of ownership of variables */

@ExpressionGenNode(Type::class)
data class FunctionCallExpression(
    val functionName: String,
    val args: List<Expression>,
    override val symbolTable: SymbolTable
) : Expression {

    override fun toRust(): String {
        return "$functionName(${args.joinToString(",") { it.toRust() }})"
    }
}

@ExpressionGenNode(StructType::class)
data class StructInstantiationExpression(
    val structName: String,
    val args: List<Pair<String, Expression>>,
    override val symbolTable: SymbolTable
) : Expression {

    override fun toRust(): String {
        return "$structName {${args.joinToString(" ") { "${it.first}: ${it.second.toRust()}," }}}"
    }
}

@ExpressionGenNode(Type::class)
data class BlockExpression(
    val statement: StatementBlock,
    val type: Type?,
    override val symbolTable: SymbolTable
) : RecursiveExpression {
    override fun toRust(): String {
        return "{\n${statement.toRust()}\n}"
    }
}

@ExpressionGenNode(Type::class)
data class IfElseExpression(
    val predicate: Expression,
    val ifBlock: BlockExpression,
    val elseBlock: BlockExpression,
    override val symbolTable: SymbolTable
) : RecursiveExpression {
    override fun toRust(): String {
        return "if (${predicate.toRust()}) \n {\n ${ifBlock.toRust()} \n} else {\n ${elseBlock.toRust()} \n}"
    }
}

sealed interface ReconditionedExpression : Expression

data class WrappingAdd(val addExpression: AddExpression, override val symbolTable: SymbolTable) :
    ReconditionedExpression {
    override fun toRust(): String {
        return "${addExpression.expr1.toRust()}.wrapping_add(${addExpression.expr2.toRust()})"
    }
}

data class WrappingMul(
    val multiplyExpression: MultiplyExpression,
    override val symbolTable: SymbolTable
) :
    ReconditionedExpression {
    override fun toRust(): String {
        return "${multiplyExpression.expr1.toRust()}.wrapping_mul(${multiplyExpression.expr2.toRust()})"
    }
}

data class WrappingSubtract(
    val subtractExpression: SubtractExpression,
    override val symbolTable: SymbolTable
) :
    ReconditionedExpression {
    override fun toRust(): String {
        return "${subtractExpression.expr1.toRust()}.wrapping_sub(${subtractExpression.expr2.toRust()})"
    }
}

data class ReconditionedDivisionExpression(
    val divideExpression: DivideExpression,
    override val symbolTable: SymbolTable
) :
    ReconditionedExpression {
    override fun toRust(): String {
        val zeroExpression = (divideExpression.expr1.toType() as NumberType).zero(symbolTable)
        return "${ReconditionedDivision.macroName}!(${divideExpression.expr1.toRust()}, ${divideExpression.expr2.toRust()}, ${zeroExpression.toRust()})"
    }
}

data class ReconditionedModExpression(
    val modExpression: ModExpression,
    override val symbolTable: SymbolTable
) :
    ReconditionedExpression {
    override fun toRust(): String {
        val zeroExpression = (modExpression.expr1.toType() as NumberType).zero(symbolTable)
        return "${ReconditionedMod.macroName}!(${modExpression.expr1.toRust()}, ${modExpression.expr2.toRust()}, ${zeroExpression.toRust()})"
    }
}

fun Expression.toType(): Type {
    return when (this) {
        is Int8Literal -> I8Type
        is Int16Literal -> I16Type
        is Int32Literal -> I32Type
        is Int64Literal -> I64Type
        is Int128Literal -> I128Type
        is Float32Literal -> F32Type
        is Float64Literal -> F64Type
        is StringLiteral -> StringType
        is BooleanLiteral -> BoolType
        is Variable -> symbolTable[this.value]!!.type
        is WrappingAdd -> this.addExpression.toType()
        is WrappingMul -> this.multiplyExpression.toType()
        is WrappingSubtract -> this.subtractExpression.toType()
        is ReconditionedDivisionExpression -> this.divideExpression.toType()
        is BinOpExpression -> this.expr1.toType()
        is GroupedExpression -> this.expression.toType()
        is BlockExpression -> this.type!!
        is ReconditionedModExpression -> this.modExpression.toType()
        is IfElseExpression -> this.ifBlock.type!!
        is FunctionCallExpression -> (symbolTable.functionSymbolTable[this.functionName]!!.type as FunctionType).returnType
        is TupleLiteral -> TupleType(this.values.map { it.toType() })
        is StructInstantiationExpression -> symbolTable.globalSymbolTable[this.structName]!!.type
        is TupleElementAccessExpression -> (this.expression.toType() as TupleType).types[this.index]
//        is StructElementAccessExpression -> (this.expression.toType() as StructType).types.first { it.first == elementName }.second
    }
}

fun Expression.toStatement(addSemicolon: Boolean = true): ExpressionStatement {
    return ExpressionStatement(this, addSemicolon, symbolTable)
}
