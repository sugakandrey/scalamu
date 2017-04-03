## Supported mutations

Mutation | Description | Example
---|--------|------------
InvertNegations | Removes negation of numerical values | `-0.5f` to `0.5f`, `-a + 1` to `a + 1`
ReplaceMathOperators | Replaces arithmetic operations on numerical values | `a + b` to `a - b`, `c / d` to `c * d`
AlwaysExecuteConditionals | Makes conditional statements always execute | `val foo = if (cond()) bar() else baz()` to `val foo = bar()`
NeverExecuteConditionals | Makes conditional statements never execute | `val foo = if (cond()) bar() else baz()` to `val foo = baz()`
NegateConditionals | Replaces all conditional operators with their logical counterpart | `a == b` to `a != b`, `foo <= baz` to `foo > baz` 
ChangeConditionalBoundaries | Replaces boundaries on conditional operators `>, <, >=, <=` | `a >= b` to `a > b`, `a < b` to `a <=b`
RemoveUnitMethodCalls | Removes calls to methods returning `Unit` | `val a = 123; doSideEffects(a)` to `val a = 123; ()`
ReplaceWithIdentityFunction | Replaces function literals typed `A => A` and method calls returning `this.type` with `id` | `Some("Hello").map(_ + "!")` to `Some("Hello").map(identity)`, `Set(1, 2) -- Set(1)` to `Set(1, 2)`
ChangeRangeBoundary | Swaps `to` and `until` | `1 until 10 by 1` to `1 to 10 by 1`, `-1f to -2f` to `-1f until -2f`
ReplaceLogicalOperators | Swaps `&&` and `||` operators on boolean values | `a && b` to `a || b`
ReplaceWithNone | Replaces calls to `Option.apply` and `Some.apply` with `None` | `Some(1).map(_ + 1)` to `Option.empty[Int].map(_ + 1)`
ReplaceWithNil | Replaces calls to `List.apply` with `Nil` | `List(0.5f, 0.6f, 0.7f)` to `List.empty[Float]`


