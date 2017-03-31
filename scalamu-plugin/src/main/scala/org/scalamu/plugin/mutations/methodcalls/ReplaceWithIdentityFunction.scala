//package org.scalamu.plugin.mutations
//
//import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}
//
///**
//  * Mutation, that replaces 
// *
// */
//object ReplaceWithIdentityFunction extends Mutation { self =>
//  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
//    new MutatingTransformer(context) {
//      import context.global._
//
//      override protected def mutation: Mutation = self
//
//      override protected def transformer(): Transformer = {
//        case q"(..${List(arg)}) => $expr" if expr.tpe =:= arg.tpe =>
//          reify[Any => Any](identity).tree
//        case tree @ q"$fn(..${List(arg)}" =>
//          fn.tpe match {
//            case MethodType(List(param), result) if result =:= param.toType => q"$arg"
//            case _                                                          => tree
//          }
//        case tree => tree
//      }
//    }
//}
